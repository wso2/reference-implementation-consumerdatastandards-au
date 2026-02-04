package org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.Valid;

/**
 * Generic display-related data required by the authorization UI.
 */
@ApiModel(description = "Generic display-related data required by the authorization UI.")
@JsonTypeName("SuccessResponsePopulateConsentAuthorizeScreenData_displayData")
@javax.annotation.Generated(
        value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-07-17T14:09:27.461176800+05:30[Asia/Colombo]",
        comments = "Generator version: 7.12.0"
)
public class SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData {

  /**
   * UI display data list.
   *
   * Example:
   * [
   *   { "accountId": "6500001232", "displayName": "joint_account_1" }
   * ]
   */
  private List<@Valid Map<String, Object>> displayData = new ArrayList<>();

  public SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData() {
  }

  public List<Map<String, Object>> getDisplayData() {
    return displayData;
  }

  public void setDisplayData(List<Map<String, Object>> displayData) {
    this.displayData = displayData;
  }

  public SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData
  addDisplayDataItem(Map<String, Object> item) {
    this.displayData.add(item);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData that =
            (SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData) o;
    return Objects.equals(displayData, that.displayData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(displayData);
  }

  @Override
  public String toString() {
    return "class SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData {\n" +
            "    displayData: " + displayData + "\n" +
            "}";
  }
}
