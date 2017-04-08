package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/** Represents the user quota. */
public class Quota {

  @SerializedName("enabled")
  @Expose
  private Boolean enabled;

  @SerializedName("max_size_kb")
  @Expose
  private Long maxSizeKb;

  @SerializedName("max_objects")
  @Expose
  private Long maxObjects;

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Long getMaxSizeKb() {
    return maxSizeKb;
  }

  public void setMaxSizeKb(Long maxSizeKb) {
    this.maxSizeKb = maxSizeKb;
  }

  public Long getMaxObjects() {
    return maxObjects;
  }

  public void setMaxObjects(Long maxObjects) {
    this.maxObjects = maxObjects;
  }
}
