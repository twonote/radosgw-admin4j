package org.twonote.rgwadmin4j.model;

/** Created by petertc on 3/30/17. */
public class CreateKeyResponse {
  /**
   * user : rgwAdmin4jTest-69211f2b-d1c1-47e2-ade1-c0c6386db1a7 access_key : 22742PSMHVP83QQM1232
   * secret_key : KtgjPrNUu4uPRArJPJGmYYF07jlE966coQyvJ7pl
   */
  private String user;

  private String access_key;
  private String secret_key;

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getAccess_key() {
    return access_key;
  }

  public void setAccess_key(String access_key) {
    this.access_key = access_key;
  }

  public String getSecret_key() {
    return secret_key;
  }

  public void setSecret_key(String secret_key) {
    this.secret_key = secret_key;
  }
}
