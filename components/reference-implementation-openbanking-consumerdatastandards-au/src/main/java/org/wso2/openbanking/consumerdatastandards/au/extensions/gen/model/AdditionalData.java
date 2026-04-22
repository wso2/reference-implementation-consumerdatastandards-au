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
 * Additional data with title, subtitle, description and items
 **/
@ApiModel(description = "Additional data with title, subtitle, description and items")
@JsonTypeName("AdditionalData")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-07T10:52:53.468738900+05:30[Asia/Colombo]", comments = "Generator version: 7.21.0")
public class AdditionalData   {
  private String title;
  private String subtitle;
  private String description;
  private @Valid List<@Valid AdditionalDataItem> items = new ArrayList<>();

  public AdditionalData() {
  }

  /**
   * Title
   **/
  public AdditionalData title(String title) {
    this.title = title;
    return this;
  }


  @ApiModelProperty(value = "Title")
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Subtitle
   **/
  public AdditionalData subtitle(String subtitle) {
    this.subtitle = subtitle;
    return this;
  }


  @ApiModelProperty(value = "Subtitle")
  @JsonProperty("subtitle")
  public String getSubtitle() {
    return subtitle;
  }

  @JsonProperty("subtitle")
  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }

  /**
   * Description
   **/
  public AdditionalData description(String description) {
    this.description = description;
    return this;
  }


  @ApiModelProperty(value = "Description")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * List of items
   **/
  public AdditionalData items(List<@Valid AdditionalDataItem> items) {
    this.items = items;
    return this;
  }


  @ApiModelProperty(value = "List of items")
  @JsonProperty("items")
  @Valid public List<@Valid AdditionalDataItem> getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(List<@Valid AdditionalDataItem> items) {
    this.items = items;
  }

  public AdditionalData addItemsItem(AdditionalDataItem itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }

    this.items.add(itemsItem);
    return this;
  }

  public AdditionalData removeItemsItem(AdditionalDataItem itemsItem) {
    if (itemsItem != null && this.items != null) {
      this.items.remove(itemsItem);
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
    AdditionalData additionalData = (AdditionalData) o;
    return Objects.equals(this.title, additionalData.title) &&
            Objects.equals(this.subtitle, additionalData.subtitle) &&
            Objects.equals(this.description, additionalData.description) &&
            Objects.equals(this.items, additionalData.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, subtitle, description, items);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdditionalData {\n");

    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    subtitle: ").append(toIndentedString(subtitle)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
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
