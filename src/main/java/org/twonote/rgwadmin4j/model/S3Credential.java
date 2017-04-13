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
}
