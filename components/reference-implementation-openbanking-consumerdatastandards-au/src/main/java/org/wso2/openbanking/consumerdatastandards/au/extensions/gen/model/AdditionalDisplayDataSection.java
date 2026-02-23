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
 * A section of additional display data with heading, subheading, description and display list
 **/
@ApiModel(description = "A section of additional display data with heading, subheading, description and display list")
@JsonTypeName("AdditionalDisplayDataSection")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-02-20T15:47:46.918170600+05:30[Asia/Colombo]", comments = "Generator version: 7.20.0")
public class AdditionalDisplayDataSection   {
  private String heading;
  private String subHeading;
  private String description;
  private @Valid List<@Valid DisplayListItem> displayList = new ArrayList<>();

  public AdditionalDisplayDataSection() {
  }

  /**
   * Section heading
   **/
  public AdditionalDisplayDataSection heading(String heading) {
    this.heading = heading;
    return this;
  }


  @ApiModelProperty(value = "Section heading")
  @JsonProperty("heading")
  public String getHeading() {
    return heading;
  }

  @JsonProperty("heading")
  public void setHeading(String heading) {
    this.heading = heading;
  }

  /**
   * Section sub-heading
   **/
  public AdditionalDisplayDataSection subHeading(String subHeading) {
    this.subHeading = subHeading;
    return this;
  }


  @ApiModelProperty(value = "Section sub-heading")
  @JsonProperty("subHeading")
  public String getSubHeading() {
    return subHeading;
  }

  @JsonProperty("subHeading")
  public void setSubHeading(String subHeading) {
    this.subHeading = subHeading;
  }

  /**
   * Descriptive text displayed in a tooltip adjacent to the sub-heading
   **/
  public AdditionalDisplayDataSection description(String description) {
    this.description = description;
    return this;
  }


  @ApiModelProperty(value = "Descriptive text displayed in a tooltip adjacent to the sub-heading")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * List of display items
   **/
  public AdditionalDisplayDataSection displayList(List<@Valid DisplayListItem> displayList) {
    this.displayList = displayList;
    return this;
  }


  @ApiModelProperty(value = "List of display items")
  @JsonProperty("displayList")
  @Valid public List<@Valid DisplayListItem> getDisplayList() {
    return displayList;
  }

  @JsonProperty("displayList")
  public void setDisplayList(List<@Valid DisplayListItem> displayList) {
    this.displayList = displayList;
  }

  public AdditionalDisplayDataSection addDisplayListItem(DisplayListItem displayListItem) {
    if (this.displayList == null) {
      this.displayList = new ArrayList<>();
    }

    this.displayList.add(displayListItem);
    return this;
  }

  public AdditionalDisplayDataSection removeDisplayListItem(DisplayListItem displayListItem) {
    if (displayListItem != null && this.displayList != null) {
      this.displayList.remove(displayListItem);
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
    AdditionalDisplayDataSection additionalDisplayDataSection = (AdditionalDisplayDataSection) o;
    return Objects.equals(this.heading, additionalDisplayDataSection.heading) &&
            Objects.equals(this.subHeading, additionalDisplayDataSection.subHeading) &&
            Objects.equals(this.description, additionalDisplayDataSection.description) &&
            Objects.equals(this.displayList, additionalDisplayDataSection.displayList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(heading, subHeading, description, displayList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdditionalDisplayDataSection {\n");

    sb.append("    heading: ").append(toIndentedString(heading)).append("\n");
    sb.append("    subHeading: ").append(toIndentedString(subHeading)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    displayList: ").append(toIndentedString(displayList)).append("\n");
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
