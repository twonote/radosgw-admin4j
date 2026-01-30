package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents RGW cluster/endpoint information.
 * 
 * <p>Example response:
 * <pre>
 * {
 *   "cluster_id": "ceph-cluster-id"
 * }
 * </pre>
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
}
