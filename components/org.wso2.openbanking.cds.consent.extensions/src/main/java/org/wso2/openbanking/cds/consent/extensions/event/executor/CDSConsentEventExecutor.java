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

package org.wso2.openbanking.cds.consent.extensions.event.executor;

import com.wso2.openbanking.accelerator.common.event.executor.OBEventExecutor;
import com.wso2.openbanking.accelerator.common.event.executor.model.OBEvent;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import com.wso2.openbanking.accelerator.identity.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.minidev.json.JSONObject;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.data.publisher.CDSDataPublishingService;
import org.wso2.openbanking.cds.common.enums.AuthorisationFlowTypeEnum;
import org.wso2.openbanking.cds.common.enums.ConsentDurationTypeEnum;
import org.wso2.openbanking.cds.common.enums.ConsentStatusEnum;
import org.wso2.openbanking.cds.common.utils.CDSCommonUtils;
import org.wso2.openbanking.cds.common.utils.CommonConstants;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;
import org.wso2.openbanking.cds.identity.utils.CDSIdentityUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * CDS event executor implementation to execute consent state change related events.
 */
public class CDSConsentEventExecutor implements OBEventExecutor {

    private static final Log log = LogFactory.getLog(CDSConsentEventExecutor.class);
    private CDSDataPublishingService dataPublishingService = CDSDataPublishingService.getCDSDataPublishingService();
    private static final String DATA_RECIPIENT_CDR_ARRANGEMENT_REVOCATION_PATH = "/arrangements/revoke";
    private static final String REVOKED_STATE = "revoked";
    private static final String EXPIRED_STATE = "expired";
    private static final String AUTHORIZED_STATE = "authorized";
    private static final String AMENDED_STATE = "amended";
    private static final String REASON = "Reason";
    private static final String CLIENT_ID = "ClientId";
    private static final String CONSENT_ID = "ConsentId";
    private static final String USER_ID = "UserId";
    private static final String VALIDITY_PERIOD = "ValidityPeriod";
    private static final String CONSENT_DATA_MAP = "ConsentDataMap";
    private static final String CONSENT_RESOURCE = "ConsentResource";
    private static final String DETAILED_CONSENT_RESOURCE = "DetailedConsentResource";
    private static final String REQUEST_URI_KEY = "requestUriKey";

    // Data publishing related constants.
    private static final String CLIENT_ID_KEY = "clientId";
    private static final String CONSENT_ID_KEY = "consentId";
    private static final String USER_ID_KEY = "userId";
    private static final String STATUS_KEY = "status";
    private static final String EXPIRY_TIME_KEY = "expiryTime";
    private static final ConcurrentLinkedDeque<String> publishedRequestUriKeyQueue = new ConcurrentLinkedDeque<>();

    @Override
    public void processEvent(OBEvent obEvent) {

        Map<String, Object> eventData = obEvent.getEventData();

        if (Boolean.parseBoolean(OpenBankingCDSConfigParser.getInstance().getConfiguration()
                .get(CDSConsentExtensionConstants.ENABLE_RECIPIENT_CONSENT_REVOCATION).toString())
                && ConsentCoreServiceConstants.CONSENT_REVOKE_FROM_DASHBOARD_REASON.equals(eventData.get(REASON))
                && REVOKED_STATE.equalsIgnoreCase(obEvent.getEventType())) {

            // call DR's arrangement revocation endpoint
            try {
                if (eventData.get(CLIENT_ID) != null && eventData.get(CONSENT_ID) != null) {
                    sendArrangementRevocationRequestToADR(eventData.get(CLIENT_ID).toString(),
                            eventData.get(CONSENT_ID).toString(), OpenBankingCDSConfigParser.getInstance()
                                    .getConfiguration().get(CDSConsentExtensionConstants.DATA_HOLDER_ID).toString());
                } else {
                    log.error("Consent ID/Client ID cannot be null");
                }
            } catch (OpenBankingException e) {
                log.error("Something went wrong when sending the arrangement revocation request to ADR", e);
            }
        }

        HashMap<String, Object> consentDataMap = (HashMap<String, Object>) eventData
                .get(CONSENT_DATA_MAP);
        ConsentResource consentResource = (ConsentResource) consentDataMap
                .get(CONSENT_RESOURCE);
        DetailedConsentResource detailedConsentResource = (DetailedConsentResource) consentDataMap
                .get(DETAILED_CONSENT_RESOURCE);

        // Publish consent data for metrics.
        if (REVOKED_STATE.equalsIgnoreCase(obEvent.getEventType()) ||
                AUTHORIZED_STATE.equalsIgnoreCase(obEvent.getEventType())) {

            log.debug("Publishing consent data for metrics.");
            HashMap<String, Object> consentData = new HashMap<>();
            consentData.put(CONSENT_ID_KEY, eventData.get(CONSENT_ID));
            consentData.put(USER_ID_KEY, eventData.get(USER_ID));
            consentData.put(CLIENT_ID_KEY, eventData.get(CLIENT_ID));
            consentData.put(STATUS_KEY, obEvent.getEventType());

            long expiryTime;
            if (AUTHORIZED_STATE.equalsIgnoreCase(obEvent.getEventType())) {
                if (consentResource != null) {
                    expiryTime = consentResource.getValidityPeriod();
                } else if (detailedConsentResource != null) {
                    expiryTime = detailedConsentResource.getValidityPeriod();
                } else {
                    expiryTime = OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond();
                }
            } else {
                expiryTime = 0;
            }
            consentData.put(EXPIRY_TIME_KEY, expiryTime);
            dataPublishingService.publishConsentData(consentData);
        }

        // Publish consent data for authorisation metrics.
        if (AUTHORIZED_STATE.equalsIgnoreCase(obEvent.getEventType()) ||
                AMENDED_STATE.equalsIgnoreCase(obEvent.getEventType()) ||
                REVOKED_STATE.equalsIgnoreCase(obEvent.getEventType()) ||
                EXPIRED_STATE.equalsIgnoreCase(obEvent.getEventType())) {

            log.debug("Publishing consent data for authorisation metrics.");
            String consentId = (String) eventData.get(CONSENT_ID);
            ConsentStatusEnum consentStatus = getConsentStatusForEventType(obEvent.getEventType());
            AuthorisationFlowTypeEnum authFlowType = getAuthFlowTypeForEventType(obEvent.getEventType());
            String requestUriKey = getRequestUriKeyFromConsentResource(consentResource, detailedConsentResource);
            if (requestUriKey != null && publishedRequestUriKeyQueue.contains(requestUriKey)) {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping authorisation data publishing for requestUriKey: " + requestUriKey +
                            " as it has already been published.");
                }
                return;
            }

            String customerProfile = null;
            String sharingDuration = null;
            if (consentResource != null) {
                customerProfile = consentResource.getConsentAttributes()
                        .get(CDSConsentExtensionConstants.CUSTOMER_PROFILE_TYPE);
                sharingDuration = consentResource.getConsentAttributes()
                        .get(CDSConsentExtensionConstants.SHARING_DURATION_VALUE);
            } else if (detailedConsentResource != null) {
                customerProfile = detailedConsentResource.getConsentAttributes()
                        .get(CDSConsentExtensionConstants.CUSTOMER_PROFILE_TYPE);
                sharingDuration = detailedConsentResource.getConsentAttributes()
                        .get(CDSConsentExtensionConstants.SHARING_DURATION_VALUE);
            }
            ConsentDurationTypeEnum consentDurationType = CDSCommonUtils.getConsentDurationType(sharingDuration);

            Map<String, Object> authorisationData = CDSCommonUtils.generateAuthorisationDataMap(consentId,
                    consentStatus, authFlowType, customerProfile, consentDurationType);

            dataPublishingService.publishAuthorisationData(authorisationData);
            addToPublishedRequestUriKeyQueue(requestUriKey);
        }

    }

    /**
     * CDS Data Holder initiated CDR Arrangement Revocation:
     * to notify the Data Recipient of the consent withdrawn by a Customer via the Data Holder’s consent dashboard.
     *
     * @param clientId     client ID
     * @param consentId    revoked sharing arrangement (consent) ID
     * @param dataHolderId ID of the Data Holder obtained from the CDR Register
     */
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    // Suppressed content - try (CloseableHttpClient httpclient = HTTPClientUtils.getHttpsClient())
    // Suppression reason - False Positive : This occurs with Java 11 when using try-with-resources and when that
    //                                       resource is being referred within the try block. This is a known issue in
    //                                       the plugin and therefore it is being suppressed.
    //                                       https://github.com/spotbugs/spotbugs/issues/1694
    protected void sendArrangementRevocationRequestToADR(String clientId, String consentId, String dataHolderId)
            throws OpenBankingException {

        String recipientBaseUri = getRecipientBaseUri(clientId);
        if (StringUtils.isBlank(recipientBaseUri)) {
            String errorMessage = "DH initiated CDR Arrangement Revocation for cdr_arrangement_id " + consentId +
                    " failed due to unavailability of recipient_base_uri. " +
                    "Please update the DR's client registration with DH with an SSA including recipient_base_uri.";
            log.error(errorMessage);
            throw new OpenBankingException(errorMessage);
        }

        String consentRevocationEndpoint = recipientBaseUri + DATA_RECIPIENT_CDR_ARRANGEMENT_REVOCATION_PATH;

        try (CloseableHttpClient httpclient = HTTPClientUtils.getHttpsClient()) {

            HttpPost httpPost = generateHttpPost(consentRevocationEndpoint, dataHolderId, recipientBaseUri, consentId);

            // POST request sent to ADR is logged here for further inspections
            log.info("DH initiated consent revocation - request: " +
                    "\n" + httpPost.getRequestLine() +
                    "\n" + (Arrays.toString(httpPost.getAllHeaders()))
                    .replaceAll("\\[|\\]", "").replaceAll(",", "\n") +
                    "\n" + EntityUtils.toString(httpPost.getEntity()));

            CloseableHttpResponse responseBody = httpclient.execute(httpPost);

            if (responseBody.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                if (log.isDebugEnabled()) {
                    log.debug("DH successfully called DR's CDR Arrangement Revocation endpoint for " +
                            "cdr_arrangement_id " + consentId + ".");
                }
            } else {
                String error = "DH initiated CDR Arrangement Revocation for cdr_arrangement_id " + consentId +
                        " returned non OK response.";
                log.error(error);
                throw new OpenBankingException(error);
            }

        } catch (IOException e) {
            log.error("Error occurred while calling DR's CDR arrangement revocation endpoint", e);
            throw new OpenBankingException("Error occurred while calling DR's CDR arrangement revocation endpoint", e);
        }
    }

    /**
     * Method to get the issued from the current time.
     *
     * @param currentTime Current time in milliseconds (unix timestamp format)
     * @return issued time in seconds (unix timestamp format)
     */
    protected static long getIatFromCurrentTime(long currentTime) {

        return currentTime / 1000;
    }

    /**
     * Method to get the expiry time when the current time is given.
     *
     * @param currentTime Current time in milliseconds (unix timestamp format)
     * @return expiry time in seconds (unix timestamp format)
     */
    protected static long getExpFromCurrentTime(long currentTime) {
        // (current time + 5 minutes) is the expiry time.
        return (currentTime / 1000) + 300;
    }

    /**
     * Method to generate signed JWT.
     *
     * @param payload payload as a string
     * @param alg     signature algorithm
     * @return JWT as a string.
     */
    protected String generateJWT(String payload, SignatureAlgorithm alg) throws OpenBankingException {

        return Jwts.builder()
                .setPayload(payload)
                .signWith(alg, CDSIdentityUtil.getJWTSigningKey())
                .compact();
    }

    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected String getRecipientBaseUri(String clientId) throws OpenBankingException {

        return new IdentityCommonHelper().getAppPropertyFromSPMetaData(clientId,
                CDSConsentExtensionConstants.RECIPIENT_BASE_URI);
    }

    /**
     * Method to generate http post request.
     *
     * @param consentRevocationEndpoint consent revocation endpoint url
     * @param dataHolderId              data holder id
     * @param recipientBaseUri          recipient base uri
     * @param consentId                 consent id
     * @return HttpPost
     */
    protected HttpPost generateHttpPost(String consentRevocationEndpoint, String dataHolderId,
                                        String recipientBaseUri, String consentId)
            throws UnsupportedEncodingException, OpenBankingException {

        HttpPost httpPost = new HttpPost(consentRevocationEndpoint);

        JSONObject jwtPayload = generateJWTPayload(dataHolderId, recipientBaseUri);

        httpPost.setHeader(HTTPConstants.CONTENT_TYPE, HTTPConstants.MEDIA_TYPE_X_WWW_FORM);
        httpPost.setHeader(HTTPConstants.HEADER_AUTHORIZATION,
                "Bearer " + generateJWT(jwtPayload.toString(), SignatureAlgorithm.PS256));
        List<NameValuePair> params = new ArrayList<>();
        String cdrArrangementJwt = getCdrArrangementJwt(jwtPayload, consentId);
        params.add(new BasicNameValuePair(CDSConsentExtensionConstants.CDR_ARRANGEMENT_JWT, cdrArrangementJwt));
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        return httpPost;
    }

    /**
     * Method to generate http post request.
     *
     * @param dataHolderId     data holder id
     * @param recipientBaseUri recipient base uri
     * @return JSONObject jwt payload
     */
    protected JSONObject generateJWTPayload(String dataHolderId, String recipientBaseUri) {

        long currentTime = System.currentTimeMillis();

        //Adding registered claims [https://tools.ietf.org/html/rfc7519]
        JSONObject jwtPayload = new JSONObject();
        jwtPayload.put(CommonConstants.ISSURE_CLAIM, dataHolderId);
        jwtPayload.put(CommonConstants.SUBJECT_CLAIM, dataHolderId);
        jwtPayload.put(CommonConstants.AUDIENCE_CLAIM, recipientBaseUri
                + DATA_RECIPIENT_CDR_ARRANGEMENT_REVOCATION_PATH);
        jwtPayload.put(CommonConstants.IAT_CLAIM, getIatFromCurrentTime(currentTime));
        jwtPayload.put(CommonConstants.EXP_CLAIM, getExpFromCurrentTime(currentTime));
        jwtPayload.put(CommonConstants.JTI_CLAIM, currentTime);

        return jwtPayload;
    }

    /**
     * Method to get signed jwt when the cdr-arrangement-id is given.
     *
     * @param cdrArrangementId - cdr-arrangement-id
     * @return signed jwt with cdr-arrangement-id
     */
    public String getCdrArrangementJwt(JSONObject jwtPayload, String cdrArrangementId)
            throws OpenBankingException {

        //Adding cdr_arrangement_id
        jwtPayload.put(CDSConsentExtensionConstants.CDR_ARRANGEMENT_ID, cdrArrangementId);
        //Add offset to jti to make jti unique from authorization header jti value.
        long jti = Long.parseLong(jwtPayload.get(CommonConstants.JTI_CLAIM).toString());
        jwtPayload.put(CommonConstants.JTI_CLAIM, Long.toString(jti + 100));
        return generateJWT(jwtPayload.toString(), SignatureAlgorithm.PS256);
    }

    private ConsentStatusEnum getConsentStatusForEventType(String eventType) {

        if (eventType.equalsIgnoreCase(REVOKED_STATE)) {
            return ConsentStatusEnum.REVOKED;
        } else if (eventType.equalsIgnoreCase(EXPIRED_STATE)) {
            return ConsentStatusEnum.EXPIRED;
        } else {
            return ConsentStatusEnum.AUTHORISED;
        }
    }

    private AuthorisationFlowTypeEnum getAuthFlowTypeForEventType(String eventType) {

        if (eventType.equalsIgnoreCase(AUTHORIZED_STATE)) {
            return AuthorisationFlowTypeEnum.CONSENT_AUTHORISATION;
        } else if (eventType.equalsIgnoreCase(AMENDED_STATE)) {
            return AuthorisationFlowTypeEnum.CONSENT_AMENDMENT_AUTHORISATION;
        } else {
            return AuthorisationFlowTypeEnum.UNCLASSIFIED;
        }
    }

    /**
     * Add the request uri key to the published data queue.
     * If the queue is full, oldest key is removed.
     * 20 keys are maintained in the queue to handle simultaneous consent state change events.
     *
     * @param requestUriKey request uri key coming as a consent attribute
     */
    private void addToPublishedRequestUriKeyQueue(String requestUriKey) {

        if (StringUtils.isBlank(requestUriKey)) {
            return;
        }
        if (publishedRequestUriKeyQueue.size() >= 20) {
            publishedRequestUriKeyQueue.pollFirst();
        }
        publishedRequestUriKeyQueue.addLast(requestUriKey);
    }

    private String getRequestUriKeyFromConsentResource(ConsentResource consentResource,
                                                       DetailedConsentResource detailedConsentResource) {

        Map<String, String> consentAttributes = null;
        if (consentResource != null) {
            consentAttributes = consentResource.getConsentAttributes();
        } else if (detailedConsentResource != null) {
            consentAttributes = detailedConsentResource.getConsentAttributes();
        }
        return consentAttributes != null ? consentAttributes.get(REQUEST_URI_KEY) : null;
    }
}
