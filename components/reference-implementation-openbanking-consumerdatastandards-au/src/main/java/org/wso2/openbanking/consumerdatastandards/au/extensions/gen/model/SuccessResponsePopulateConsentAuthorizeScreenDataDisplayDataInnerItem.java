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
@JsonTypeName("SuccessResponsePopulateConsentAuthorizeScreenData_displayData_innerItem")
@javax.annotation.Generated(
        value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen",
        date = "2025-07-17T14:09:27.461176800+05:30[Asia/Colombo]",
        comments = "Generator version: 7.12.0"
)
public class SuccessResponsePopulateConsentAuthorizeScreenDataDisplayDataInnerItem {

  /**
   * Main heading shown in the UI.
   */
  private String heading;

  /**
   * Sub heading shown under the main heading.
   */
  private String subHeading;

  /**
   * Tooltip/help description near sub heading shown in the UI.
   */
  private String description;

  /**
   * UI display data list.
   */
  private List<@Valid Map<String, Object>> displayList = new ArrayList<>();

  public SuccessResponsePopulateConsentAuthorizeScreenDataDisplayDataInnerItem() {}

  // Heading
  public String getHeading() {
    return heading;
  }

  public void setHeading(String heading) {
    this.heading = heading;
  }

  // SubHeading
  public String getSubHeading() {
    return subHeading;
  }

  public void setSubHeading(String subHeading) {
    this.subHeading = subHeading;
  }

  // Tooltip Description
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  // Display Data
  public List<Map<String, Object>> getDisplayList() {
    return displayList;
  }

  public void setDisplayList(List<Map<String, Object>> displayData) {
    this.displayList = displayData;
  }

  public SuccessResponsePopulateConsentAuthorizeScreenDataDisplayDataInnerItem
  addDisplayListItem(Map<String, Object> item) {
    this.displayList.add(item);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SuccessResponsePopulateConsentAuthorizeScreenDataDisplayDataInnerItem)) return false;
    SuccessResponsePopulateConsentAuthorizeScreenDataDisplayDataInnerItem that =
            (SuccessResponsePopulateConsentAuthorizeScreenDataDisplayDataInnerItem) o;
    return Objects.equals(heading, that.heading) &&
            Objects.equals(subHeading, that.subHeading) &&
            Objects.equals(description, that.description) &&
            Objects.equals(displayList, that.displayList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(heading, subHeading, description, displayList);
  }

  @Override
  public String toString() {
    return "class SuccessResponsePopulateConsentAuthorizeScreenDataDisplayDataInnerItem {\n" +
            "    heading: " + heading + "\n" +
            "    subHeading: " + subHeading + "\n" +
            "    description: " + description + "\n" +
            "    displayData: " + displayList + "\n" +
            "}";
  }
}
