package org.twonote.rgwadmin4j.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.junit.Ignore;
import org.junit.Test;
import org.twonote.rgwadmin4j.model.*;

import java.io.ByteArrayInputStream;
import java.util.*;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class RgwAdminImplTest extends BaseTest {

  @Test
  public void createKey() throws Exception {
    testWithAUser(
        v -> {
          List<Key> response;

          // basic
          response = RGW_ADMIN.createKey(v.getUserId());
          assertEquals(2, response.size());
          assertEquals(2, RGW_ADMIN.getUserInfo(v.getUserId()).get().getKeys().size());

          // specify the key
          String accessKey = v.getUserId() + "-adminAccessKey";
          String secretKey = v.getUserId() + "-adminSecretKey";
          response = RGW_ADMIN.createKey(v.getUserId(), accessKey, secretKey);
          assertTrue(
              response
                  .stream()
                  .anyMatch(
                      v1 ->
                          accessKey.equals(v1.getAccessKey())
                              && secretKey.equals(v1.getSecretKey())));

          // user not exist
          try {
            RGW_ADMIN.createKey(UUID.randomUUID().toString());
            fail();
          } catch (RgwAdminException e) {
            assertEquals("InvalidArgument", e.getMessage());
          }
        });
  }

  @Test
  public void removeKey() throws Exception {
    testWithAUser(
        v -> {
          String accessKey = v.getKeys().get(0).getAccessKey();

          // basic
          RGW_ADMIN.removeKey(v.getUserId(), accessKey);
          assertEquals(0, RGW_ADMIN.getUserInfo(v.getUserId()).get().getKeys().size());

          // key not exist
          try {
            RGW_ADMIN.removeKey(v.getUserId(), UUID.randomUUID().toString());
            fail();
          } catch (RgwAdminException e) {
            assertEquals(
                403, e.status()); // ceph version 11.2.0 (f223e27eeb35991352ebc1f67423d4ebc252adb7)
          }

          // user not exist
          try {
            RGW_ADMIN.removeKey(UUID.randomUUID().toString(), UUID.randomUUID().toString());
            fail();
          } catch (RgwAdminException e) {
            assertEquals(
                400, e.status()); // ceph version 11.2.0 (f223e27eeb35991352ebc1f67423d4ebc252adb7)
          }
        });
  }

  @Test
  public void createKeyForSubUser() throws Exception {
    testWithASubUser(
        v -> {
          List<Key> response;

          // basic
          String absSubUserId = v.getSubusers().get(0).getId(); // In forms of "foo:bar"
          String userId = absSubUserId.split(":")[0];
          String subUserId = absSubUserId.split(":")[1];
          response = RGW_ADMIN.createKeyForSubUser(userId, subUserId);
          assertTrue(response.stream().anyMatch(vv -> absSubUserId.equals(vv.getUser())));

          // specify the key
          String accessKey = v.getUserId() + "-adminAccessKey";
          String secretKey = v.getUserId() + "-adminSecretKey";
          response = RGW_ADMIN.createKeyForSubUser(userId, subUserId, accessKey, secretKey);
          assertTrue(
              response
                  .stream()
                  .anyMatch(
                      v1 ->
                          absSubUserId.equals(v1.getUser())
                              && accessKey.equals(v1.getAccessKey())
                              && secretKey.equals(v1.getSecretKey())));

          // sub user not exist
          // Ceph version 11.2.0 (f223e27eeb35991352ebc1f67423d4ebc252adb7)
          // Create a orphan key without user in
          RGW_ADMIN.createKeyForSubUser(userId, "XXXXXXX");
        });
  }

  @Test
  public void removeKeyFromSubUser() throws Exception {
    testWithASubUser(
        v -> {
          String absSubUserId = v.getSubusers().get(0).getId(); // In forms of "foo:bar"
          String userId = absSubUserId.split(":")[0];
          String subUserId = absSubUserId.split(":")[1];

          List<Key> response = RGW_ADMIN.createKeyForSubUser(userId, subUserId);
          Key keyToDelete =
              response.stream().filter(vv -> absSubUserId.equals(vv.getUser())).findFirst().get();

          // basic
          RGW_ADMIN.removeKeyFromSubUser(userId, subUserId, keyToDelete.getAccessKey());
          assertFalse(
              RGW_ADMIN
                  .getUserInfo(userId)
                  .get()
                  .getKeys()
                  .stream()
                  .anyMatch(
                      k ->
                          keyToDelete
                              .getAccessKey()
                              .equals(k.getAccessKey()))); // Should not contain this key anymore

          // key not exist
          try {
            RGW_ADMIN.removeKeyFromSubUser(userId, subUserId, UUID.randomUUID().toString());
            fail();
          } catch (RgwAdminException e) {
            // ceph version 11.2.0 (f223e27eeb35991352ebc1f67423d4ebc252adb7)
            assertEquals("InvalidAccessKeyId", e.getMessage());
            assertEquals(403, e.status());
          }
        });
  }

  @Test
  public void createSecretForSubUser() throws Exception {
    testWithASubUser(
        v -> {
          List<Key> response;

          // basic
          String absSubUserId = v.getSubusers().get(0).getId(); // In forms of "foo:bar"
          String userId = absSubUserId.split(":")[0];
          String subUserId = absSubUserId.split(":")[1];
          response = RGW_ADMIN.createSecretForSubUser(userId, subUserId);
          assertTrue(response.stream().anyMatch(vv -> absSubUserId.equals(vv.getUser())));

          // specify the key
          String secret = v.getUserId() + "-secret";
          response = RGW_ADMIN.createSecretForSubUser(userId, subUserId, secret);
          assertTrue(
              response
                  .stream()
                  .anyMatch(
                      v1 -> absSubUserId.equals(v1.getUser()) && secret.equals(v1.getSecretKey())));

          // sub user not exist
          // Create a orphan key without user in ceph version 11.2.0 (f223e27eeb35991352ebc1f67423d4ebc252adb7)
          RGW_ADMIN.createSecretForSubUser(userId, subUserId);
        });
  }

  @Test
  public void removeSecretFromSubUser() throws Exception {
    testWithASubUser(
        v -> {
          String absSubUserId = v.getSubusers().get(0).getId(); // In forms of "foo:bar"
          String userId = absSubUserId.split(":")[0];
          String subUserId = absSubUserId.split(":")[1];

          RGW_ADMIN.createSecretForSubUser(userId, subUserId);

          // basic
          RGW_ADMIN.removeSecretFromSubUser(userId, subUserId);
          assertFalse(
              RGW_ADMIN
                  .getUserInfo(userId)
                  .get()
                  .getSwiftKeys()
                  .stream()
                  .anyMatch(
                      k ->
                          absSubUserId.equals(
                              k.getUser()))); // The sub user should not have swift key/secret
        });
  }

  @Test
  @Ignore("See trimUsage()")
  public void trimUserUsage() throws Exception {}

  @Test
  public void trimUsage() throws Exception {
    testWithAUser(
        v -> {
          createSomeObjects(v);

          String userId = v.getUserId();

          UsageInfo response;
          response = RGW_ADMIN.getUserUsage(userId).get();
          if (response.getSummary().stream().noneMatch(vv -> userId.equals(vv.getUser()))) {
            fail("No usage log corresponding to the given user id...need more sleep?");
          }

          RGW_ADMIN.trimUserUsage(userId, null);

          // Usage data are generated in the async way, hope it will be available after wait.
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

          response = RGW_ADMIN.getUserUsage(userId).get();
          if (response.getSummary().stream().anyMatch(vv -> userId.equals(vv.getUser()))) {
            fail("Exist usage log corresponding to the given user id...trim log failed");
          }
        });
  }

  @Test
  @Ignore("See getUsage()")
  public void getUserUsage() throws Exception {}

  @Test
  public void getUsage() throws Exception {
    testWithAUser(
        v -> {
          String userId = v.getUserId();

          // Do something to let usage log generated.
          createSomeObjects(v);

          UsageInfo response;

          response = RGW_ADMIN.getUsage(null).get();
          if (response.getSummary().stream().noneMatch(vv -> userId.equals(vv.getUser()))) {
            fail("No usage log corresponding to the given user id...need more sleep?");
          }

          response = RGW_ADMIN.getUserUsage(userId, null).get();
          if (response.getSummary().stream().noneMatch(vv -> userId.equals(vv.getUser()))) {
            fail("No usage log corresponding to the given user id...need more sleep?");
          }
        });
  }

  @Test
  public void modifySubUser() throws Exception {
    testWithAUser(
        v -> {
          String subUserId = UUID.randomUUID().toString();
          // basic
          List<SubUser> response = RGW_ADMIN.createSubUser(v.getUserId(), subUserId, null);
          assertEquals("<none>", response.get(0).getPermissions());
          response =
              RGW_ADMIN.modifySubUser(
                  v.getUserId(), subUserId, ImmutableMap.of("access", "full"));
          assertEquals("full-control", response.get(0).getPermissions());
        });
  }

  @Test
  public void removeSubUser() throws Exception {
    testWithAUser(
        v -> {
          String subUserId = UUID.randomUUID().toString();
          // basic
          RGW_ADMIN.createSubUserForSwift(v.getUserId(), subUserId);
          User response2 = RGW_ADMIN.getUserInfo(v.getUserId()).get();
          assertEquals(1, response2.getSwiftKeys().size());
          RGW_ADMIN.removeSubUser(v.getUserId(), subUserId);
          response2 = RGW_ADMIN.getUserInfo(v.getUserId()).get();
          assertEquals(0, response2.getSwiftKeys().size());
        });
  }

  @Ignore("Works in v11.2.0-kraken or above.")
  @Test
  public void createSubUser() throws Exception {
    testWithAUser(
        v -> {
          String subUserId = UUID.randomUUID().toString();
          // basic
          List<SubUser> response =
              RGW_ADMIN.createSubUser(
                  v.getUserId(), subUserId, ImmutableMap.of("key-type", "s3", "access", "full"));
          assertEquals(1, response.size());
          String fullSubUserId = v.getUserId() + ":" + subUserId;
          assertEquals(fullSubUserId, response.get(0).getId());
          assertEquals("full-control", response.get(0).getPermissions());

          // exist in get user info response
          User response2 = RGW_ADMIN.getUserInfo(v.getUserId()).get();
          assertEquals(fullSubUserId, response2.getSubusers().get(0).getId());

          // test subuser in s3
          Key key =
              response2
                  .getKeys()
                  .stream()
                  .filter(e -> fullSubUserId.equals(e.getUser()))
                  .findFirst()
                  .get();
          AmazonS3 s3 = createS3(key.getAccessKey(), key.getSecretKey());
          s3.listBuckets();
          String bucketName = UUID.randomUUID().toString().toLowerCase();
          s3.createBucket(bucketName);
          s3.putObject(bucketName, "qqq", "qqqq");
          s3.listObjects(bucketName);
          s3.getObject(bucketName, "qqq");
        });
  }

  @Test
  public void createSubUserForSwift() throws Exception {
    testWithAUser(
        v -> {
          String subUserId = UUID.randomUUID().toString();
          // basic
          List<SubUser> response = RGW_ADMIN.createSubUserForSwift(v.getUserId(), subUserId);
          assertEquals(1, response.size());
          assertEquals(v.getUserId() + ":" + subUserId, response.get(0).getId());
          assertEquals("full-control", response.get(0).getPermissions());

          // test subuser in swift
          User response2 = RGW_ADMIN.getUserInfo(v.getUserId()).get();
          String username = response2.getSwiftKeys().get(0).getUser();
          String password = response2.getSwiftKeys().get(0).getSecretKey();
          testSwiftConnectivity(username, password);
        });
  }

  @Test
  public void checkBucketIndex() throws Exception {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          AmazonS3 s3 =
              createS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey());
          String bucketName = userId.toLowerCase();

          // not exist
          RGW_ADMIN.checkBucketIndex(bucketName, true, true);

          s3.createBucket(bucketName);
          // Do not know how to check the behavior...
          Optional result = RGW_ADMIN.checkBucketIndex(bucketName, true, true);
          assertTrue(result.isPresent());
        });
  }

  @Ignore("See getAndSetUserQuota()")
  @Test
  public void getUserQuota() throws Exception {}

  @Ignore("See getAndSetUserQuota()")
  @Test
  public void setUserQuota() throws Exception {}

  @Test
  @Ignore("See userCapability()")
  public void addUserCapability() throws Exception {}

  @Test
  public void userCapability() throws Exception {
    testWithASubUser(
        v -> {
          String userId = v.getUserId();
          List<Cap> userCaps =
              Arrays.asList(
                  new Cap(Cap.Type.USAGE, Cap.Perm.READ_WRITE),
                  new Cap(Cap.Type.USERS, Cap.Perm.WRITE));
          List<Cap> retUserCaps;

          // add
          retUserCaps = RGW_ADMIN.addUserCapability(userId, userCaps);
          assertEquals(userCaps, retUserCaps);

          // remove
          List<Cap> toRemove = userCaps.subList(0, 1);
          List<Cap> toRemain = userCaps.subList(1, 2);
          retUserCaps = RGW_ADMIN.removeUserCapability(userId, toRemove);
          assertEquals(toRemain, retUserCaps);
        });
  }

  @Test
  @Ignore("See userCapability()")
  public void removeUserCapability() throws Exception {}

  @Test
  public void removeBucket() throws Exception {
    String bucketName = "testremovebkbk" + UUID.randomUUID().toString();

    // remove bucket not exist
    Thread.sleep(3000);
    RGW_ADMIN.removeBucket(bucketName);

    testWithAUser(
        v -> {
          String userId = "testremovebk" + UUID.randomUUID().toString();

          User response = RGW_ADMIN.createUser(userId);
          AmazonS3 s3 =
              createS3(
                  response.getKeys().get(0).getAccessKey(),
                  response.getKeys().get(0).getSecretKey());
          s3.createBucket(bucketName);

          ByteArrayInputStream input = new ByteArrayInputStream("Hello World!".getBytes());
          s3.putObject(bucketName, "hello.txt", input, new ObjectMetadata());

          RGW_ADMIN.removeBucket(bucketName);

          try {
            s3.headBucket(new HeadBucketRequest(bucketName));
            fail();
          } catch (Exception e) {
            assertTrue("Not Found".equals(((AmazonS3Exception) e).getErrorMessage()));
          }
        });
  }

  @Test
  public void unlinkBucket() throws Exception {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          AmazonS3 s3 =
              createS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey());
          String bucketName = userId.toLowerCase();

          // not exist
          RGW_ADMIN.unlinkBucket(bucketName, userId);

          s3.createBucket(bucketName);

          // basic
          RGW_ADMIN.unlinkBucket(bucketName, userId);
          assertEquals(0, s3.listBuckets().size());

          // head is ok...
          s3.headBucket(new HeadBucketRequest(bucketName));

          // again
          RGW_ADMIN.unlinkBucket(bucketName, userId);
        });
  }

  @Test
  public void linkBucket() throws Exception {
    testWithAUser(
        v -> {
          String userId = "linkbkusr" + UUID.randomUUID().toString();
          String bucketName = "linkbkusrbk" + UUID.randomUUID().toString();
          User response = RGW_ADMIN.createUser(userId);
          AmazonS3 s3 =
              createS3(
                  response.getKeys().get(0).getAccessKey(),
                  response.getKeys().get(0).getSecretKey());
          s3.createBucket(bucketName);

          BucketInfo _response = RGW_ADMIN.getBucketInfo(bucketName).get();

          // basic
          String bucketId = _response.getId();
          RGW_ADMIN.linkBucket(bucketName, bucketId, adminUserId);
          BucketInfo __response = RGW_ADMIN.getBucketInfo(bucketName).get();
          assertEquals(adminUserId, __response.getOwner());

          // execute again
          // Ceph 9.2.x throw exception; ceph 10.2.2 returns 404 so no exception will show.
          //            exception.expect(RuntimeException.class);
          RGW_ADMIN.linkBucket(bucketName, bucketId, adminUserId);

          // bad argument
          //            exception.expect(RuntimeException.class);
          RGW_ADMIN.linkBucket(bucketName + "qq", bucketId, adminUserId);

          //            exception.expect(RuntimeException.class);
          RGW_ADMIN.linkBucket(bucketName, bucketId, adminUserId + "qqq");

          //            exception.expect(RuntimeException.class);
          RGW_ADMIN.linkBucket(bucketName, bucketId + "qq", adminUserId);
        });
  }

  @Test
  public void listBucketInfo() throws Exception {
    testWithASubUser(
        v -> {
          AmazonS3 s3 =
              createS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey());
          for (int i = 0; i < 3; i++) {
            s3.createBucket(UUID.randomUUID().toString().toLowerCase());
          }
          List<BucketInfo> response = RGW_ADMIN.listBucketInfo(v.getUserId());
          assertEquals(3, response.size());
        });
  }

  @Test
  public void listBucket() throws Exception {
    testWithASubUser(
        v -> {
          AmazonS3 s3 =
              createS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey());
          for (int i = 0; i < 3; i++) {
            s3.createBucket(UUID.randomUUID().toString().toLowerCase());
          }
          List<String> response = RGW_ADMIN.listBucket(v.getUserId());
          assertEquals(3, response.size());
        });
  }

  @Test
  public void getBucketInfo() throws Exception {
    testWithASubUser(
        v -> {
          AmazonS3 s3 =
              createS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey());
          String bucketName = UUID.randomUUID().toString().toLowerCase();
          s3.createBucket(bucketName);

          Optional<BucketInfo> response = RGW_ADMIN.getBucketInfo(bucketName);
          assertTrue(response.isPresent());
        });
  }

  @Test
  public void modifyUser() throws Exception {
    String userId = "testModifyUserId";
    RGW_ADMIN.createUser(userId);

    // basic
    RGW_ADMIN.modifyUser(
        userId, ImmutableMap.of("max-buckets", String.valueOf(Integer.MAX_VALUE)));
    User response = RGW_ADMIN.getUserInfo(userId).get();
    assertEquals(Integer.valueOf(Integer.MAX_VALUE), response.getMaxBuckets());

    // user not exist
    RGW_ADMIN.modifyUser(
        userId + "qqqq", ImmutableMap.of("max-buckets", String.valueOf(Integer.MAX_VALUE)));

    // ignore call with wrong arguments
    RGW_ADMIN.modifyUser(
        userId, ImmutableMap.of("QQQQQ", String.valueOf(Integer.MAX_VALUE)));
    RGW_ADMIN.modifyUser(userId, ImmutableMap.of("max-buckets", "you-know-my-name"));
    assertEquals(Integer.valueOf(Integer.MAX_VALUE), response.getMaxBuckets());
  }

  @Test
  public void removeUser() throws Exception {
    // The operation is success if the user is not exist in the system after the operation is executed.
    String userId = "testRemoveUserId";
    RGW_ADMIN.createUser(userId);
    RGW_ADMIN.removeUser(userId);
    assertFalse(RGW_ADMIN.getUserInfo(userId).isPresent());

    // The operation does not throw exception even if the user is not exist in the beginning.
    RGW_ADMIN.removeUser(userId);
  }

  @Test
  public void createUser() throws Exception {
    String userId = "bobx" + UUID.randomUUID().toString();
    try {
      // basic
      User response = RGW_ADMIN.createUser(userId);
      assertEquals(userId, response.getUserId());
      assertNotNull(response.getKeys().get(0).getAccessKey());
      assertNotNull(response.getKeys().get(0).getSecretKey());
      assertEquals(Integer.valueOf(0), response.getSuspended());
      assertEquals(Integer.valueOf(1000), response.getMaxBuckets());

      // create exist one should act like modification
      response = RGW_ADMIN.createUser(userId, ImmutableMap.of("max-buckets", "1"));
      assertEquals(Integer.valueOf(1), response.getMaxBuckets());

    } finally {
      RGW_ADMIN.removeUser(userId);
    }
  }

  @Test
  public void getUserInfo() throws Exception {
    // basic
    User response = RGW_ADMIN.getUserInfo(adminUserId).get();
    assertEquals(Integer.valueOf(0), response.getSuspended());
    assertEquals(adminUserId, response.getUserId());
    List<Cap> caps =
        Arrays.asList(
            new Cap(Cap.Type.USERS, Cap.Perm.READ_WRITE),
            new Cap(Cap.Type.BUCKETS, Cap.Perm.READ_WRITE));
    assertTrue(response.getCaps().containsAll(caps));

    // not exist
    assertFalse(RGW_ADMIN.getUserInfo(UUID.randomUUID().toString()).isPresent());
  }

  @Test
  public void suspendUser() throws Exception {
    testWithASubUser(
        v -> {
          String userId = v.getUserId();
          User response;

          // suspend
          RGW_ADMIN.suspendUser(userId, true);
          response = RGW_ADMIN.getUserInfo(userId).get();
          assertEquals(Integer.valueOf(1), response.getSuspended());

          // resume
          RGW_ADMIN.suspendUser(userId, false);
          response = RGW_ADMIN.getUserInfo(userId).get();
          assertEquals(Integer.valueOf(0), response.getSuspended());
        });
  }

  @Test
  public void userQuotaMaxObjects() throws Exception {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          Quota quota;

          // max object = 2
          RGW_ADMIN.setUserQuota(userId, 2, -1);
          quota = RGW_ADMIN.getUserQuota(userId).get();
          assertEquals(true, quota.getEnabled());

          AmazonS3 s3 =
              createS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey());
          String bucketName = userId.toLowerCase();
          s3.createBucket(bucketName);

          // allow 1st,2ed obj
          s3.putObject(bucketName, userId + "1", "qqqq");
          s3.putObject(bucketName, userId + "2", "qqqq");

          // deny 3rd obj
          try {
            s3.putObject(bucketName, userId + "3", "qqqq");
            fail();
          } catch (AmazonS3Exception e) {
            assertEquals("QuotaExceeded", e.getErrorCode());
          }
        });
  }

  /*
   * radosgw implementation note:
   * The behavior of the quota evaluation is taking effect in the unit of 4KiB, i.e., isExceed = ( ceil(TO_USED_SIZE_IN_BYTE/4096) > floor(maxSize/4) ? )
   * For example, when mazSize is 5, put a object with 4096 bytes will be ok, but put with 4096 + 1 bytes will be blocked.
   * (Assume that the used size is 0 before taking the action.)
   */
  @Test
  public void userQuotaMaxSize() throws Exception {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          Quota quota;

          // max size = 6 bytes
          RGW_ADMIN.setUserQuota(userId, -1, 12);
          quota = RGW_ADMIN.getUserQuota(userId).get();
          assertEquals(true, quota.getEnabled());

          AmazonS3 s3 =
              createS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey());
          String bucketName = userId.toLowerCase();
          s3.createBucket(bucketName);

          // ok, ok, ok, since the total to used size exceed 12KiB
          s3.putObject(bucketName, userId + "1", createString(4096));
          s3.putObject(bucketName, userId + "2", createString(4096));
          s3.putObject(bucketName, userId + "3", createString(4096));

          // not ok, since the total to used size exceed 12KiB +1
          try {
            s3.putObject(bucketName, userId + "4", createString(1));
            fail();
          } catch (AmazonS3Exception e) {
            assertEquals("QuotaExceeded", e.getErrorCode());
          }
        });
  }

  @Test
  public void getAndSetUserQuota() throws Exception {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          Quota quota;

          // default false
          quota = RGW_ADMIN.getUserQuota(userId).get();
          assertEquals(false, quota.getEnabled());
          assertEquals(Long.valueOf(-1), quota.getMaxObjects());
          assertTrue(
              quota.getMaxSizeKb() == -1 // jewel
                  || quota.getMaxSizeKb() == 0 // kraken
              );

          // set quota
          RGW_ADMIN.setUserQuota(userId, 1, 1);
          quota = RGW_ADMIN.getUserQuota(userId).get();
          assertEquals(true, quota.getEnabled());
          assertEquals(Long.valueOf(1), quota.getMaxObjects());
          assertEquals(Long.valueOf(1), quota.getMaxSizeKb());
        });

    // not exist
    try {
      RGW_ADMIN.getUserQuota(UUID.randomUUID().toString());
      fail();
    } catch (RgwAdminException e) {
      assertEquals(400, e.status());
      assertEquals("InvalidArgument", e.getMessage());
    }

    RGW_ADMIN.setUserQuota(UUID.randomUUID().toString(), 1, 1);
  }

  @Test
  public void getObjectPolicy() throws Exception {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          AmazonS3 s3 =
              createS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey());
          String bucketName = userId.toLowerCase();
          String objectKey = userId.toLowerCase();
          s3.createBucket(bucketName);
          s3.putObject(bucketName, objectKey, "qqq");
          String resp = RGW_ADMIN.getObjectPolicy(bucketName, objectKey).get();
          assertFalse(Strings.isNullOrEmpty(resp));
        });
  }

  @Test
  public void getBucketPolicy() throws Exception {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          AmazonS3 s3 =
              createS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey());
          String bucketName = userId.toLowerCase();
          s3.createBucket(bucketName);
          String resp = RGW_ADMIN.getBucketPolicy(bucketName).get();
          assertFalse(Strings.isNullOrEmpty(resp));
        });
  }

  @Test
  public void removeObject() throws Exception {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          AmazonS3 s3 =
              createS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey());
          String bucketName = userId.toLowerCase();
          s3.createBucket(bucketName);
          String objectKey = userId.toLowerCase();
          s3.putObject(bucketName, objectKey, "qqq");

          // basic
          RGW_ADMIN.removeObject(bucketName, objectKey);
          try {
            s3.getObjectMetadata(bucketName, objectKey);
            fail();
          } catch (AmazonS3Exception e) {
            assertEquals(404, e.getStatusCode());
          }

          // not exist
          RGW_ADMIN.removeObject(bucketName, objectKey);
        });
  }
}