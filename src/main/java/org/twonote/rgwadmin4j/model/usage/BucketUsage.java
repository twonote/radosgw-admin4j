package org.twonote.rgwadmin4j.model.usage;

import java.util.List;

/** Created by petertc on 4/6/17. */
public class BucketUsage {
  /**
   * bucket : howdoyoudo time : 2017-04-05 08:00:00.000000Z epoch : 1491379200 owner : howdoyoudo
   * categories :
   * [{"category":"create_bucket","bytes_sent":171,"bytes_received":0,"ops":9,"successful_ops":9},{"category":"put_obj","bytes_sent":0,"bytes_received":983040,"ops":24,"successful_ops":24}]
   */
  private String bucket;

  private String time;
  private long epoch;
  private String owner;
  private List<Usage> categories;

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public long getEpoch() {
    return epoch;
  }

  public void setEpoch(long epoch) {
    this.epoch = epoch;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public List<Usage> getCategories() {
    return categories;
  }

  public void setCategories(List<Usage> categories) {
    this.categories = categories;
  }
}
