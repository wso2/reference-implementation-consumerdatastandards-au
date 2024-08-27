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

package org.wso2.openbanking.cds.arrangement.revocation.endpoint;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import com.wso2.openbanking.accelerator.runtime.identity.authn.filter.OBOAuthClientAuthenticatorProxy;
import org.wso2.openbanking.cds.arrangement.revocation.constants.Constants;
import org.wso2.openbanking.cds.common.config.OpenBankingCDSConfigParser;
import org.wso2.openbanking.cds.common.error.handling.models.CDSErrorMeta;
import org.wso2.openbanking.cds.common.error.handling.util.ErrorConstants;
import org.wso2.openbanking.cds.common.error.handling.util.ErrorUtil;
import org.wso2.openbanking.cds.common.metadata.domain.MetadataValidationResponse;
import org.wso2.openbanking.cds.common.metadata.status.validator.service.MetadataService;
import io.swagger.annotations.Api;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.InInterceptors;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * CDR Arrangement Revocation Endpoint used by data recipients to revoke an existing sharing arrangement.
 * Endpoint is secured with MTLS
 * Private key jwt mechanism is used to authenticate the client
 */
@Path("/arrangements")
@InInterceptors(classes = OBOAuthClientAuthenticatorProxy.class)
@Api(value = "/")
public class ArrangementRevocationApi {

    private static final Log LOG = LogFactory.getLog(ArrangementRevocationApi.class);
    private static final ConsentCoreServiceImpl consentCoreService = new ConsentCoreServiceImpl();

    @POST
    @Path("/revoke")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json"})
    public Response revokeArrangement(@Context HttpServletRequest request,
                                      MultivaluedMap<String, String> paramMap) {

        OAuthClientAuthnContext clientAuthnContext = (OAuthClientAuthnContext)
                request.getAttribute(Constants.CLIENT_AUTHENTICATION_CONTEXT);

        // Check if the client authentication is successful
        if (!clientAuthnContext.isAuthenticated()) {
            return handleOAuthErrorResponse("invalid_client", clientAuthnContext.getErrorMessage(), 401);
        }
        List<String> objList = paramMap.get(Constants.CDR_ARRANGEMENT_ID);

        if (objList == null) {
            return handleErrorResponse(ErrorConstants.AUErrorEnum.FIELD_MISSING, "cdr_arrangement_id " +
                    "not present in the request", HttpServletResponse.SC_BAD_REQUEST);
        }

        // Validate client status from metadata service
        if (OpenBankingCDSConfigParser.getInstance().isMetadataCacheEnabled()) {
            MetadataValidationResponse metadataValidationResponse = MetadataService.
                    shouldFacilitateConsentWithdrawal(clientAuthnContext.getClientId());
            if (!metadataValidationResponse.isValid()) {
                return handleErrorResponse(ErrorConstants.AUErrorEnum.INVALID_PRODUCT_STATUS,
                        metadataValidationResponse.getErrorMessage(), HttpServletResponse.SC_FORBIDDEN);
            }
        }

        String cdrArrangementId = objList.get(0);
        String clientId = clientAuthnContext.getClientId();
        String userId;
        DetailedConsentResource consent = null;

        try {
            consent = consentCoreService.getDetailedConsent(cdrArrangementId);
        } catch (ConsentManagementException e) {
            LOG.error(e);
        }

        // Return error if consent is not available in the database
        if (consent == null) {
            return handleErrorResponse(ErrorConstants.AUErrorEnum.INVALID_ARRANGEMENT,
                    "invalid cdr-arrangement-id sent in the request", 422);
        }

        try {
            userId = getUserIdForConsent(consent);
        } catch (ConsentManagementException e) {
            LOG.error("Error occurred while retrieving user id for the consent", e);
            return handleErrorResponse(ErrorConstants.AUErrorEnum.UNEXPECTED_ERROR,
                    "Server error while revoking the consent",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // Return error if consent is not in the authorized status
        if (!(Constants.AUTHORIZED_CONSENT_STATUS.equals(consent.getCurrentStatus()))) {
            return handleErrorResponse(ErrorConstants.AUErrorEnum.INVALID_ARRANGEMENT,
                    "cdr arrangement is not in authorised state",
                    422);
        }

        // Return error if the authenticated client is not bound to the consent
        if (!clientId.equals(consent.getClientID())) {
            return handleErrorResponse(ErrorConstants.AUErrorEnum.RESOURCE_FORBIDDEN,
                    "Unauthorized access to the cdr arrangement",
                    HttpServletResponse.SC_FORBIDDEN);
        }

        // Revoke the consent and tokens
        try {
            consentCoreService.revokeConsentWithReason(cdrArrangementId, Constants.REVOKED_CONSENT_STATUS, userId,
                    Constants.CONSENT_REVOKE_REASON);
        } catch (ConsentManagementException e) {
            LOG.error("Error occurred while revoking the consent", e);
            return handleErrorResponse(ErrorConstants.AUErrorEnum.UNEXPECTED_ERROR,
                    "Server error while revoking the consent",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        // Respond with 204 if successful
        return Response.noContent().build();
    }

    /**
     * Return CDS specific error response.
     *
     * @param errorCode    - CDS error code
     * @param errorMessage - Error message
     * @param httpCode     - Http code
     * @return - Response
     */
    private Response handleErrorResponse(ErrorConstants.AUErrorEnum errorCode, String errorMessage, int httpCode) {

        JSONArray errorList = new JSONArray();
        errorList.add(ErrorUtil.getErrorObject(errorCode, errorMessage, new CDSErrorMeta()));
        Response.ResponseBuilder respBuilder = Response.status(httpCode);
        return respBuilder.entity(ErrorUtil.getErrorJson(errorList)).build();
    }

    /**
     * Return CDS specific error response.
     *
     * @param errorCode    - CDS error code
     * @param errorMessage - CDS error message
     * @param httpCode     - Http code
     * @return - Response
     */
    private Response handleOAuthErrorResponse(String errorCode, String errorMessage, int httpCode) {

        JSONObject errorObj = new JSONObject();
        errorObj.put(ErrorConstants.ERROR, errorCode);
        errorObj.put(ErrorConstants.ERROR_DESCRIPTION, errorMessage);
        Response.ResponseBuilder respBuilder = Response.status(httpCode);
        return respBuilder.entity(errorObj.toString()).build();
    }

    /**
     * Extract user-id from DetailedConsentResource.
     *
     * @param detailedConsentResource - DetailedConsentResource
     * @return - User-Id
     * @throws ConsentManagementException - ConsentManagementException
     */
    private String getUserIdForConsent(DetailedConsentResource detailedConsentResource)
            throws ConsentManagementException {
        // Extract userId from authorizationResources
        ArrayList<AuthorizationResource> authorizationResources = detailedConsentResource
                .getAuthorizationResources();

        String consentUserID = StringUtils.EMPTY;
        if (authorizationResources != null && !authorizationResources.isEmpty()) {
            for (AuthorizationResource authorizationResource : detailedConsentResource.getAuthorizationResources()) {
                if (Constants.AUTH_RESOURCE_TYPE_PRIMARY.equals(authorizationResource.getAuthorizationType())) {
                    consentUserID = authorizationResource.getUserID();
                    break;
                }
            }
        }

        if (StringUtils.isBlank(consentUserID)) {
            LOG.error("User ID is required for token revocation, cannot proceed");
            throw new ConsentManagementException("User ID is required for token revocation, cannot " +
                    "proceed");
        }
        return consentUserID;
    }
}
