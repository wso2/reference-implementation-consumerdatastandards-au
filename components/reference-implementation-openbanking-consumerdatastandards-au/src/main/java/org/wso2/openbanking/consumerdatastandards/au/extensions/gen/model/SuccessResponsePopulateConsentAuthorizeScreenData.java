package org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("SuccessResponsePopulateConsentAuthorizeScreenData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-02-20T15:47:46.918170600+05:30[Asia/Colombo]", comments = "Generator version: 7.20.0")
public class SuccessResponsePopulateConsentAuthorizeScreenData   {
  private SuccessResponsePopulateConsentAuthorizeScreenDataConsentData consentData;
  private SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData;
  private @Valid List<@Valid AdditionalDisplayDataSection> additionalDisplayData = new ArrayList<>();

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
   * Additional display data sections for the authorization screen
   **/
  public SuccessResponsePopulateConsentAuthorizeScreenData additionalDisplayData(List<@Valid AdditionalDisplayDataSection> additionalDisplayData) {
    this.additionalDisplayData = additionalDisplayData;
    return this;
  }


  @ApiModelProperty(value = "Additional display data sections for the authorization screen")
  @JsonProperty("additionalDisplayData")
  @Valid public List<@Valid AdditionalDisplayDataSection> getAdditionalDisplayData() {
    return additionalDisplayData;
  }

  @JsonProperty("additionalDisplayData")
  public void setAdditionalDisplayData(List<@Valid AdditionalDisplayDataSection> additionalDisplayData) {
    this.additionalDisplayData = additionalDisplayData;
  }

  public SuccessResponsePopulateConsentAuthorizeScreenData addAdditionalDisplayDataItem(AdditionalDisplayDataSection additionalDisplayDataItem) {
    if (this.additionalDisplayData == null) {
      this.additionalDisplayData = new ArrayList<>();
    }

    this.additionalDisplayData.add(additionalDisplayDataItem);
    return this;
  }

  public SuccessResponsePopulateConsentAuthorizeScreenData removeAdditionalDisplayDataItem(AdditionalDisplayDataSection additionalDisplayDataItem) {
    if (additionalDisplayDataItem != null && this.additionalDisplayData != null) {
      this.additionalDisplayData.remove(additionalDisplayDataItem);
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
            Objects.equals(this.additionalDisplayData, successResponsePopulateConsentAuthorizeScreenData.additionalDisplayData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentData, consumerData, additionalDisplayData);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponsePopulateConsentAuthorizeScreenData {\n");

    sb.append("    consentData: ").append(toIndentedString(consentData)).append("\n");
    sb.append("    consumerData: ").append(toIndentedString(consumerData)).append("\n");
    sb.append("    additionalDisplayData: ").append(toIndentedString(additionalDisplayData)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

