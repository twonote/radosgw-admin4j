package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;

/** Represents the sub-user information. */
public class SubUser {
  /** id : rgwAdmin4jTest-6eed8682-c533-4364-bd36-684e3a9f32c7:qqqqq permissions : full-control */
  private String id;

  @SerializedName("permissions")
  private Permission permission;

  /**
   * Get sub-user ID
   *
   * @return sub-user ID
   */
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get access permission of the sub-user.
   *
   * <p>Note that the sub-user may not have any permission.
   *
   * @return The permission.
   */
  public Optional<Permission> getPermission() {
    return Optional.ofNullable(permission);
  }

  public void setPermission(Permission permission) {
    this.permission = permission;
  }

  /**
   * Access permission for sub-user.
   */
  public enum Permission {
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
      return super.toString().toLowerCase();
    }
  }
}