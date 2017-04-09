package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.SerializedName;

/**
 * Administrative capabilities
 *
 * <p>Created by hrchu on 2017/4/8.
 */
public class Cap {
  /** type : usage perm : * */
  private final Type type;

  private final Perm perm;

  public Cap(Type type, Perm perm) {
    this.type = type;
    this.perm = perm;
  }

  public Type getType() {
    return type;
  }

  public Perm getPerm() {
    return perm;
  }

  /**
   * Format as the request parameter
   *
   * @return foo=bar
   */
  @Override
  public String toString() {
    return type + "=" + perm;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Cap cap = (Cap) o;

    return type == cap.type && perm == cap.perm;
  }

  @Override
  public int hashCode() {
    int result = type != null ? type.hashCode() : 0;
    result = 31 * result + (perm != null ? perm.hashCode() : 0);
    return result;
  }

  public enum Type {
    @SerializedName("users")
    USERS,

    @SerializedName("buckets")
    BUCKETS,

    @SerializedName("metadata")
    METADATA,

    @SerializedName("usage")
    USAGE,

    @SerializedName("zone")
    ZONE;

    @Override
    public String toString() {
      return super.toString().toLowerCase();
    }
  }

  public enum Perm {
    @SerializedName("read")
    READ,

    @SerializedName("write")
    WRITE,

    @SerializedName(
      value = "*",
      alternate = {"read, write", "read,write", "write, read", "write,read"}
    )
    READ_WRITE;

    @Override
    public String toString() {
      if (this.equals(Perm.READ_WRITE)) {
        return "*";
      } else {
        return super.toString().toLowerCase();
      }
    }
  }
}
