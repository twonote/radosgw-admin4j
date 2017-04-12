package org.twonote.rgwadmin4j.model;

/**
 * Created by hrchu on 2017/4/12.
 */
public enum KeyType {
  S3, SWIFT;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }
}
