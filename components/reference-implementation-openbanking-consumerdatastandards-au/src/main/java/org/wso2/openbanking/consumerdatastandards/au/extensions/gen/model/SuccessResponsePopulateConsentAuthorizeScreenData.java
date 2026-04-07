package org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("SuccessResponsePopulateConsentAuthorizeScreenData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-07T10:52:53.468738900+05:30[Asia/Colombo]", comments = "Generator version: 7.21.0")
public class SuccessResponsePopulateConsentAuthorizeScreenData   {
  private SuccessResponsePopulateConsentAuthorizeScreenDataConsentData consentData;
  private SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData;
  private @Valid List<@Valid AdditionalData> additionalData = new ArrayList<>();

  public SuccessResponsePopulateConsentAuthorizeScreenData() {
  }

  /**
   **/
  public SuccessResponsePopulateConsentAuthorizeScreenData consentData(SuccessResponsePopulateConsentAuthorizeScreenDataConsentData consentData) {
    this.consentData = consentData;
    return this;
  }


  @ApiModelProperty(value = "")
  @JsonProperty("consentData")
  @Valid public SuccessResponsePopulateConsentAuthorizeScreenDataConsentData getConsentData() {
    return consentData;
  }

  @JsonProperty("consentData")
  public void setConsentData(SuccessResponsePopulateConsentAuthorizeScreenDataConsentData consentData) {
    this.consentData = consentData;
  }

  /**
   **/
  public SuccessResponsePopulateConsentAuthorizeScreenData consumerData(SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData) {
    this.consumerData = consumerData;
    return this;
  }


  @ApiModelProperty(value = "")
  @JsonProperty("consumerData")
  @Valid public SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData getConsumerData() {
    return consumerData;
  }

  @JsonProperty("consumerData")
  public void setConsumerData(SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData) {
    this.consumerData = consumerData;
  }

  /**
   * Additional data for the authorization screen
   **/
  public SuccessResponsePopulateConsentAuthorizeScreenData additionalData(List<@Valid AdditionalData> additionalData) {
    this.additionalData = additionalData;
    return this;
  }


  @ApiModelProperty(value = "Additional data for the authorization screen")
  @JsonProperty("additionalData")
  @Valid public List<@Valid AdditionalData> getAdditionalData() {
    return additionalData;
  }

  @JsonProperty("additionalData")
  public void setAdditionalData(List<@Valid AdditionalData> additionalData) {
    this.additionalData = additionalData;
  }

  public SuccessResponsePopulateConsentAuthorizeScreenData addAdditionalDataItem(AdditionalData additionalDataItem) {
    if (this.additionalData == null) {
      this.additionalData = new ArrayList<>();
    }

    this.additionalData.add(additionalDataItem);
    return this;
  }

  public SuccessResponsePopulateConsentAuthorizeScreenData removeAdditionalDataItem(AdditionalData additionalDataItem) {
    if (additionalDataItem != null && this.additionalData != null) {
      this.additionalData.remove(additionalDataItem);
    }

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuccessResponsePopulateConsentAuthorizeScreenData successResponsePopulateConsentAuthorizeScreenData = (SuccessResponsePopulateConsentAuthorizeScreenData) o;
    return Objects.equals(this.consentData, successResponsePopulateConsentAuthorizeScreenData.consentData) &&
            Objects.equals(this.consumerData, successResponsePopulateConsentAuthorizeScreenData.consumerData) &&
            Objects.equals(this.additionalData, successResponsePopulateConsentAuthorizeScreenData.additionalData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentData, consumerData, additionalData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponsePopulateConsentAuthorizeScreenData {\n");

    sb.append("    consentData: ").append(toIndentedString(consentData)).append("\n");
    sb.append("    consumerData: ").append(toIndentedString(consumerData)).append("\n");
    sb.append("    additionalData: ").append(toIndentedString(additionalData)).append("\n");
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
