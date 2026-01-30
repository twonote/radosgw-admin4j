package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents account information for Ceph Squid release and later.
 *
 * <p>Accounts provide multi-tenancy support with comprehensive quota and resource limits.
 * Account IDs have the format: "RGW" + 17 numeric characters.
 *
 * @since Ceph Squid
 */
public class Account {

  @SerializedName("id")
  @Expose
  private String id;

  @SerializedName("name")
  @Expose
  private String name;

  @SerializedName("tenant")
  @Expose
  private String tenant;

  @SerializedName("email")
  @Expose
  private String email;

  @SerializedName("quota")
  @Expose
  private Quota quota;

  @SerializedName("bucket_quota")
  @Expose
  private Quota bucketQuota;

  @SerializedName("max_users")
  @Expose
  private Integer maxUsers;

  @SerializedName("max_roles")
  @Expose
  private Integer maxRoles;

  @SerializedName("max_groups")
  @Expose
  private Integer maxGroups;

  @SerializedName("max_access_keys")
  @Expose
  private Integer maxAccessKeys;

  @SerializedName("max_buckets")
  @Expose
  private Integer maxBuckets;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTenant() {
    return tenant;
  }

  public void setTenant(String tenant) {
    this.tenant = tenant;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Quota getQuota() {
    return quota;
  }

  public void setQuota(Quota quota) {
    this.quota = quota;
  }

  public Quota getBucketQuota() {
    return bucketQuota;
  }

  public void setBucketQuota(Quota bucketQuota) {
    this.bucketQuota = bucketQuota;
  }

  public Integer getMaxUsers() {
    return maxUsers;
  }

  public void setMaxUsers(Integer maxUsers) {
    this.maxUsers = maxUsers;
  }

  public Integer getMaxRoles() {
    return maxRoles;
  }

  public void setMaxRoles(Integer maxRoles) {
    this.maxRoles = maxRoles;
  }

  public Integer getMaxGroups() {
    return maxGroups;
  }

  public void setMaxGroups(Integer maxGroups) {
    this.maxGroups = maxGroups;
  }

  public Integer getMaxAccessKeys() {
    return maxAccessKeys;
  }

  public void setMaxAccessKeys(Integer maxAccessKeys) {
    this.maxAccessKeys = maxAccessKeys;
  }

  public Integer getMaxBuckets() {
    return maxBuckets;
  }

  public void setMaxBuckets(Integer maxBuckets) {
    this.maxBuckets = maxBuckets;
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

    if (id != null ? !id.equals(account.id) : account.id != null) {
      return false;
    }
    if (name != null ? !name.equals(account.name) : account.name != null) {
      return false;
    }
    if (tenant != null ? !tenant.equals(account.tenant) : account.tenant != null) {
      return false;
    }
    if (email != null ? !email.equals(account.email) : account.email != null) {
      return false;
    }
    if (quota != null ? !quota.equals(account.quota) : account.quota != null) {
      return false;
    }
    if (bucketQuota != null
        ? !bucketQuota.equals(account.bucketQuota)
        : account.bucketQuota != null) {
      return false;
    }
    if (maxUsers != null ? !maxUsers.equals(account.maxUsers) : account.maxUsers != null) {
      return false;
    }
    if (maxRoles != null ? !maxRoles.equals(account.maxRoles) : account.maxRoles != null) {
      return false;
    }
    if (maxGroups != null ? !maxGroups.equals(account.maxGroups) : account.maxGroups != null) {
      return false;
    }
    if (maxAccessKeys != null
        ? !maxAccessKeys.equals(account.maxAccessKeys)
        : account.maxAccessKeys != null) {
      return false;
    }
    return maxBuckets != null ? maxBuckets.equals(account.maxBuckets) : account.maxBuckets == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (tenant != null ? tenant.hashCode() : 0);
    result = 31 * result + (email != null ? email.hashCode() : 0);
    result = 31 * result + (quota != null ? quota.hashCode() : 0);
    result = 31 * result + (bucketQuota != null ? bucketQuota.hashCode() : 0);
    result = 31 * result + (maxUsers != null ? maxUsers.hashCode() : 0);
    result = 31 * result + (maxRoles != null ? maxRoles.hashCode() : 0);
    result = 31 * result + (maxGroups != null ? maxGroups.hashCode() : 0);
    result = 31 * result + (maxAccessKeys != null ? maxAccessKeys.hashCode() : 0);
    result = 31 * result + (maxBuckets != null ? maxBuckets.hashCode() : 0);
    return result;
  }
}
