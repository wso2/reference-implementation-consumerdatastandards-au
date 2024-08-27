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

package org.wso2.openbanking.cds.consent.extensions.validate.utils;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.cds.consent.extensions.common.CDSConsentExtensionConstants;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Consent validate util class for CDS specification.
 */
public class CDSConsentValidatorUtil {

    private static final Log log = LogFactory.getLog(CDSConsentValidatorUtil.class);

    /**
     * Validate whether consent is expired.
     *
     * @param expDateVal
     * @return
     * @throws ConsentException
     */
    public static Boolean isConsentExpired(String expDateVal) throws ConsentException {

        if (StringUtils.isNotBlank(expDateVal) && !CDSConsentExtensionConstants.ZERO.equalsIgnoreCase(expDateVal)) {
            try {
                OffsetDateTime expDate = OffsetDateTime.parse(expDateVal);
                return OffsetDateTime.now().isAfter(expDate);
            } catch (DateTimeParseException e) {
                log.error("Error occurred while parsing the expiration date" + " : " + expDateVal, e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Error occurred while parsing the expiration date");
            }
        } else {
            return false;
        }

    }

    /**
     * Method to validate account ids in post request body.
     *
     * @param consentValidateData
     * @return
     */
    public static String validAccountIdsInPostRequest(ConsentValidateData consentValidateData) {

        List<String> requestedAccountsList = new ArrayList<>();
        for (Object element : (ArrayList) ((JSONObject) consentValidateData.getPayload()
                .get(CDSConsentExtensionConstants.DATA)).get(CDSConsentExtensionConstants.ACCOUNT_IDS)) {
            if (element != null) {
                requestedAccountsList.add(element.toString());
            } else {
                return null;
            }
        }
        if (!requestedAccountsList.isEmpty()) {
            List<String> consentedAccountsList = new ArrayList<>();

            for (ConsentMappingResource resource : consentValidateData.getComprehensiveConsent()
                    .getConsentMappingResources()) {
                consentedAccountsList.add(resource.getAccountID());
            }
            for (String requestedAccount : requestedAccountsList) {
                if (!consentedAccountsList.contains(requestedAccount)) {
                    return requestedAccount;
                }
            }
            return "SUCCESS";
        }
        return null;
    }

    /**
     * Method to validate whether account id is valid.
     *
     * @param consentValidateData
     * @return
     */
    public static Boolean isAccountIdValid(ConsentValidateData consentValidateData) {

        if (!consentValidateData.getRequestPath().contains("{accountId}")) {
            return true;
        }
        String resourcePath = consentValidateData.getResourceParams().get("ResourcePath");
        ArrayList<String> resourceArrayList = new ArrayList<>(Arrays.asList(resourcePath.split("/")));

        for (ConsentMappingResource resource : consentValidateData.getComprehensiveConsent()
                .getConsentMappingResources()) {
            if (resourceArrayList.contains(resource.getAccountID())) {
                return true;
            }
        }
        return false;
    }
}
