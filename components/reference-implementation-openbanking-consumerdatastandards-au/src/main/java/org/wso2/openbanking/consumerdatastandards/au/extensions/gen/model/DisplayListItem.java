package org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Individual display list item
 **/
@ApiModel(description = "Individual display list item")
@JsonTypeName("DisplayListItem")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-02-20T15:47:46.918170600+05:30[Asia/Colombo]", comments = "Generator version: 7.20.0")
public class DisplayListItem   {
    private String displayText;

    public DisplayListItem() {
    }

    @JsonCreator
    public DisplayListItem(
            @JsonProperty(required = true, value = "displayText") String displayText
    ) {
        this.displayText = displayText;
    }

    /**
     * Text to display in the list
     **/
    public DisplayListItem displayText(String displayText) {
        this.displayText = displayText;
        return this;
    }


    @ApiModelProperty(required = true, value = "Text to display in the list")
    @JsonProperty(required = true, value = "displayText")
    @NotNull
    public String getDisplayText() {
        return displayText;
    }

    @JsonProperty(required = true, value = "displayText")
    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DisplayListItem displayListItem = (DisplayListItem) o;
        return Objects.equals(this.displayText, displayListItem.displayText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayText);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DisplayListItem {\n");

        sb.append("    displayText: ").append(toIndentedString(displayText)).append("\n");
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
