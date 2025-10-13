package org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("SuccessResponsePopulateConsentAuthorizeScreenData_consumerData_accounts_inner")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-09-19T15:45:23.929498+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner   {
  private Boolean selected;
  private String displayName;
  private Map<String, Object> additionalProperties = new HashMap<>();

  public SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner() {
  }

  @JsonCreator
  public SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner(
          @JsonProperty(required = true, value = "displayName") String displayName
  ) {
    this.displayName = displayName;
  }

  /**
   * Whether the account is selected by default
   **/
  public SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner selected(Boolean selected) {
    this.selected = selected;
    return this;
  }


  @ApiModelProperty(value = "Whether the account is selected by default")
  @JsonProperty("selected")
  public Boolean getSelected() {
    return selected;
  }

  @JsonProperty("selected")
  public void setSelected(Boolean selected) {
    this.selected = selected;
  }

  /**
   * Account display name
   **/
  public SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }


  @ApiModelProperty(required = true, value = "Account display name")
  @JsonProperty(required = true, value = "displayName")
  @NotNull public String getDisplayName() {
    return displayName;
  }

  @JsonProperty(required = true, value = "displayName")
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Get any additional properties that are not explicitly defined in the OpenAPI spec
   * @return a map of additional properties
   */
  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  /**
   * Set any additional property that is not explicitly defined in the OpenAPI spec
   * @param name The key of the additional property
   * @param value The value of the additional property
   */
  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner successResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner = (SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner) o;
    return Objects.equals(this.selected, successResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner.selected) &&
            Objects.equals(this.displayName, successResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner.displayName) &&
            Objects.equals(this.additionalProperties, successResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner.additionalProperties); // Include additional properties in equals
  }

  @Override
  public int hashCode() {
    return Objects.hash(selected, displayName, additionalProperties); // Include additional properties in hashCode
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuccessResponsePopulateConsentAuthorizeScreenDataConsumerDataAccountsInner {\n");

    sb.append("    selected: ").append(toIndentedString(selected)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n"); // Include additional properties in toString
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
