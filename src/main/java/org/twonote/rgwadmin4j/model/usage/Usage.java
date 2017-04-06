package org.twonote.rgwadmin4j.model.usage;

/** Created by petertc on 4/6/17. */
public class Usage {
  /** category : create_bucket bytes_sent : 171 bytes_received : 0 ops : 9 successful_ops : 9 */
  private String category;

  private int bytes_sent;
  private int bytes_received;
  private int ops;
  private int successful_ops;

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public int getBytes_sent() {
    return bytes_sent;
  }

  public void setBytes_sent(int bytes_sent) {
    this.bytes_sent = bytes_sent;
  }

  public int getBytes_received() {
    return bytes_received;
  }

  public void setBytes_received(int bytes_received) {
    this.bytes_received = bytes_received;
  }

  public int getOps() {
    return ops;
  }

  public void setOps(int ops) {
    this.ops = ops;
  }

  public int getSuccessful_ops() {
    return successful_ops;
  }

  public void setSuccessful_ops(int successful_ops) {
    this.successful_ops = successful_ops;
  }
}
