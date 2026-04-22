package org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Individual additional data item
 **/
@ApiModel(description = "Individual additional data item")
@JsonTypeName("AdditionalDataItem")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-04-07T10:52:53.468738900+05:30[Asia/Colombo]", comments = "Generator version: 7.21.0")
public class AdditionalDataItem   {
    private String item;
    private String type;

    public AdditionalDataItem() {
    }

    @JsonCreator
    public AdditionalDataItem(
            @JsonProperty(required = true, value = "item") String item
    ) {
        this.item = item;
    }

    /**
     * Item value
     **/
    public AdditionalDataItem item(String item) {
        this.item = item;
        return this;
    }


    @ApiModelProperty(required = true, value = "Item value")
    @JsonProperty(required = true, value = "item")
    @NotNull public String getItem() {
        return item;
    }

    @JsonProperty(required = true, value = "item")
    public void setItem(String item) {
        this.item = item;
    }

    /**
     * Item type
     **/
    public AdditionalDataItem type(String type) {
        this.type = type;
        return this;
    }


    @ApiModelProperty(value = "Item type")
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdditionalDataItem additionalDataItem = (AdditionalDataItem) o;
        return Objects.equals(this.item, additionalDataItem.item) &&
                Objects.equals(this.type, additionalDataItem.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AdditionalDataItem {\n");

        sb.append("    item: ").append(toIndentedString(item)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
