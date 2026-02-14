package org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
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

  @ApiModelProperty(value = "Display data sections for the authorization UI")
  @JsonProperty("items")
  private List<@Valid SuccessResponsePopulateConsentAuthorizeScreenDataDisplayDataInnerItem> items =
          new ArrayList<>();

  public SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData() {}

  public List<SuccessResponsePopulateConsentAuthorizeScreenDataDisplayDataInnerItem> getItems() {
    return items;
  }

  public void setItems(
          List<SuccessResponsePopulateConsentAuthorizeScreenDataDisplayDataInnerItem> items) {
    this.items = items;
  }

  public SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData addItem(
          SuccessResponsePopulateConsentAuthorizeScreenDataDisplayDataInnerItem item) {
    this.items.add(item);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData)) return false;
        SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData that =
          (SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData) o;
        return Objects.equals(items, that.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items);
  }

  @Override
  public String toString() {
        return "class SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData {\n" +
          "    items: " + items + "\n" +
          "}";
  }
}
