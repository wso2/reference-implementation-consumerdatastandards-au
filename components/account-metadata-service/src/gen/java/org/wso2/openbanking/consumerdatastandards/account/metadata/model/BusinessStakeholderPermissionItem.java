package org.wso2.openbanking.consumerdatastandards.account.metadata.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("BusinessStakeholderPermissionItem")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2026-03-23T16:02:15.442856200+05:30[Asia/Colombo]", comments = "Generator version: 7.20.0")
public class BusinessStakeholderPermissionItem   {
  private String accountId;
  private String userId;
  public enum PermissionEnum {

    VIEW(String.valueOf("VIEW")), AUTHORIZE(String.valueOf("AUTHORIZE")), REVOKE(String.valueOf("REVOKE"));


    private String value;

    PermissionEnum (String v) {
      value = v;
    }

    public String value() {
      return value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
    public static PermissionEnum fromString(String s) {
      for (PermissionEnum b : PermissionEnum.values()) {
        // using Objects.toString() to be safe if value type non-object type
        // because types like 'int' etc. will be auto-boxed
        if (Objects.toString(b.value).equals(s)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

    @JsonCreator
    public static PermissionEnum fromValue(String value) {
      for (PermissionEnum b : PermissionEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private PermissionEnum permission;

  public BusinessStakeholderPermissionItem() {
  }

  @JsonCreator
  public BusinessStakeholderPermissionItem(
          @JsonProperty(required = true, value = "accountId") String accountId,
          @JsonProperty(required = true, value = "userId") String userId,
          @JsonProperty(required = true, value = "permission") PermissionEnum permission
  ) {
    this.accountId = accountId;
    this.userId = userId;
    this.permission = permission;
  }

  /**
   * Account ID
   **/
  public BusinessStakeholderPermissionItem accountId(String accountId) {
    this.accountId = accountId;
    return this;
  }


  @ApiModelProperty(example = "586-522-B0025", required = true, value = "Account ID")
  @JsonProperty(required = true, value = "accountId")
  @NotNull  @Size(min=1)public String getAccountId() {
    return accountId;
  }

  @JsonProperty(required = true, value = "accountId")
  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  /**
   * User identifier
   **/
  public BusinessStakeholderPermissionItem userId(String userId) {
    this.userId = userId;
    return this;
  }


  @ApiModelProperty(example = "nominatedUser1@wso2.com@carbon.super", required = true, value = "User identifier")
  @JsonProperty(required = true, value = "userId")
  @NotNull  @Size(min=1)public String getUserId() {
    return userId;
  }

  @JsonProperty(required = true, value = "userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Permission assigned to the user for the account
   **/
  public BusinessStakeholderPermissionItem permission(PermissionEnum permission) {
    this.permission = permission;
    return this;
  }


  @ApiModelProperty(example = "AUTHORIZE", required = true, value = "Permission assigned to the user for the account")
  @JsonProperty(required = true, value = "permission")
  @NotNull public PermissionEnum getPermission() {
    return permission;
  }

  @JsonProperty(required = true, value = "permission")
  public void setPermission(PermissionEnum permission) {
    this.permission = permission;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BusinessStakeholderPermissionItem businessStakeholderPermissionItem = (BusinessStakeholderPermissionItem) o;
    return Objects.equals(this.accountId, businessStakeholderPermissionItem.accountId) &&
            Objects.equals(this.userId, businessStakeholderPermissionItem.userId) &&
            Objects.equals(this.permission, businessStakeholderPermissionItem.permission);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountId, userId, permission);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BusinessStakeholderPermissionItem {\n");

    sb.append("    accountId: ").append(toIndentedString(accountId)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    permission: ").append(toIndentedString(permission)).append("\n");
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

