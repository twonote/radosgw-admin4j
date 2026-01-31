package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents RGW cluster/endpoint information.
 * 
 * <p>The /info endpoint returns cluster identification information.
 * According to the Ceph documentation, the response should contain an info section
 * with a cluster_id field, but the actual implementation may vary.
 */
public class ClusterInfo {

  @SerializedName("cluster_id")
  @Expose
  private String clusterId;

  /**
   * Gets the cluster ID.
   * 
   * @return The cluster ID, typically the value returned from librados::rados::cluster_fsid()
   */
  public String getClusterId() {
    return clusterId;
  }

  /**
   * Sets the cluster ID.
   * 
   * @param clusterId The cluster ID to set
   */
  public void setClusterId(String clusterId) {
    this.clusterId = clusterId;
  }

  @Override
  public String toString() {
    return "ClusterInfo{clusterId='" + clusterId + "'}";
  }
}
