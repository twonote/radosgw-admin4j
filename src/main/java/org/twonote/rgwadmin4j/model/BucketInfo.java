package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/** Represents the bucket information. */
public class BucketInfo {
  /*
   {
    "bucket":"testgetbucketinfo",
    "pool":"default.rgw.buckets.data",
    "index_pool":"default.rgw.buckets.index",
    "id":"064a046c-aa3b-405a-beb4-0968b75ea03f.24099.1",
    "marker":"064a046c-aa3b-405a-beb4968b75ea03f.24099.1",
    "owner":"testgetbucketinfouserx",
    "ver":"0#7",
    "master_ver":"0#0",
    "mtime":"2017-04-07 15:07:37.280666",
    "max_marker":"0#",
    "usage":{
       "rgw.main":{
          "size":122880,
          "size_actual":122880,
          "size_utilized":122880,
          "size_kb":120,
          "size_kb_actual":120,
          "size_kb_utilized":120,
          "num_objects":3
       }
    },
    "bucket_quota":{
       "enabled":false,
       "check_on_raw":false,
       "max_size":-1,
       "max_size_kb":0,
       "max_objects":-1
    }
  */
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

  /** Bucket stats information. */
  public static class Usage {
    @SerializedName("rgw.main")
    private RgwMain rgwMain;

    public RgwMain getRgwMain() {
      return rgwMain;
    }

    public void setRgwMain(RgwMain rgwMain) {
      this.rgwMain = rgwMain;
    }

    public static class RgwMain {
      /**
       * size : 122880 size_actual : 122880 size_utilized : 122880 size_kb : 120 size_kb_actual :
       * 120 size_kb_utilized : 120 num_objects : 3
       */
      private long size;

      private long size_actual;
      private long size_utilized;
      private long size_kb;
      private long size_kb_actual;
      private long size_kb_utilized;
      private long num_objects;

      public long getSize() {
        return size;
      }

      public void setSize(long size) {
        this.size = size;
      }

      public long getSize_actual() {
        return size_actual;
      }

      public void setSize_actual(long size_actual) {
        this.size_actual = size_actual;
      }

      public long getSize_utilized() {
        return size_utilized;
      }

      public void setSize_utilized(long size_utilized) {
        this.size_utilized = size_utilized;
      }

      public long getSize_kb() {
        return size_kb;
      }

      public void setSize_kb(long size_kb) {
        this.size_kb = size_kb;
      }

      public long getSize_kb_actual() {
        return size_kb_actual;
      }

      public void setSize_kb_actual(long size_kb_actual) {
        this.size_kb_actual = size_kb_actual;
      }

      public long getSize_kb_utilized() {
        return size_kb_utilized;
      }

      public void setSize_kb_utilized(long size_kb_utilized) {
        this.size_kb_utilized = size_kb_utilized;
      }

      public long getNum_objects() {
        return num_objects;
      }

      public void setNum_objects(long num_objects) {
        this.num_objects = num_objects;
      }
    }
  }
}
