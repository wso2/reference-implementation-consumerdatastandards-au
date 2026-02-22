package org.wso2.openbanking.consumerdatastandards.account.metadata.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonTypeName("ApiResponse")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-02-19T15:46:58.698251900+05:30[Asia/Colombo]", comments = "Generator version: 7.20.0")
public class ModelApiResponse   {
    private String message;

    public ModelApiResponse() {
    }

    /**
     * Response message
     **/
    public ModelApiResponse message(String message) {
        this.message = message;
        return this;
    }


    @ApiModelProperty(example = "Disclosure options updated successfully", value = "Response message")
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModelApiResponse _apiResponse = (ModelApiResponse) o;
        return Objects.equals(this.message, _apiResponse.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelApiResponse {\n");

        sb.append("    message: ").append(toIndentedString(message)).append("\n");
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
