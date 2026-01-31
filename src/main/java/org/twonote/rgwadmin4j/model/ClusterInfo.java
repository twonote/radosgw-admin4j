package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents RGW cluster/endpoint information.
 * 
 * <p>The /info endpoint returns cluster identification information.
 * The actual response structure is:
 * {
 *   "info": {
 *     "storage_backends": [
 *       {
 *         "name": "rados",
 *         "cluster_id": "..."
 *       }
 *     ]
 *   }
 * }
 */
public class ClusterInfo {

  @SerializedName("info")
  @Expose
  private Info info;

  /**
   * Gets the cluster ID from the first storage backend (typically RADOS).
   * 
   * @return The cluster ID, or null if not available
   */
  public String getClusterId() {
    if (info != null && info.storageBackends != null && !info.storageBackends.isEmpty()) {
      return info.storageBackends.get(0).clusterId;
    }
    return null;
  }

  public Info getInfo() {
    return info;
  }

  public void setInfo(Info info) {
    this.info = info;
  }

  @Override
  public String toString() {
    return "ClusterInfo{info=" + info + "}";
  }

  public static class Info {
    @SerializedName("storage_backends")
    @Expose
    private List<StorageBackend> storageBackends;

    public List<StorageBackend> getStorageBackends() {
      return storageBackends;
    }

    public void setStorageBackends(List<StorageBackend> storageBackends) {
      this.storageBackends = storageBackends;
    }

    @Override
    public String toString() {
      return "Info{storageBackends=" + storageBackends + "}";
    }
  }

  public static class StorageBackend {
    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("cluster_id")
    @Expose
    private String clusterId;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getClusterId() {
      return clusterId;
    }

    public void setClusterId(String clusterId) {
      this.clusterId = clusterId;
    }

    @Override
    public String toString() {
      return "StorageBackend{name='" + name + "', clusterId='" + clusterId + "'}";
    }
  }
}
