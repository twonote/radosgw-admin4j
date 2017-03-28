package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Quota {

  @SerializedName("enabled")
  @Expose
  private Boolean enabled;
  // FIXME: byte or kb?
  @SerializedName("max_size_kb")
  @Expose
  private Integer maxSizeKb;
  @SerializedName("max_objects")
  @Expose
  private Integer maxObjects;

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Integer getMaxSizeKb() {
    return maxSizeKb;
  }

  public void setMaxSizeKb(Integer maxSizeKb) {
    this.maxSizeKb = maxSizeKb;
  }

  public Integer getMaxObjects() {
    return maxObjects;
  }

  public void setMaxObjects(Integer maxObjects) {
    this.maxObjects = maxObjects;
  }

}
