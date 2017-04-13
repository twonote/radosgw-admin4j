package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/** Represents the Swift credential associated with a sub-userId. */
public class SwiftCredential {
  /** user : rgwAdmin4jTest-69211f2b-d1c1-47e2-ade1-c0c6386db1a7 accessKey : 22742PSMHVP83QQM1232 */
  @SerializedName("user")
  @Expose
  private String username;

  @SerializedName("secret_key")
  @Expose
  private String password;

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  // The username is equivalent to the user id for swift subuser.
  public String getUserId() {
    return username;
  }
}
