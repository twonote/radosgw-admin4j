package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents account information in Ceph RGW.
 *
 * <p>Accounts are a feature introduced in Ceph Squid that provide higher-level organizational
 * containers for managing multiple users with IAM-like capabilities.
 *
 * <p>Note: This feature requires Ceph Squid or later.
 */
public class Account {

  @SerializedName("id")
  @Expose
  private String accountId;

  @SerializedName("name")
  @Expose
  private String accountName;

  @SerializedName("email")
  @Expose
  private String email;

  @SerializedName("tenant")
  @Expose
  private String tenant;

  @SerializedName("quota")
  @Expose
  private Quota quota;

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getAccountName() {
    return accountName;
  }

  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getTenant() {
    return tenant;
  }

  public void setTenant(String tenant) {
    this.tenant = tenant;
  }

  public Quota getQuota() {
    return quota;
  }

  public void setQuota(Quota quota) {
    this.quota = quota;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Account account = (Account) o;

    if (accountId != null ? !accountId.equals(account.accountId) : account.accountId != null) {
      return false;
    }
    if (accountName != null
        ? !accountName.equals(account.accountName)
        : account.accountName != null) {
      return false;
    }
    if (email != null ? !email.equals(account.email) : account.email != null) {
      return false;
    }
    if (tenant != null ? !tenant.equals(account.tenant) : account.tenant != null) {
      return false;
    }
    return quota != null ? quota.equals(account.quota) : account.quota == null;
  }

  @Override
  public int hashCode() {
    int result = accountId != null ? accountId.hashCode() : 0;
    result = 31 * result + (accountName != null ? accountName.hashCode() : 0);
    result = 31 * result + (email != null ? email.hashCode() : 0);
    result = 31 * result + (tenant != null ? tenant.hashCode() : 0);
    result = 31 * result + (quota != null ? quota.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Account{"
        + "accountId='"
        + accountId
        + '\''
        + ", accountName='"
        + accountName
        + '\''
        + ", email='"
        + email
        + '\''
        + ", tenant='"
        + tenant
        + '\''
        + ", quota="
        + quota
        + '}';
  }
}