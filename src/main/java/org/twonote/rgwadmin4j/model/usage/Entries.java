package org.twonote.rgwadmin4j.model.usage;

import java.util.List;

/** Created by petertc on 4/6/17. */
public class Entries {
  /**
   * user : howdoyoudo buckets : [{"bucket":"howdoyoudo","time":"2017-04-05
   * 08:00:00.000000Z","epoch":1491379200,"owner":"howdoyoudo","categories":[{"category":"create_bucket","bytes_sent":171,"bytes_received":0,"ops":9,"successful_ops":9},{"category":"put_obj","bytes_sent":0,"bytes_received":983040,"ops":24,"successful_ops":24}]}]
   */
  private String user;

  private List<BucketUsage> buckets;

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public List<BucketUsage> getBuckets() {
    return buckets;
  }

  public void setBuckets(List<BucketUsage> buckets) {
    this.buckets = buckets;
  }
}
