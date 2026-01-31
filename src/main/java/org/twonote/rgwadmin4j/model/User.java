package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents the user information.
 */
public class User {

  @SerializedName("user_id")
  @Expose
  private String userId;

  @SerializedName("display_name")
  @Expose
  private String displayName;

  @SerializedName("email")
  @Expose
  private String email;

  @SerializedName("suspended")
  @Expose
  private Integer suspended;

  @SerializedName("max_buckets")
  @Expose
  private Integer maxBuckets;

  @SerializedName("subusers")
  @Expose
  private List<SubUser> subusers = null;

  @SerializedName("keys")
  @Expose
  private List<S3Credential> s3Credentials = null;

  @SerializedName("swift_keys")
  @Expose
  private List<SwiftCredential> swiftCredentials = null;

  @SerializedName("caps")
  @Expose
  private List<Cap> caps = null;

  @SerializedName("account_id")
  @Expose
  private String accountId;

  @SerializedName("account_root")
  @Expose
  private Boolean accountRoot;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Integer getSuspended() {
    return suspended;
  }

  public void setSuspended(Integer suspended) {
    this.suspended = suspended;
  }

  public Integer getMaxBuckets() {
    return maxBuckets;
  }

  public void setMaxBuckets(Integer maxBuckets) {
    this.maxBuckets = maxBuckets;
  }

  public List<SubUser> getSubusers() {
    return subusers;
  }

  public void setSubusers(List<SubUser> subusers) {
    this.subusers = subusers;
  }

  public List<S3Credential> getS3Credentials() {
    return s3Credentials;
  }

  public void setS3Credentials(List<S3Credential> s3Credentials) {
    this.s3Credentials = s3Credentials;
  }

  public List<SwiftCredential> getSwiftCredentials() {
    return swiftCredentials;
  }

  public void setSwiftCredentials(List<SwiftCredential> swiftCredentials) {
    this.swiftCredentials = swiftCredentials;
  }

  public List<Cap> getCaps() {
    return caps;
  }

  public void setCaps(List<Cap> caps) {
    this.caps = caps;
  }

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public Boolean getAccountRoot() {
    return accountRoot;
  }

  public void setAccountRoot(Boolean accountRoot) {
    this.accountRoot = accountRoot;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    User user = (User) o;

    if (userId != null ? !userId.equals(user.userId) : user.userId != null) {
      return false;
    }
    if (displayName != null ? !displayName.equals(user.displayName) : user.displayName != null) {
      return false;
    }
    if (email != null ? !email.equals(user.email) : user.email != null) {
      return false;
    }
    if (suspended != null ? !suspended.equals(user.suspended) : user.suspended != null) {
      return false;
    }
    if (maxBuckets != null ? !maxBuckets.equals(user.maxBuckets) : user.maxBuckets != null) {
      return false;
    }
    if (subusers != null ? !subusers.equals(user.subusers) : user.subusers != null) {
      return false;
    }
    if (s3Credentials != null
        ? !s3Credentials.equals(user.s3Credentials)
        : user.s3Credentials != null) {
      return false;
    }
    if (swiftCredentials != null
        ? !swiftCredentials.equals(user.swiftCredentials)
        : user.swiftCredentials != null) {
      return false;
    }
    if (caps != null ? !caps.equals(user.caps) : user.caps != null) {
      return false;
    }
    if (accountId != null ? !accountId.equals(user.accountId) : user.accountId != null) {
      return false;
    }
    return accountRoot != null ? accountRoot.equals(user.accountRoot) : user.accountRoot == null;
  }

  @Override
  public int hashCode() {
    int result = userId != null ? userId.hashCode() : 0;
    result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
    result = 31 * result + (email != null ? email.hashCode() : 0);
    result = 31 * result + (suspended != null ? suspended.hashCode() : 0);
    result = 31 * result + (maxBuckets != null ? maxBuckets.hashCode() : 0);
    result = 31 * result + (subusers != null ? subusers.hashCode() : 0);
    result = 31 * result + (s3Credentials != null ? s3Credentials.hashCode() : 0);
    result = 31 * result + (swiftCredentials != null ? swiftCredentials.hashCode() : 0);
    result = 31 * result + (caps != null ? caps.hashCode() : 0);
    result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
    result = 31 * result + (accountRoot != null ? accountRoot.hashCode() : 0);
    return result;
  }
}
