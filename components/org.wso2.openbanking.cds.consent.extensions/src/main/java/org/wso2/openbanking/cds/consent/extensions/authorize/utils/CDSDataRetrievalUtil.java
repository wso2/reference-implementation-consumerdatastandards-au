/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.openbanking.cds.consent.extensions.authorize.utils;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.exception.PushAuthRequestValidatorException;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.util.PushAuthRequestValidatorUtils;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.openbanking.cds.consent.extensions.authorize.impl.model.AccountConsentRequest;
import org.wso2.openbanking.cds.consent.extensions.authorize.impl.model.AccountData;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Util class for Consent authorize CDS implementation.
 */
public class CDSDataRetrievalUtil {

    private static final Log log = LogFactory.getLog(CDSDataRetrievalUtil.class);
    private static final String JWT_PART_DELIMITER = "\\.";
    private static final int NUMBER_OF_PARTS_IN_JWE = 5;

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    // Suppressed content - try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient())
    // Suppression reason - False Positive : This occurs with Java 11 when using try-with-resources and when that
    //                                       resource is being referred within the try block. This is a known issue in
    //                                       the plugin and therefore it is being suppressed.
    //                                       https://github.com/spotbugs/spotbugs/issues/1694
    public static String getAccountsFromEndpoint(String sharableAccountsRetrieveUrl, Map<String, String> parameters,
                                                 Map<String, String> headers) {

        String retrieveUrl = "";
        if (!sharableAccountsRetrieveUrl.endsWith(CDSConsentExtensionConstants.SERVICE_URL_SLASH)) {
            retrieveUrl = sharableAccountsRetrieveUrl + CDSConsentExtensionConstants.SERVICE_URL_SLASH;
        } else {
            retrieveUrl = sharableAccountsRetrieveUrl;
        }
        if (!parameters.isEmpty()) {
            retrieveUrl = buildRequestURL(retrieveUrl, parameters);
        }

        if (log.isDebugEnabled()) {
            log.debug("Sharable accounts retrieve endpoint : " + retrieveUrl);
        }

        try (CloseableHttpClient client = HTTPClientUtils.getHttpsClient()) {
            HttpGet request = new HttpGet(retrieveUrl);
            request.addHeader(CDSConsentExtensionConstants.ACCEPT_HEADER_NAME,
                    CDSConsentExtensionConstants.ACCEPT_HEADER_VALUE);
            if (!headers.isEmpty()) {
                for (Map.Entry<String, String> key : headers.entrySet()) {
                    if (key.getKey() != null && key.getValue() != null) {
                        request.addHeader(key.getKey(), key.getValue());
                    }
                }
            }
            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                log.error("Retrieving sharable accounts failed");
                return null;
            } else {
                InputStream in = response.getEntity().getContent();
                return IOUtils.toString(in, String.valueOf(StandardCharsets.UTF_8));
            }
        } catch (IOException | OpenBankingException e) {
            log.error("Exception occurred while retrieving sharable accounts", e);
        }
        return null;
    }

    /**
     * Build the complete URL with query parameters sent in the map.
     *
     * @param baseURL    the base URL
     * @param parameters map of parameters
     * @return the output URL
     */
    public static String buildRequestURL(String baseURL, Map<String, String> parameters) {

        List<NameValuePair> pairs = new ArrayList<>();

        for (Map.Entry<String, String> key : parameters.entrySet()) {
            if (key.getKey() != null && key.getValue() != null) {
                pairs.add(new BasicNameValuePair(key.getKey(), key.getValue()));
            }
        }
        String queries = URLEncodedUtils.format(pairs, CDSConsentExtensionConstants.CHAR_SET);
        return baseURL + "?" + queries;
    }

    /**
     * convert the scope string to permission enum list.
     *
     * @param scopeString string containing the requested scopes
     * @return list of permission enums to be stored
     */
    public static List<PermissionsEnum> getPermissionList(String scopeString) {

        ArrayList<PermissionsEnum> permissionList = new ArrayList<>();
        if (StringUtils.isNotBlank(scopeString)) {
            // Remove "openid", "profile" and "cdr:registration" from the scope list to display.
            List<String> openIdScopes = Stream.of(scopeString.split(" "))
                    .filter(x -> (!StringUtils.equalsIgnoreCase(x, CDSConsentExtensionConstants.OPENID_SCOPE)
                            && !StringUtils.equalsIgnoreCase(x, CDSConsentExtensionConstants.CDR_REGISTRATION_SCOPE)))
                    .collect(Collectors.toList());
            for (String scope : openIdScopes) {
                PermissionsEnum permissionsEnum = PermissionsEnum.fromValue(scope);
                permissionList.add(permissionsEnum);
            }
        }
        return permissionList;
    }

    /**
     * Method to extract request object from query params.
     *
     * @param spQueryParams
     * @return
     */
    public static String extractRequestObject(String spQueryParams) throws ConsentException {
        if (spQueryParams != null && !spQueryParams.trim().isEmpty()) {
            String requestObject = null;
            String[] spQueries = spQueryParams.split("&");
            String clientId = null;
            for (String param : spQueries) {

                if (param.contains("client_id=")) {
                    clientId = param.split("client_id=")[1];
                }
                if (param.contains("request=")) {
                    requestObject = (param.substring("request=".length())).replaceAll(
                            "\\r\\n|\\r|\\n|\\%20", "");
                } else if (param.contains("request_uri=")) {
                    log.debug("Resolving request URI during Steps execution");
                    String[] requestUri = (param.substring("request_uri=".length())).replaceAll(
                            "(?i)\\%3A", ":").split(":");
                    // session key will be obtained splitting the request uri with ":" and getting the last index
                    // sample request_uri - urn:<substring>:<sessionKey>
                    String sessionKey = requestUri[(requestUri.length - 1)];
                    SessionDataCacheKey cacheKey = new SessionDataCacheKey(sessionKey);
                    SessionDataCacheEntry sessionDataCacheEntry = SessionDataCache.
                            getInstance().getValueFromCache(cacheKey);
                    if (sessionDataCacheEntry != null) {
                        // essential claims - <request object JWT>:<request object JWT expiry time>
                        String requestObjectFromCache = sessionDataCacheEntry.getoAuth2Parameters().
                                getEssentialClaims().split(":")[0];
                        // check whether request object is encrypted
                        if (requestObjectFromCache.split(JWT_PART_DELIMITER).length == NUMBER_OF_PARTS_IN_JWE) {
                            try {
                                // decrypt request object assuming it was signed before encrypting therefore,
                                // return value is a singed JWT
                                requestObject = PushAuthRequestValidatorUtils.decrypt(requestObjectFromCache, clientId);
                            } catch (PushAuthRequestValidatorException e) {
                                log.error("Error occurred while decrypting", e);
                                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                                        "Request object cannot be extracted");
                            }
                        } else {
                            // cached request object should be a signed JWT in this scenario
                            requestObject = requestObjectFromCache;
                        }
                        log.debug("Removing request_URI entry from cache");
                        SessionDataCache.getInstance().clearCacheEntry(cacheKey);
                    } else {
                        log.error("Could not find cache entry with request URI");
                        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                                "Request object cannot be extracted");
                    }
                }
            }
            if (requestObject != null) {
                return requestObject;
            }
        }
        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Request object cannot be extracted");
    }

    /**
     * Method to extract redirect url from query params.
     *
     * @param spQueryParams
     * @return
     */
    public static String getRedirectURL(String spQueryParams) throws ConsentException {
        if (spQueryParams != null && !spQueryParams.trim().isEmpty()) {
            String redirectURL = null;
            String[] spQueryParamList = spQueryParams.split("&");
            for (String param : spQueryParamList) {
                if (param.startsWith("redirect_uri=")) {
                    redirectURL = param.substring("redirect_uri=".length());
                }
            }
            if (redirectURL != null) {
                try {
                    return URLDecoder.decode(redirectURL, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    log.error("Error occurred while decoding redirect URL", e);
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            "Error occurred while decoding redirect URL");
                }
            }
        }
        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Redirect URL cannot be extracted");
    }

    /**
     * Method to extract state from query params.
     *
     * @param spQueryParams query params
     * @return state
     */
    public static String getStateParameter(String spQueryParams) {
        String state = null;
        if (spQueryParams != null && !spQueryParams.trim().isEmpty()) {
            String[] spQueryParamList = spQueryParams.split("&");
            for (String param : spQueryParamList) {
                if (param.startsWith("state=")) {
                    state = param.substring("state=".length());
                    break;
                }
            }
        }
        return state;
    }

    /**
     * Maps data to AccountConsentRequest model.
     *
     * @param consentData     consent data
     * @param date            expirationDate
     * @param permissionsList permissions list
     * @return an AccountConsentRequest model
     */
    public static AccountConsentRequest getAccountConsent(ConsentData consentData, String date,
                                                          List<PermissionsEnum> permissionsList) {
        AccountConsentRequest accountConsentRequest = new AccountConsentRequest();
        AccountData accountData = new AccountData();
        accountData.setPermissions(permissionsList);
        accountData.setExpirationDateTime(date);
        accountConsentRequest.setAccountData(accountData);
        accountConsentRequest.setRequestId(consentData.getConsentId());
        return accountConsentRequest;
    }

    /**
     * Get service provider full name.
     *
     * @param clientId clientId
     * @return service provider full name
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public static String getServiceProviderFullName(String clientId) throws OpenBankingException {
        IdentityCommonHelper identityCommonHelper = new IdentityCommonHelper();
        String spOrgName = identityCommonHelper.getAppPropertyFromSPMetaData(clientId, "org_name");
        String spClientName = identityCommonHelper.getAppPropertyFromSPMetaData(clientId, "client_name");
        return String.format("%s, %s", spOrgName, spClientName);
    }

    public static String getExpiryFromReceipt(String receiptString) throws ConsentException {

        try {
            Object receiptJSON = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(receiptString);
            if (!(receiptJSON instanceof JSONObject)) {
                log.error("Receipt is not a JSON object");
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Receipt is not a JSON object");
            }
            JSONObject receipt = (JSONObject) receiptJSON;
            return ((JSONObject) receipt.get(CDSConsentExtensionConstants.ACCOUNT_DATA)).
                    getAsString(CDSConsentExtensionConstants.EXPIRATION_DATE_TIME);
        } catch (ParseException e) {
            log.error(String.format("Exception occurred while parsing the consent receipt. %s", e.getMessage()));
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occurred while parsing the consent receipt");
        }
    }

    public static JSONArray getPermissionsFromReceipt(String receiptString) throws ConsentException {

        try {
            Object receiptJSON = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(receiptString);
            if (!(receiptJSON instanceof JSONObject)) {
                log.error("Receipt is not a JSON object");
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Receipt is not a JSON object");
            }
            JSONObject receipt = (JSONObject) receiptJSON;
            JSONArray permissionEnumsArray = new JSONArray();
            JSONObject accountData = (JSONObject) receipt.get(CDSConsentExtensionConstants.ACCOUNT_DATA);
            JSONArray permissionNames = (JSONArray) accountData.get(CDSConsentExtensionConstants.PERMISSIONS);

            for (Object permissionName : permissionNames) {
                PermissionsEnum permissionEnum = PermissionsEnum.valueOf(permissionName.toString());
                permissionEnumsArray.add(permissionEnum);
            }
            return permissionEnumsArray;
        } catch (ParseException e) {
            log.error(String.format("Exception occurred while parsing the consent receipt. %s", e.getMessage()));
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occurred while parsing the consent receipt");
        }
    }

}
