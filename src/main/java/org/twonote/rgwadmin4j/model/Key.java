package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents the key associated with a user or sub-user.
 *
 * <p>Note that the key may represents a S3 access key pair (Composed of access key and secret key)
 * or a Swift secret. In case of Swift, the secret will be available in the access key field.
 */
public class Key {
  /**
   * user : rgwAdmin4jTest-69211f2b-d1c1-47e2-ade1-c0c6386db1a7 accessKey : 22742PSMHVP83QQM1232
   * secretKey : KtgjPrNUu4uPRArJPJGmYYF07jlE966coQyvJ7pl
   */
  @SerializedName("user")
  @Expose
  private String user;

  @SerializedName("access_key")
  @Expose
  private String accessKey;

  @SerializedName("secret_key")
  @Expose
  private String secretKey;

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }
}
