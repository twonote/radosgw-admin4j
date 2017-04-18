package org.twonote.rgwadmin4j.examples;

import com.amazonaws.services.s3.AmazonS3;
import com.google.common.collect.ImmutableMap;
import org.junit.Ignore;
import org.junit.Test;
import org.twonote.rgwadmin4j.impl.BaseTest;
import org.twonote.rgwadmin4j.model.*;

import java.util.List;
import java.util.UUID;

/** Created by petertc on 4/18/17. */
public class Example extends BaseTest {
  // Remove @Ignore before run
  @Test
  @Ignore("Not a test")
  public void run() throws Exception {
    String userId = "exampleUserId-" + UUID.randomUUID().toString();
    try {
      user(userId);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      RGW_ADMIN.removeUser(userId);
    }

    testWithAUser(Example::quota);
    testWithAUser(Example::store);
    testWithAUser(Example::usage);
  }

  /*
   * User/subuser management
   */
  private static void user(String userId) {
    // List user in the system
    List<User> users = RGW_ADMIN.listUserInfo();

    // Create user
    RGW_ADMIN.createUser(userId);

    // Get user information and show keys
    User user = RGW_ADMIN.getUserInfo(userId).get();
    user.getS3Credentials().stream().peek(System.out::println);

    // Create subuser
    SubUser subUser = RGW_ADMIN.createSubUser(userId, "subUserId", SubUser.Permission.FULL, CredentialType.SWIFT);

    // Suspend a user
    RGW_ADMIN.suspendUser(userId, true);

    // Remove a user
    RGW_ADMIN.removeUser(userId);
  }

  /*
   * Quota management
   */
  private static void quota(User user) {
    String userId = user.getUserId();

    // Allow the user owns more buckets
    RGW_ADMIN.modifyUser(userId, ImmutableMap.of("max-buckets", String.valueOf(Integer.MAX_VALUE)));

    // Set the quota that causes the user can have at most one thousand objects and the maximal usage is 1 GiB
    RGW_ADMIN.setUserQuota(userId, 1000, 1048576);
  }

  /*
   * Data management
   */
  private static void store(User user) {
    // Create bucket by the new user
    AmazonS3 s3 = createS3(user.getS3Credentials().get(0));
    String bucketName = "bucket-" + UUID.randomUUID().toString().toLowerCase();
    s3.createBucket(bucketName);

    // Transfer the bucket owner from the user just created to the administrator
    BucketInfo bucketInfo = RGW_ADMIN.getBucketInfo(bucketName).get();
    RGW_ADMIN.linkBucket(bucketName, bucketInfo.getId(), adminUserId);

    // Remove a bucket
    RGW_ADMIN.removeBucket(bucketName);
  }

  /*
   * Usage report
   */
  private static void usage(User user) {
    String userId = user.getUserId();

    // Retrieve and show the usage report for a given user
    UsageInfo userUsage = RGW_ADMIN.getUserUsage(userId).get();
    userUsage.getSummary().stream().peek(System.out::println);
  }

}
