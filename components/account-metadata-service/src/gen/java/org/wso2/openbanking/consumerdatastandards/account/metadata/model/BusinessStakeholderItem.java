package org.wso2.openbanking.consumerdatastandards.account.metadata.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("BusinessStakeholderItem")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-03-23T16:02:15.442856200+05:30[Asia/Colombo]", comments = "Generator version: 7.20.0")
public class BusinessStakeholderItem   {
  private String accountID;
  private @Valid List<@Size(min = 1)String> accountOwners = new ArrayList<>();
  private @Valid List<@Valid BusinessStakeholderRepresentative> nominatedRepresentatives = new ArrayList<>();

  public BusinessStakeholderItem() {
  }

  @JsonCreator
  public BusinessStakeholderItem(
          @JsonProperty(required = true, value = "accountID") String accountID,
          @JsonProperty(required = true, value = "accountOwners") List< @Size(min=1)String> accountOwners,
          @JsonProperty(required = true, value = "nominatedRepresentatives") List<@Valid BusinessStakeholderRepresentative> nominatedRepresentatives
  ) {
    this.accountID = accountID;
    this.accountOwners = accountOwners;
    this.nominatedRepresentatives = nominatedRepresentatives;
  }

  /**
   * Account ID
   **/
  public BusinessStakeholderItem accountID(String accountID) {
    this.accountID = accountID;
    return this;
  }


  @ApiModelProperty(example = "586-522-B0025", required = true, value = "Account ID")
  @JsonProperty(required = true, value = "accountID")
  @NotNull  @Size(min=1)public String getAccountID() {
    return accountID;
  }

  @JsonProperty(required = true, value = "accountID")
  public void setAccountID(String accountID) {
    this.accountID = accountID;
  }

  /**
   * List of account owner user identifiers
   **/
  public BusinessStakeholderItem accountOwners(List<@Size(min = 1)String> accountOwners) {
    this.accountOwners = accountOwners;
    return this;
  }


  @ApiModelProperty(example = "[\"nominatedUser3@wso2.com@carbon.super\",\"user2@wso2.com@carbon.super\"]", required = true, value = "List of account owner user identifiers")
  @JsonProperty(required = true, value = "accountOwners")
  @NotNull public List< @Size(min=1)String> getAccountOwners() {
    return accountOwners;
  }

  @JsonProperty(required = true, value = "accountOwners")
  public void setAccountOwners(List<@Size(min = 1)String> accountOwners) {
    this.accountOwners = accountOwners;
  }

  public BusinessStakeholderItem addAccountOwnersItem(String accountOwnersItem) {
    if (this.accountOwners == null) {
      this.accountOwners = new ArrayList<>();
    }

    this.accountOwners.add(accountOwnersItem);
    return this;
  }

  public BusinessStakeholderItem removeAccountOwnersItem(String accountOwnersItem) {
    if (accountOwnersItem != null && this.accountOwners != null) {
      this.accountOwners.remove(accountOwnersItem);
    }

    return this;
  }
  /**
   * List of nominated representatives with permissions
   **/
  public BusinessStakeholderItem nominatedRepresentatives(List<@Valid BusinessStakeholderRepresentative> nominatedRepresentatives) {
    this.nominatedRepresentatives = nominatedRepresentatives;
    return this;
  }


  @ApiModelProperty(required = true, value = "List of nominated representatives with permissions")
  @JsonProperty(required = true, value = "nominatedRepresentatives")
  @NotNull @Valid  @Size(min=1)public List<@Valid BusinessStakeholderRepresentative> getNominatedRepresentatives() {
    return nominatedRepresentatives;
  }

  @JsonProperty(required = true, value = "nominatedRepresentatives")
  public void setNominatedRepresentatives(List<@Valid BusinessStakeholderRepresentative> nominatedRepresentatives) {
    this.nominatedRepresentatives = nominatedRepresentatives;
  }

  public BusinessStakeholderItem addNominatedRepresentativesItem(BusinessStakeholderRepresentative nominatedRepresentativesItem) {
    if (this.nominatedRepresentatives == null) {
      this.nominatedRepresentatives = new ArrayList<>();
    }

    this.nominatedRepresentatives.add(nominatedRepresentativesItem);
    return this;
  }

  public BusinessStakeholderItem removeNominatedRepresentativesItem(BusinessStakeholderRepresentative nominatedRepresentativesItem) {
    if (nominatedRepresentativesItem != null && this.nominatedRepresentatives != null) {
      this.nominatedRepresentatives.remove(nominatedRepresentativesItem);
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
    BusinessStakeholderItem businessStakeholderItem = (BusinessStakeholderItem) o;
    return Objects.equals(this.accountID, businessStakeholderItem.accountID) &&
            Objects.equals(this.accountOwners, businessStakeholderItem.accountOwners) &&
            Objects.equals(this.nominatedRepresentatives, businessStakeholderItem.nominatedRepresentatives);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountID, accountOwners, nominatedRepresentatives);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BusinessStakeholderItem {\n");

    sb.append("    accountID: ").append(toIndentedString(accountID)).append("\n");
    sb.append("    accountOwners: ").append(toIndentedString(accountOwners)).append("\n");
    sb.append("    nominatedRepresentatives: ").append(toIndentedString(nominatedRepresentatives)).append("\n");
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

