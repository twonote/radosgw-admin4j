package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents RGW cluster/endpoint information.
 * 
 * <p>Example response:
 * <pre>
 * {
 *   "info": {
 *     "cluster_id": "ceph-cluster-id"
 *   }
 * }
 * </pre>
 */
public class ClusterInfo {

  @SerializedName("info")
  @Expose
  private Info info;

  /**
   * Gets the info container.
   * 
   * @return The info container with cluster metadata
   */
  public Info getInfo() {
    return info;
  }

  /**
   * Sets the info container.
   * 
   * @param info The info container to set
   */
  public void setInfo(Info info) {
    this.info = info;
  }

  /**
   * Gets the cluster ID directly for convenience.
   * 
   * @return The cluster ID, or null if info is not set
   */
  public String getClusterId() {
    return info != null ? info.getClusterId() : null;
  }

  /**
   * Inner class representing the info section.
   */
  public static class Info {
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
}
