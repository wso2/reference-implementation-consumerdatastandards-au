/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.openbanking.consumerdatastandards.au.extensions.utils;

import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.minidev.json.parser.JSONParser;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CDSErrorEnum;
import org.wso2.openbanking.consumerdatastandards.au.extensions.constants.CommonConstants;
import org.wso2.openbanking.consumerdatastandards.au.extensions.exceptions.CDSConsentException;

/**
 * Utility class for consent authorization related operations.
 */
public class ConsentAuthorizationUtil {

    private static final Log log = LogFactory.getLog(ConsentAuthorizationUtil.class);
    private static final int secondsInYear = (int) TimeUnit.SECONDS.convert(365, TimeUnit.DAYS);

    /**
     * Extracts required data from the given request object.
     *
     * @param jsonRequestBody The JSON object representing the request payload from which required data will be extracted.
     * @return A map containing the extracted data, or error information if extraction fails.
     * @throws CDSConsentException If there is an error while parsing the request object or extracting required data.
     */
    public static Map<String, Object> extractRequiredDataFromRequestObject(JSONObject jsonRequestBody) throws CDSConsentException {

        String clientID;
        Map<String, Object> dataMap = new HashMap<>();

        try {
            long sharingDuration = 0;
            clientID = jsonRequestBody.getString(CommonConstants.CLIENT_ID);

            // Validate client_id existence
            if (StringUtils.isBlank(clientID)) {
                log.error("client_id not found in request object");
                dataMap.put(CommonConstants.IS_ERROR, "client_id not found in request object");
                return dataMap;
            }
            dataMap.put(CommonConstants.CLIENT_ID, clientID);

            // Verify claims
            if (jsonRequestBody.has(CommonConstants.CLAIMS)) {
                JSONObject claims = (JSONObject) jsonRequestBody.get(CommonConstants.CLAIMS);
                if (claims.has(CommonConstants.SHARING_DURATION)) {

                    String sharingDurationStr = claims.get(CommonConstants.SHARING_DURATION) == null ?
                            StringUtils.EMPTY : claims.get(CommonConstants.SHARING_DURATION).toString();

                    sharingDuration = sharingDurationStr.isEmpty() ? 0 : Long.parseLong(sharingDurationStr);

                    if (sharingDuration > secondsInYear) {
                        sharingDuration = secondsInYear;
                        if (log.isDebugEnabled()) {
                            log.debug("Requested sharing_duration is greater than a year,therefore one year duration"
                                    + " is set as consent expiration for request object of client: "
                                    + dataMap.get(CommonConstants.CLIENT_ID));
                        }
                    }
                    dataMap.put(CommonConstants.EXPIRATION_DATE_TIME,
                            getConsentExpiryDateTime(sharingDuration));
                }
                if (sharingDuration == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("sharing_duration not found in the request object of client: " + clientID);
                    }
                    dataMap.put(CommonConstants.EXPIRATION_DATE_TIME, 0);
                }

                // adding original sharing_duration_value to data map
                dataMap.put(CommonConstants.SHARING_DURATION_VALUE, sharingDuration);

                // Extracting cdr_arrangement_id, id_token and userinfo claims if exist
                if (claims.has(CommonConstants.CDR_ARRANGEMENT_ID)
                        && claims.get(CommonConstants.CDR_ARRANGEMENT_ID) != null) {
                    dataMap.put(CommonConstants.CDR_ARRANGEMENT_ID,
                            claims.get(CommonConstants.CDR_ARRANGEMENT_ID).toString());
                }

                String idTokenJsonString = claims.has(CommonConstants.ID_TOKEN) ?
                        claims.get(CommonConstants.ID_TOKEN).toString() : null;

                String userInfoJsonString = claims.has(CommonConstants.USERINFO) ?
                        claims.get(CommonConstants.USERINFO).toString() : null;

                JSONParser parser = new JSONParser();
                dataMap.put(CommonConstants.ID_TOKEN_CLAIMS, StringUtils.isNotBlank(idTokenJsonString) ?
                        parser.parse(idTokenJsonString) : new JSONObject());
                dataMap.put(CommonConstants.USERINFO_CLAIMS, StringUtils.isNotBlank(userInfoJsonString) ?
                        parser.parse(userInfoJsonString) : new JSONObject());
            }
        } catch (ParseException e) {
            log.error("Error while parsing the request object", e);
            throw new CDSConsentException(CDSErrorEnum.UNEXPECTED_ERROR, "Error while parsing the request object: " + e);
        }
        return dataMap;
    }


    /**
     * Calculate consent expiry date time based on sharing duration.
     * @param sharingDuration the duration in seconds for which the consent is valid
     * @return the calculated consent expiry date and time as an {@link OffsetDateTime}
     */
    private static OffsetDateTime getConsentExpiryDateTime(long sharingDuration) {

        OffsetDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC);
        return currentTime.plusSeconds(sharingDuration);
    }

    /**
     * Set profile scope related individual claims as permissions.
     *
     * @param jsonRequestBody  request payload
     * @param userInfoClaims user info claims
     * @param idTokenClaims  id token claims
     */
    private void setClaimPermissions(JSONObject consentData, JSONObject jsonRequestBody, JSONObject userInfoClaims, JSONObject idTokenClaims) {

        StringBuilder scopeString = new StringBuilder(jsonRequestBody.getString("scope"));

        List[] clusters = {CommonConstants.NAME_CLUSTER_CLAIMS,
                CommonConstants.PHONE_CLUSTER_CLAIMS,
                CommonConstants.EMAIL_CLUSTER_CLAIMS,
                CommonConstants.MAIL_CLUSTER_CLAIMS};

        for (List<String> cluster : clusters) {
            for (String claim : cluster) {
                if (userInfoClaims.has(claim) || idTokenClaims.has(claim)) {
                    scopeString.append(" ");
                    scopeString.append(claim);
                }
            }
        }
        consentData.put("scope", scopeString.toString().trim());
    }
}
