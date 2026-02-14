package org.wso2.openbanking.consumerdatastandards.au.extensions.gen.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;

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
public class ConsumerAndDisplayData {
  private SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData;
  private SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData displayData;

  public ConsumerAndDisplayData(SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData consumerData,
                                SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData displayData) {
    this.consumerData = consumerData;
    this.displayData = displayData;
  }

  public SuccessResponsePopulateConsentAuthorizeScreenDataConsumerData getConsumerData() {
    return consumerData;
  }

  public SuccessResponsePopulateConsentAuthorizeScreenDataDisplayData getDisplayData() {
    return displayData;
  }
}
