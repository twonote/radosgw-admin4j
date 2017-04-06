package org.twonote.rgwadmin4j.model;

/** Created by petertc on 3/30/17. */
public class SubUser {
  /** id : rgwAdmin4jTest-6eed8682-c533-4364-bd36-684e3a9f32c7:qqqqq permissions : full-control */
  private String id;

  private String permissions;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPermissions() {
    return permissions;
  }

  public void setPermissions(String permissions) {
    this.permissions = permissions;
  }
}
