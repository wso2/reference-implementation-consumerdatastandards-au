package org.wso2.openbanking.consumerdatastandards.account.metadata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@JsonTypeName("ErrorResponse")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-02-24T14:09:49.873196700+05:30[Asia/Colombo]", comments = "Generator version: 7.20.0")
public class ErrorResponse   {
    private String errorDescription;

    public ErrorResponse() {
    }

    /**
     * Error description
     **/
    public ErrorResponse errorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
        return this;
    }


    @ApiModelProperty(example = "Invalid disclosure option status. Allowed values are no-sharing, pre-approval", value = "Error description")
    @JsonProperty("error_description")
    public String getErrorDescription() {
        return errorDescription;
    }

    @JsonProperty("error_description")
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ErrorResponse errorResponse = (ErrorResponse) o;
        return Objects.equals(this.errorDescription, errorResponse.errorDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorDescription);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ErrorResponse {\n");

        sb.append("    errorDescription: ").append(toIndentedString(errorDescription)).append("\n");
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
