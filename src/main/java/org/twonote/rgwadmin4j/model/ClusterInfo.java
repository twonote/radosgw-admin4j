package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents cluster information returned by the /info endpoint.
 *
 * <p>The info endpoint provides metadata about the RADOS Gateway cluster.
 *
 * @since Ceph Squid
 */
public class ClusterInfo {

  @SerializedName("cluster_id")
  @Expose
  private String clusterId;

  @SerializedName("realm_id")
  @Expose
  private String realmId;

  @SerializedName("realm_name")
  @Expose
  private String realmName;

  @SerializedName("zonegroup_id")
  @Expose
  private String zonegroupId;

  @SerializedName("zonegroup_name")
  @Expose
  private String zonegroupName;

  @SerializedName("zone_id")
  @Expose
  private String zoneId;

  @SerializedName("zone_name")
  @Expose
  private String zoneName;

  public String getClusterId() {
    return clusterId;
  }

  public void setClusterId(String clusterId) {
    this.clusterId = clusterId;
  }

  public String getRealmId() {
    return realmId;
  }

  public void setRealmId(String realmId) {
    this.realmId = realmId;
  }

  public String getRealmName() {
    return realmName;
  }

  public void setRealmName(String realmName) {
    this.realmName = realmName;
  }

  public String getZonegroupId() {
    return zonegroupId;
  }

  public void setZonegroupId(String zonegroupId) {
    this.zonegroupId = zonegroupId;
  }

  public String getZonegroupName() {
    return zonegroupName;
  }

  public void setZonegroupName(String zonegroupName) {
    this.zonegroupName = zonegroupName;
  }

  public String getZoneId() {
    return zoneId;
  }

  public void setZoneId(String zoneId) {
    this.zoneId = zoneId;
  }

  public String getZoneName() {
    return zoneName;
  }

  public void setZoneName(String zoneName) {
    this.zoneName = zoneName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ClusterInfo that = (ClusterInfo) o;

    if (clusterId != null ? !clusterId.equals(that.clusterId) : that.clusterId != null) {
      return false;
    }
    if (realmId != null ? !realmId.equals(that.realmId) : that.realmId != null) {
      return false;
    }
    if (realmName != null ? !realmName.equals(that.realmName) : that.realmName != null) {
      return false;
    }
    if (zonegroupId != null ? !zonegroupId.equals(that.zonegroupId) : that.zonegroupId != null) {
      return false;
    }
    if (zonegroupName != null
        ? !zonegroupName.equals(that.zonegroupName)
        : that.zonegroupName != null) {
      return false;
    }
    if (zoneId != null ? !zoneId.equals(that.zoneId) : that.zoneId != null) {
      return false;
    }
    return zoneName != null ? zoneName.equals(that.zoneName) : that.zoneName == null;
  }

  @Override
  public int hashCode() {
    int result = clusterId != null ? clusterId.hashCode() : 0;
    result = 31 * result + (realmId != null ? realmId.hashCode() : 0);
    result = 31 * result + (realmName != null ? realmName.hashCode() : 0);
    result = 31 * result + (zonegroupId != null ? zonegroupId.hashCode() : 0);
    result = 31 * result + (zonegroupName != null ? zonegroupName.hashCode() : 0);
    result = 31 * result + (zoneId != null ? zoneId.hashCode() : 0);
    result = 31 * result + (zoneName != null ? zoneName.hashCode() : 0);
    return result;
  }
}
