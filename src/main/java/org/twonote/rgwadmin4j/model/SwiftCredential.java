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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SwiftCredential that = (SwiftCredential) o;

    if (username != null ? !username.equals(that.username) : that.username != null) return false;
    return password != null ? password.equals(that.password) : that.password == null;
  }

  @Override
  public int hashCode() {
    int result = username != null ? username.hashCode() : 0;
    result = 31 * result + (password != null ? password.hashCode() : 0);
    return result;
  }
}
