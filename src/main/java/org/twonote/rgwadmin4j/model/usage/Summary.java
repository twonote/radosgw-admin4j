package org.twonote.rgwadmin4j.model.usage;

import java.util.List;

/** Created by petertc on 4/6/17. */
public class Summary {
  /**
   * user : howdoyoudo categories :
   * [{"category":"create_bucket","bytes_sent":171,"bytes_received":0,"ops":9,"successful_ops":9},{"category":"put_obj","bytes_sent":0,"bytes_received":983040,"ops":24,"successful_ops":24}]
   * total : {"bytes_sent":171,"bytes_received":983040,"ops":33,"successful_ops":33}
   */
  private String user;

  private Usage total;
  private List<Usage> categories;

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public Usage getTotal() {
    return total;
  }

  public void setTotal(Usage total) {
    this.total = total;
  }

  public List<Usage> getCategories() {
    return categories;
  }

  public void setCategories(List<Usage> categories) {
    this.categories = categories;
  }
}
