package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/** Represents the S3 credential associated with a userId or sub-userId. */
public class S3Credential {
  /**
   * user : rgwAdmin4jTest-69211f2b-d1c1-47e2-ade1-c0c6386db1a7 accessKey : 22742PSMHVP83QQM1232
   * secretKey : KtgjPrNUu4uPRArJPJGmYYF07jlE966coQyvJ7pl
   */
  @SerializedName("user")
  @Expose
  private String userId;

  @SerializedName("access_key")
  @Expose
  private String accessKey;

  @SerializedName("secret_key")
  @Expose
  private String secretKey;

  public String getUserId() {
    return userId;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    S3Credential that = (S3Credential) o;

    if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
    if (accessKey != null ? !accessKey.equals(that.accessKey) : that.accessKey != null)
      return false;
    return secretKey != null ? secretKey.equals(that.secretKey) : that.secretKey == null;
  }

  @Override
  public int hashCode() {
    int result = userId != null ? userId.hashCode() : 0;
    result = 31 * result + (accessKey != null ? accessKey.hashCode() : 0);
    result = 31 * result + (secretKey != null ? secretKey.hashCode() : 0);
    return result;
  }
}
