package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents the credential associated with a user or sub-user.
 *
 * <p>Note that the key may represents a S3 or a Swift credential, and have different forms. S3
 * credential is composed of access key and secret key. On the other hand, Swift credential is a
 * username/password pair.
 */
// TODO: divided into swift/s3 key
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

  /**
   * Retrieve S3 user ID or Swift username.
   *
   * @return S3 user ID or Swift username.
   */
  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  /**
   * Retrieve S3 access key or Swift password.
   *
   * @return S3 access key or Swift password.
   */
  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  /**
   * Retrieve S3 secret key.
   *
   * <p>If the object represents swift credential, this field will be null.
   *
   * @return S3 secret key.
   */
  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  /**
   * Determine the key represents S3 or swift credential.
   *
   * @return True if the key is swift credential. Vice versa.
   */
  public boolean isSwiftKey() {
    return secretKey == null;
  }

  /**
   * Determine the key represents S3 or swift credential.
   *
   * @return True if the key is S3 credential. Vice versa.
   */
  public boolean isS3Key() {
    return !isSwiftKey();
  }
}
