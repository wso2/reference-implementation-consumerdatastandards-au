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
package org.wso2.openbanking.cds.identity.dcr.validation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.wso2.openbanking.accelerator.identity.dcr.exception.DCRValidationException;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DefaultRegistrationValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.wso2.openbanking.cds.identity.dcr.model.CDSRegistrationRequest;
import org.wso2.openbanking.cds.identity.dcr.model.CDSRegistrationResponse;
import org.wso2.openbanking.cds.identity.dcr.model.CDSSoftwareStatementBody;
import org.wso2.openbanking.cds.identity.dcr.utils.ValidationUtils;

import java.util.Map;

/**
 * CDS specific registration validator class.
 */
public class CDSRegistrationValidatorImpl extends DefaultRegistrationValidatorImpl {

    @Override
    public void validatePost(RegistrationRequest registrationRequest) throws DCRValidationException {

        CDSRegistrationRequest cdsRegistrationRequest = new CDSRegistrationRequest(registrationRequest);
        ValidationUtils.validateRequest(cdsRegistrationRequest);
    }

    @Override
    public void validateUpdate(RegistrationRequest registrationRequest) throws DCRValidationException {

        CDSRegistrationRequest cdsRegistrationRequest = new CDSRegistrationRequest(registrationRequest);
        ValidationUtils.validateRequest(cdsRegistrationRequest);
    }

    @Override
    public void setSoftwareStatementPayload(RegistrationRequest registrationRequest, String decodedSSA) {

        CDSSoftwareStatementBody cdsSoftwareStatementBody = new GsonBuilder().create()
                .fromJson(decodedSSA, CDSSoftwareStatementBody.class);
        registrationRequest.setSoftwareStatementBody(cdsSoftwareStatementBody);
    }

    @Override
    public String getRegistrationResponse(Map<String, Object> spMetaData) {

        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(spMetaData);
        CDSRegistrationResponse cdsRegistrationResponse = gson.fromJson(jsonElement, CDSRegistrationResponse.class);
        if (StringUtils.isBlank(cdsRegistrationResponse.getRequestObjectSigningAlg())) {
            cdsRegistrationResponse.setRequestObjectSigningAlg("PS256");
        }
        return gson.toJson(cdsRegistrationResponse);
    }
}
