package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.SerializedName;

/** Represents the sub-user information. */
public class SubUser {
  /** id : rgwAdmin4jTest-6eed8682-c533-4364-bd36-684e3a9f32c7:qqqqq permissions : full-control */
  private String id;

  @SerializedName("permissions")
  private Permission permission;

  /**
   * Get sub-user ID
   *
   * <p>Note that it will be in the absolute form, i.e., [USER_ID]:[SUB_USER_ID]
   *
   * @return sub-user ID
   */
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getParentUserId() {
    return id.split(":")[0];
  }

  public String getRelativeSubUserId() {
    return id.split(":")[1];
  }

  /**
   * Get access permission of the sub-user.
   *
   * <p>Note that the sub-user may not have any permission.
   *
   * @return The permission.
   */
  public Permission getPermission() {
    return permission;
  }

  public void setPermission(Permission permission) {
    this.permission = permission;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SubUser subUser = (SubUser) o;

    if (id != null ? !id.equals(subUser.id) : subUser.id != null) return false;
    return permission == subUser.permission;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (permission != null ? permission.hashCode() : 0);
    return result;
  }

  /** Access permission for sub-user. */
  public enum Permission {
    @SerializedName("<none>")
    NONE,

    @SerializedName("read")
    READ,

    @SerializedName("write")
    WRITE,

    @SerializedName("readwrite")
    READ_WRITE,

    @SerializedName(
      value = "full",
      alternate = {"full-control"}
    )
    FULL;

    @Override
    public String toString() {
      if (this.equals(NONE)) {
        return "";
      } else {
        return super.toString().toLowerCase();
      }
    }
  }
}
