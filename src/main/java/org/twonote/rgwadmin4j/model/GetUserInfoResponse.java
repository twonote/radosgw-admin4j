package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class GetUserInfoResponse {

    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("display_name")
    @Expose
    private String displayName;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("suspended")
    @Expose
    private Integer suspended;
    @SerializedName("max_buckets")
    @Expose
    private Integer maxBuckets;
    @SerializedName("subusers")
    @Expose
    private List<Object> subusers = null;
    @SerializedName("keys")
    @Expose
    private List<Key> keys = null;
    @SerializedName("swift_keys")
    @Expose
    private List<Object> swiftKeys = null;
    @SerializedName("caps")
    @Expose
    private List<Map<String,String>> caps = null;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getSuspended() {
        return suspended;
    }

    public void setSuspended(Integer suspended) {
        this.suspended = suspended;
    }

    public Integer getMaxBuckets() {
        return maxBuckets;
    }

    public void setMaxBuckets(Integer maxBuckets) {
        this.maxBuckets = maxBuckets;
    }

    public List<Object> getSubusers() {
        return subusers;
    }

    public void setSubusers(List<Object> subusers) {
        this.subusers = subusers;
    }

    public List<Key> getKeys() {
        return keys;
    }

    public void setKeys(List<Key> keys) {
        this.keys = keys;
    }

    public List<Object> getSwiftKeys() {
        return swiftKeys;
    }

    public void setSwiftKeys(List<Object> swiftKeys) {
        this.swiftKeys = swiftKeys;
    }

    public List<Map<String,String>> getCaps() {
        return caps;
    }

    public void setCaps(List<Map<String,String>> caps) {
        this.caps = caps;
    }

}
