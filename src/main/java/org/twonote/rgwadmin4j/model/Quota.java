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

  // These fields are not part of the JSON response, but are used to track
  // which user/bucket this quota belongs to for the overloaded set methods
  private transient String userId;
  private transient String bucket;

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

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }
}
