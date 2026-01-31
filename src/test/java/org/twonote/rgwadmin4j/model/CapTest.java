package org.twonote.rgwadmin4j.model;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import org.junit.Test;

/**
 * Created by hrchu on 2017/4/8.
 */
public class CapTest {

  private final Gson gson = new Gson();

  @Test
  public void test() {
    Cap capBucketsRead = new Cap(Cap.Type.BUCKETS, Cap.Perm.READ);
    Cap capBucketsReadWrite = new Cap(Cap.Type.BUCKETS, Cap.Perm.READ_WRITE);
    Cap capUserInfoWithoutKeys = new Cap(Cap.Type.USER_INFO_WITHOUT_KEYS, Cap.Perm.READ);
    String strBucketsRead = "buckets=read";
    String strBucketsReadStar = "buckets=*";
    String strUserInfoWithoutKeys = "user-info-without-keys=read";

    // format Cap to request parameter
    assertEquals(strBucketsRead, capBucketsRead.toString());
    assertEquals(strBucketsReadStar, capBucketsReadWrite.toString());
    assertEquals(strUserInfoWithoutKeys, capUserInfoWithoutKeys.toString());

    // serialize and deserialize
    assertEquals(capBucketsRead, gson.fromJson(gson.toJson(capBucketsRead), Cap.class));
    assertEquals(capBucketsReadWrite, gson.fromJson(gson.toJson(capBucketsReadWrite), Cap.class));
    assertEquals(capUserInfoWithoutKeys, gson.fromJson(gson.toJson(capUserInfoWithoutKeys), Cap.class));

    // more test on perm read write
    assertEquals(
        capBucketsReadWrite, gson.fromJson("{\"type\":\"buckets\",\"perm\":\"*\"}", Cap.class));
    assertEquals(
        capBucketsReadWrite,
        gson.fromJson("{\"type\":\"buckets\",\"perm\":\"read,write\"}", Cap.class));
    assertEquals(
        capBucketsReadWrite,
        gson.fromJson("{\"type\":\"buckets\",\"perm\":\"read, write\"}", Cap.class));
    assertEquals(
        capBucketsReadWrite,
        gson.fromJson("{\"type\":\"buckets\",\"perm\":\"write,read\"}", Cap.class));
    assertEquals(
        capBucketsReadWrite,
        gson.fromJson("{\"type\":\"buckets\",\"perm\":\"write, read\"}", Cap.class));
    
    // test user-info-without-keys deserialization
    assertEquals(
        capUserInfoWithoutKeys,
        gson.fromJson("{\"type\":\"user-info-without-keys\",\"perm\":\"read\"}", Cap.class));
  }
}
