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
package org.wso2.openbanking.cds.identity.dcr.model;

import com.google.gson.annotations.SerializedName;
import com.wso2.openbanking.accelerator.identity.dcr.model.SoftwareStatementBody;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DCRCommonConstants;
import com.wso2.openbanking.accelerator.identity.dcr.validation.validationgroups.AttributeChecks;
import com.wso2.openbanking.accelerator.identity.dcr.validation.validationgroups.MandatoryChecks;
import org.wso2.openbanking.cds.identity.dcr.validation.annotation.ValidateSSACallbackUris;
import org.wso2.openbanking.cds.identity.dcr.validation.annotation.ValidateSSAIssuer;
import org.wso2.openbanking.cds.identity.dcr.validation.annotation.ValidateSSAScopes;
import org.wso2.openbanking.cds.identity.dcr.validation.annotation.ValidateSSASectorIdentifierUri;
import org.wso2.openbanking.cds.identity.dcr.validation.annotation.ValidateSSASoftwareRoles;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;


/**
 * CDSSoftwareStatementBody class.
 */
@ValidateSSASectorIdentifierUri(message = "redirect_uris do not match with " +
        "the elements in sector identifier uri:" + DCRCommonConstants.INVALID_META_DATA, groups = AttributeChecks.class)
public class CDSSoftwareStatementBody extends SoftwareStatementBody {

    @SerializedName("logo_uri")
    private String logoUri;

    @SerializedName("legal_entity_id")
    private String legalEntityId;

    @SerializedName("legal_entity_name")
    private String legalEntityName;

    @SerializedName("client_description")
    private String clientDescription;

    @SerializedName("client_uri")
    private String clientUri;

    @SerializedName("sector_identifier_uri")
    private String sectorIdentifierUri;

    @SerializedName("tos_uri")
    private String tosUri;

    @SerializedName("policy_uri")
    private String policyUri;

    @SerializedName("revocation_uri")
    private String revocationUri;

    @SerializedName("recipient_base_uri")
    private String recipientBaseUri;

    @SerializedName("software_roles")
    private String softwareRoles;

    public String getLogoUri() {

        return logoUri;
    }

    public void setLogoUri(String logoUri) {

        this.logoUri = logoUri;
    }

    public String getLegalEntityId() {

        return legalEntityId;
    }

    public void setLegalEntityId(String legalEntityId) {

        this.legalEntityId = legalEntityId;
    }

    public String getLegalEntityName() {

        return legalEntityName;
    }

    public void setLegalEntityName(String legalEntityName) {

        this.legalEntityName = legalEntityName;
    }

    public String getClientDescription() {

        return clientDescription;
    }

    public void setClientDescription(String clientDescription) {

        this.clientDescription = clientDescription;
    }

    public String getClientUri() {

        return clientUri;
    }

    public void setClientUri(String clientUri) {

        this.clientUri = clientUri;
    }

    public String getSectorIdentifierUri() {

        return sectorIdentifierUri;
    }

    public void setSectorIdentifierUri(String sectorIdentifierUri) {

        this.sectorIdentifierUri = sectorIdentifierUri;
    }

    public String getTosUri() {

        return tosUri;
    }

    public void setTosUri(String tosUri) {

        this.tosUri = tosUri;
    }

    public String getPolicyUri() {

        return policyUri;
    }

    public void setPolicyUri(String policyUri) {

        this.policyUri = policyUri;
    }

    public String getJwksRevocationUri() {

        return revocationUri;
    }

    public void setJwksRevocationUri(String revocationUri) {

        this.revocationUri = revocationUri;
    }

    public String getRecipientBaseUri() {

        return recipientBaseUri;
    }

    public void setRecipientBaseUri(String recipientBaseUri) {

        this.recipientBaseUri = recipientBaseUri;
    }

    @ValidateSSASoftwareRoles(message = "Invalid Software Roles in software statement:" +
            DCRCommonConstants.INVALID_META_DATA, groups = AttributeChecks.class)
    @NotBlank(message = "Software Roles can not be null or empty in SSA:" + DCRCommonConstants.INVALID_META_DATA,
            groups = MandatoryChecks.class)
    public String getSoftwareRoles() {

        return softwareRoles;
    }

    public void setSoftwareRoles(String softwareRoles) {

        this.softwareRoles = softwareRoles;
    }

    @Override
    @ValidateSSAIssuer(message = "Invalid Issuer in software statement:" + DCRCommonConstants.INVALID_META_DATA,
            groups = AttributeChecks.class)
    @NotBlank(message = "Issuer can not be null or empty in SSA:" + DCRCommonConstants.INVALID_META_DATA,
            groups = MandatoryChecks.class)
    public String getSsaIssuer() {

        return super.getSsaIssuer();
    }

    @Override
    @ValidateSSACallbackUris(message = "Redirect URIs do not contain the same hostname:" +
            DCRCommonConstants.INVALID_META_DATA, groups = AttributeChecks.class)
    @NotEmpty(message = "Redirect URIs can not be null or empty in SSA:" + DCRCommonConstants.INVALID_META_DATA,
            groups = MandatoryChecks.class)
    public List<String> getCallbackUris() {

        return super.getCallbackUris();
    }

    @Override
    @ValidateSSAScopes(message = "Mandatory scopes are not given in the software statement:" +
            DCRCommonConstants.INVALID_META_DATA, groups = AttributeChecks.class)
    @NotBlank(message = "Scopes can not be null or empty in SSA:" + DCRCommonConstants.INVALID_META_DATA,
            groups = MandatoryChecks.class)
    public String getScopes() {

        return super.getScopes();
    }
}
