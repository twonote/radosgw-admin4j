package org.twonote.rgwadmin4j.model.usage;

/** Created by petertc on 4/6/17. */
public class Usage {
  /** category : create_bucket bytes_sent : 171 bytes_received : 0 ops : 9 successful_ops : 9 */
  private String category;

  private long bytes_sent;
  private long bytes_received;
  private long ops;
  private long successful_ops;

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public long getBytes_sent() {
    return bytes_sent;
  }

  public void setBytes_sent(long bytes_sent) {
    this.bytes_sent = bytes_sent;
  }

  public long getBytes_received() {
    return bytes_received;
  }

  public void setBytes_received(long bytes_received) {
    this.bytes_received = bytes_received;
  }

  public long getOps() {
    return ops;
  }

  public void setOps(long ops) {
    this.ops = ops;
  }

  public long getSuccessful_ops() {
    return successful_ops;
  }

  public void setSuccessful_ops(long successful_ops) {
    this.successful_ops = successful_ops;
  }
}
