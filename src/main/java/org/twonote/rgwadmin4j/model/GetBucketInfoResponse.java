package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetBucketInfoResponse {

    @SerializedName("bucket")
    @Expose
    private String bucket;
    @SerializedName("pool")
    @Expose
    private String pool;
    @SerializedName("index_pool")
    @Expose
    private String indexPool;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("marker")
    @Expose
    private String marker;
    @SerializedName("owner")
    @Expose
    private String owner;
    @SerializedName("ver")
    @Expose
    private String ver;
    @SerializedName("master_ver")
    @Expose
    private String masterVer;
    @SerializedName("mtime")
    @Expose
    private String mtime;
    @SerializedName("max_marker")
    @Expose
    private String maxMarker;
    @SerializedName("usage")
    @Expose
    private Usage usage;
    @SerializedName("bucket_quota")
    @Expose
    private Quota bucketQuota;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPool() {
        return pool;
    }

    public void setPool(String pool) {
        this.pool = pool;
    }

    public String getIndexPool() {
        return indexPool;
    }

    public void setIndexPool(String indexPool) {
        this.indexPool = indexPool;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getMasterVer() {
        return masterVer;
    }

    public void setMasterVer(String masterVer) {
        this.masterVer = masterVer;
    }

    public String getMtime() {
        return mtime;
    }

    public void setMtime(String mtime) {
        this.mtime = mtime;
    }

    public String getMaxMarker() {
        return maxMarker;
    }

    public void setMaxMarker(String maxMarker) {
        this.maxMarker = maxMarker;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public Quota getBucketQuota() {
        return bucketQuota;
    }

    public void setBucketQuota(Quota bucketQuota) {
        this.bucketQuota = bucketQuota;
    }

}
