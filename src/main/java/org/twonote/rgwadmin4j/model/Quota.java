package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents the user quota.
 */
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Quota quota = (Quota) o;

    if (enabled != null ? !enabled.equals(quota.enabled) : quota.enabled != null) {
      return false;
    }
    if (maxSizeKb != null ? !maxSizeKb.equals(quota.maxSizeKb) : quota.maxSizeKb != null) {
      return false;
    }
    return maxObjects != null ? maxObjects.equals(quota.maxObjects) : quota.maxObjects == null;
  }

  @Override
  public int hashCode() {
    int result = enabled != null ? enabled.hashCode() : 0;
    result = 31 * result + (maxSizeKb != null ? maxSizeKb.hashCode() : 0);
    result = 31 * result + (maxObjects != null ? maxObjects.hashCode() : 0);
    return result;
  }
}
