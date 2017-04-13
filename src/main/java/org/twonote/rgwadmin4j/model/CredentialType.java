package org.twonote.rgwadmin4j.model;

/**
 * Represents credential type.
 *
 * <p>Created by hrchu on 2017/4/12.
 */
public enum CredentialType {
  S3,
  SWIFT;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }
}
