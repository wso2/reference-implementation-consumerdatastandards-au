package org.wso2.openbanking.consumerdatastandards.account.metadata.model;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("DisclosureOptionItem")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-03-30T12:21:39.036572300+05:30[Asia/Colombo]", comments = "Generator version: 7.21.0")
public class DisclosureOptionItem   {
    private String accountId;
    private String disclosureOption;

    public DisclosureOptionItem() {
    }

    @JsonCreator
    public DisclosureOptionItem(
            @JsonProperty(required = true, value = "accountId") String accountId,
            @JsonProperty(required = true, value = "disclosureOption") String disclosureOption
    ) {
        this.accountId = accountId;
        this.disclosureOption = disclosureOption;
    }

    /**
     * Account ID
     **/
    public DisclosureOptionItem accountId(String accountId) {
        this.accountId = accountId;
        return this;
    }


    @ApiModelProperty(required = true, value = "Account ID")
    @JsonProperty(required = true, value = "accountId")
    @NotNull public String getAccountId() {
        return accountId;
    }

    @JsonProperty(required = true, value = "accountId")
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * Disclosure option status
     **/
    public DisclosureOptionItem disclosureOption(String disclosureOption) {
        this.disclosureOption = disclosureOption;
        return this;
    }


    @ApiModelProperty(example = "no-sharing", required = true, value = "Disclosure option status")
    @JsonProperty(required = true, value = "disclosureOption")
    @NotNull public String getDisclosureOption() {
        return disclosureOption;
    }

    @JsonProperty(required = true, value = "disclosureOption")
    public void setDisclosureOption(String disclosureOption) {
        this.disclosureOption = disclosureOption;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DisclosureOptionItem disclosureOptionItem = (DisclosureOptionItem) o;
        return Objects.equals(this.accountId, disclosureOptionItem.accountId) &&
                Objects.equals(this.disclosureOption, disclosureOptionItem.disclosureOption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, disclosureOption);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DisclosureOptionItem {\n");

        sb.append("    accountId: ").append(toIndentedString(accountId)).append("\n");
        sb.append("    disclosureOption: ").append(toIndentedString(disclosureOption)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }


}
