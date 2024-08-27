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

package org.wso2.openbanking.cds.common.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.enums.AuthorisationFlowTypeEnum;
import org.wso2.openbanking.cds.common.enums.AuthorisationStageEnum;
import org.wso2.openbanking.cds.common.enums.ConsentDurationTypeEnum;
import org.wso2.openbanking.cds.common.enums.ConsentStatusEnum;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * CDS Common Utils.
 */
public class CDSCommonUtils {

    private static final String HMACSHA256 = "HmacSHA256";

    private static final Log LOG = LogFactory.getLog(CDSCommonUtils.class);

    /**
     * Method to retrieve the request URI key from the request URI.
     *
     * @param requestUri request URI
     * @return request URI key
     */
    public static String getRequestUriKey(String requestUri) {

        if (StringUtils.isBlank(requestUri)) {
            LOG.error("Request URI not found.");
            return null;
        }

        String[] uriParts = requestUri.split(":");
        String requestUriKey = uriParts[uriParts.length - 1];

        return StringUtils.isBlank(requestUriKey) ? null : requestUriKey;
    }

    /**
     * Returns the request URI key after retrieving from the spQueryParams.
     *
     * @param spQueryParams query params
     * @return request URI key
     */
    public static String getRequestUriKeyFromQueryParams(String spQueryParams) {

        List<NameValuePair> params = URLEncodedUtils.parse(spQueryParams, StandardCharsets.UTF_8);

        for (NameValuePair param : params) {
            if (CommonConstants.REQUEST_URI.equals(param.getName())) {
                return getRequestUriKey(param.getValue());
            }
        }

        return null;
    }

    /**
     * Encrypt access token using HmacSHA256.
     *
     * @param accessToken String access token
     * @return encrypted token
     */
    public static String encryptAccessToken(String accessToken) {

        try {
            byte[] secretKey = OpenBankingCDSConfigParser.getInstance().getTokenEncryptionSecretKey()
                    .getBytes(StandardCharsets.UTF_8);
            Mac mac = Mac.getInstance(HMACSHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, HMACSHA256);
            mac.init(secretKeySpec);
            accessToken = new String(Hex.encodeHex(mac.doFinal(accessToken.getBytes(StandardCharsets.UTF_8))));
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Unable to encrypt the access token. Invalid encryption algorithm.", e);
            return null;
        } catch (InvalidKeyException e) {
            LOG.error("Unable to encrypt the access token. Invalid encryption key.", e);
            return null;
        }
        return accessToken;
    }

    /**
     * Returns the type of consent duration based on the sharing duration value.
     *
     * @param sharingDurationValue sharing duration value
     * @return consent duration type
     */
    public static ConsentDurationTypeEnum getConsentDurationType(String sharingDurationValue) {

        if (StringUtils.isEmpty(sharingDurationValue)) {
            return ConsentDurationTypeEnum.ONCE_OFF;
        }

        try {
            long sharingDurationInSeconds = Long.parseLong(sharingDurationValue.trim());
            if (sharingDurationInSeconds <= CommonConstants.ONE_DAY_IN_SECONDS) {
                return ConsentDurationTypeEnum.ONCE_OFF;
            }
        } catch (NumberFormatException e) {
            LOG.error("Error when converting sharing duration to a number.", e);
        }

        return ConsentDurationTypeEnum.ONGOING;
    }

    /**
     * Generate abandoned consent flow data map.
     *
     * @param requestUriKey request uri key
     * @param consentId     consent id
     * @param stage         consent flow stage
     * @return abandoned consent flow data map
     */
    public static Map<String, Object> generateAbandonedConsentFlowDataMap(String requestUriKey, String consentId,
                                                                          AuthorisationStageEnum stage) {

        Map<String, Object> abandonedConsentFlowDataMap = new HashMap<>();

        abandonedConsentFlowDataMap.put("requestUriKey", requestUriKey);
        abandonedConsentFlowDataMap.put("consentId", consentId);
        abandonedConsentFlowDataMap.put("stage", stage.toString());
        abandonedConsentFlowDataMap.put("timestamp", Instant.now().toEpochMilli());
        return abandonedConsentFlowDataMap;
    }

    /**
     * Generate authorisation data map.
     *
     * @param consentId           consent id
     * @param consentStatus       consent status
     * @param authFlowType        authorisation flow type
     * @param customerProfile     customer profile
     * @param consentDurationType consent duration type
     * @return authorisation data map
     */
    public static Map<String, Object> generateAuthorisationDataMap(String consentId, ConsentStatusEnum consentStatus,
                                                                   AuthorisationFlowTypeEnum authFlowType,
                                                                   String customerProfile,
                                                                   ConsentDurationTypeEnum consentDurationType) {

        Map<String, Object> authorisationDataMap = new HashMap<>();

        authorisationDataMap.put("consentId", consentId);
        authorisationDataMap.put("consentStatus", consentStatus.toString());
        authorisationDataMap.put("authFlowType", authFlowType.toString());
        authorisationDataMap.put("customerProfile", customerProfile);
        authorisationDataMap.put("consentDurationType", consentDurationType.toString());
        authorisationDataMap.put("timestamp", Instant.now().toEpochMilli());
        return authorisationDataMap;
    }
}
