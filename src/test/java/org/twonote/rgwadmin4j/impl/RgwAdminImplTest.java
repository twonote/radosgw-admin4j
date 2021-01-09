package org.twonote.rgwadmin4j.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Ignore;
import org.junit.Test;
import org.twonote.rgwadmin4j.model.BucketInfo;
import org.twonote.rgwadmin4j.model.Cap;
import org.twonote.rgwadmin4j.model.CredentialType;
import org.twonote.rgwadmin4j.model.Quota;
import org.twonote.rgwadmin4j.model.S3Credential;
import org.twonote.rgwadmin4j.model.SubUser;
import org.twonote.rgwadmin4j.model.SwiftCredential;
import org.twonote.rgwadmin4j.model.UsageInfo;
import org.twonote.rgwadmin4j.model.User;

public class RgwAdminImplTest extends BaseTest {

  @Test
  public void listSubUser() {
    testWithASubUser(
        s -> {
          List<String> subUserIds = RGW_ADMIN.listSubUser(s.getUserId());
          assertEquals(
              subUserIds,
              s.getSubusers().stream().map(SubUser::getId).collect(Collectors.toList()));
        });
  }

  @Test
  public void listUserInfo() {
    testWithAUser(
        u -> {
          List<User> users = RGW_ADMIN.listUserInfo();
          assertTrue(users.stream().anyMatch(u::equals));
        });
  }

  @Test
  public void listUser() {
    testWithAUser(
        u -> {
          List<String> userIds = RGW_ADMIN.listUser();
          assertTrue(userIds.stream().anyMatch(k -> u.getUserId().equals(k)));
        });
  }

  @Test
  public void listSubUserInfo() {
    testWithASubUser(
        u -> {
          List<SubUser> subUsers = RGW_ADMIN.listSubUserInfo(u.getUserId());
          assertEquals(subUsers, u.getSubusers());
        });
  }

  @Test
  public void getSubUserInfo() {
    testWithASubUser(
        u -> {
          Optional<SubUser> subUserInfo =
              RGW_ADMIN.getSubUserInfo(
                  u.getUserId(), u.getSubusers().get(0).getRelativeSubUserId());
          assertEquals(u.getSubusers().get(0), subUserInfo.get());
        });
  }

  @Test
  public void createS3Credential() {
    testWithAUser(
        v -> {
          List<S3Credential> response;

          // basic
          response = RGW_ADMIN.createS3Credential(v.getUserId());
          assertEquals(2, response.size());
          assertEquals(2, RGW_ADMIN.getUserInfo(v.getUserId()).get().getS3Credentials().size());

          // specify the key
          String accessKey = v.getUserId() + "-adminAccessKey";
          String secretKey = v.getUserId() + "-adminSecretKey";
          response = RGW_ADMIN.createS3Credential(v.getUserId(), accessKey, secretKey);
          assertTrue(
              response
                  .stream()
                  .anyMatch(
                      v1 ->
                          accessKey.equals(v1.getAccessKey())
                              && secretKey.equals(v1.getSecretKey())));

          // user not exist
          try {
            RGW_ADMIN.createS3Credential(UUID.randomUUID().toString());
            fail("user not exist should throw exception");
          } catch (RgwAdminException e) {
            assertTrue(
                "InvalidArgument".equals(e.getMessage()) // kraken
                    || "NoSuchUser".equals(e.getMessage()) // luminous
            );
          }
        });
  }

  @Test
  public void removeS3Credential() {
    testWithAUser(
        v -> {
          String accessKey = v.getS3Credentials().get(0).getAccessKey();

          // basic
          RGW_ADMIN.removeS3Credential(v.getUserId(), accessKey);
          assertEquals(0, RGW_ADMIN.getUserInfo(v.getUserId()).get().getS3Credentials().size());

          // key not exist
          try {
            RGW_ADMIN.removeS3Credential(v.getUserId(), UUID.randomUUID().toString());
            fail();
          } catch (RgwAdminException e) {
            assertEquals(
                403, e.status()); // ceph version 11.2.0 (f223e27eeb35991352ebc1f67423d4ebc252adb7)
          }

          // user not exist
          try {
            RGW_ADMIN.removeS3Credential(
                UUID.randomUUID().toString(), UUID.randomUUID().toString());
            fail("user not exist should throw exception");
          } catch (RgwAdminException e) {
            assertTrue(
                "InvalidArgument".equals(e.getMessage())
                    || // kraken
                    "NoSuchUser".equals(e.getMessage()) // luminous
            );
          }
        });
  }

  @Test
  public void createS3CredentialForSubUser() {
    testWithASubUser(
        v -> {
          List<S3Credential> response;

          // basic
          String absSubUserId = v.getSubusers().get(0).getId(); // In forms of "foo:bar"
          String userId = absSubUserId.split(":")[0];
          String subUserId = absSubUserId.split(":")[1];
          response = RGW_ADMIN.createS3CredentialForSubUser(userId, subUserId);
          assertTrue(response.stream().anyMatch(vv -> absSubUserId.equals(vv.getUserId())));

          // specify the key
          String accessKey = v.getUserId() + "-adminAccessKey";
          String secretKey = v.getUserId() + "-adminSecretKey";
          response =
              RGW_ADMIN.createS3CredentialForSubUser(userId, subUserId, accessKey, secretKey);
          assertTrue(
              response
                  .stream()
                  .anyMatch(
                      v1 ->
                          absSubUserId.equals(v1.getUserId())
                              && accessKey.equals(v1.getAccessKey())
                              && secretKey.equals(v1.getSecretKey())));

          // sub user not exist
          // Ceph version 11.2.0 (f223e27eeb35991352ebc1f67423d4ebc252adb7)
          // Create a orphan key without user in
          RGW_ADMIN.createS3CredentialForSubUser(userId, "XXXXXXX");
        });
  }

  @Test
  public void removeS3CredentialFromSubUser() {
    testWithASubUser(
        v -> {
          String absSubUserId = v.getSubusers().get(0).getId(); // In forms of "foo:bar"
          String userId = absSubUserId.split(":")[0];
          String subUserId = absSubUserId.split(":")[1];

          List<S3Credential> response = RGW_ADMIN.createS3CredentialForSubUser(userId, subUserId);
          S3Credential keyToDelete =
              response.stream().filter(vv -> absSubUserId.equals(vv.getUserId())).findFirst().get();

          // basic
          RGW_ADMIN.removeS3CredentialFromSubUser(userId, subUserId, keyToDelete.getAccessKey());
          assertFalse(
              RGW_ADMIN
                  .getUserInfo(userId)
                  .get()
                  .getS3Credentials()
                  .stream()
                  .anyMatch(
                      k ->
                          keyToDelete
                              .getAccessKey()
                              .equals(k.getAccessKey()))); // Should not contain this key anymore

          // key not exist
          try {
            RGW_ADMIN.removeS3CredentialFromSubUser(
                userId, subUserId, UUID.randomUUID().toString());
            fail();
          } catch (RgwAdminException e) {
            // ceph version 11.2.0 (f223e27eeb35991352ebc1f67423d4ebc252adb7)
            assertEquals("InvalidAccessKeyId", e.getMessage());
            assertEquals(403, e.status());
          }
        });
  }

  @Test
  public void createSwiftCredentialForSubUser() {
    testWithASubUser(
        v -> {
          SwiftCredential swiftCredential;

          // basic
          String absSubUserId = v.getSubusers().get(0).getId(); // In forms of "foo:bar"
          String userId = absSubUserId.split(":")[0];
          String subUserId = absSubUserId.split(":")[1];
          swiftCredential = RGW_ADMIN.createSwiftCredentialForSubUser(userId, subUserId);
          assertTrue(absSubUserId.equals(swiftCredential.getUserId()));
          assertNotNull(swiftCredential.getUsername());
          assertNotNull(swiftCredential.getPassword());

          // specify the key
          String password = v.getUserId() + "-secret";
          swiftCredential = RGW_ADMIN.createSwiftCredentialForSubUser(userId, subUserId, password);
          assertTrue(absSubUserId.equals(swiftCredential.getUserId()));
          assertNotNull(swiftCredential.getUsername());
          assertNotNull(swiftCredential.getPassword());
          assertEquals(password, swiftCredential.getPassword());

          // sub user not exist
          // Create a orphan key without user in ceph version 11.2.0
          // (f223e27eeb35991352ebc1f67423d4ebc252adb7)
          RGW_ADMIN.createSwiftCredentialForSubUser(userId, subUserId);
        });
  }

  @Test
  public void removeSwiftCredentialFromSubUser() {
    testWithASubUser(
        v -> {
          String absSubUserId = v.getSubusers().get(0).getId(); // In forms of "foo:bar"
          String userId = absSubUserId.split(":")[0];
          String subUserId = absSubUserId.split(":")[1];

          RGW_ADMIN.createSwiftCredentialForSubUser(userId, subUserId);

          // basic
          RGW_ADMIN.removeSwiftCredentialFromSubUser(userId, subUserId);
          assertFalse(
              RGW_ADMIN
                  .getUserInfo(userId)
                  .get()
                  .getSwiftCredentials()
                  .stream()
                  .anyMatch(
                      k ->
                          absSubUserId.equals(
                              k.getUserId()))); // The sub user should not have swift key/secret
        });
  }

  @Test
  @Ignore("See trimUsage()")
  public void trimUserUsage() {
  }

  @Test
  public void trimUsage() {
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
  public void getUserUsage() {
  }

  @Test
  public void getUsage() {
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
  public void setSubUserPermission() {
    testWithASubUser(
        su -> {
          SubUser subUser = su.getSubusers().get(0);

          for (SubUser.Permission permissionToSet : SubUser.Permission.values()) {
            List<SubUser> response =
                RGW_ADMIN.setSubUserPermission(
                    subUser.getParentUserId(), subUser.getRelativeSubUserId(), permissionToSet);

            SubUser result =
                response.stream().filter(r -> subUser.getId().equals(r.getId())).findFirst().get();
            assertEquals(permissionToSet, result.getPermission());
          }
        });
  }

  @Test
  public void modifySubUser() {
    testWithAUser(
        v -> {
          String subUserId = UUID.randomUUID().toString();
          // basic
          List<SubUser> response = RGW_ADMIN.createSubUser(v.getUserId(), subUserId, null);
          assertEquals(SubUser.Permission.NONE, response.get(0).getPermission());
          response =
              RGW_ADMIN.modifySubUser(v.getUserId(), subUserId, ImmutableMap.of("access", "full"));
          assertEquals(SubUser.Permission.FULL, response.get(0).getPermission());
        });
  }

  @Test
  public void removeSubUser() {
    testWithAUser(
        v -> {
          String subUserId = UUID.randomUUID().toString();
          // basic
          RGW_ADMIN.createSubUser(
              v.getUserId(), subUserId, SubUser.Permission.FULL, CredentialType.SWIFT);
          User response2 = RGW_ADMIN.getUserInfo(v.getUserId()).get();
          assertEquals(1, response2.getSwiftCredentials().size());
          RGW_ADMIN.removeSubUser(v.getUserId(), subUserId);
          response2 = RGW_ADMIN.getUserInfo(v.getUserId()).get();
          assertEquals(0, response2.getSwiftCredentials().size());
        });
  }

  @Test
  public void createSubUser() {
    testWithAUser(
        u -> {
          String userId = u.getUserId();
          String subUserId = "SUB-" + UUID.randomUUID().toString();
          String absSubUserId = String.join(":", userId, subUserId);

          // basic
          SubUser.Permission permission = SubUser.Permission.FULL;
          SubUser response =
              RGW_ADMIN.createSubUser(userId, subUserId, permission, CredentialType.SWIFT);
          assertEquals(permission, response.getPermission());
          Optional<SwiftCredential> keyResponse =
              RGW_ADMIN
                  .getUserInfo(userId)
                  .get()
                  .getSwiftCredentials()
                  .stream()
                  .filter(k -> absSubUserId.equals(k.getUserId()))
                  .findFirst();
          assertTrue(keyResponse.isPresent());
        });
  }

  @Ignore("Works in v11.2.0-kraken or above.")
  @Test
  public void _createSubUser() {
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
          assertEquals(SubUser.Permission.FULL, response.get(0).getPermission());

          // exist in get user info response
          User response2 = RGW_ADMIN.getUserInfo(v.getUserId()).get();
          assertEquals(fullSubUserId, response2.getSubusers().get(0).getId());

          // test subuser in s3
          S3Credential key =
              response2
                  .getS3Credentials()
                  .stream()
                  .filter(e -> fullSubUserId.equals(e.getUserId()))
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
  public void checkBucketIndex() {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          AmazonS3 s3 =
              createS3(
                  v.getS3Credentials().get(0).getAccessKey(),
                  v.getS3Credentials().get(0).getSecretKey());
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
  public void getUserQuota() {
  }

  @Ignore("See getAndSetUserQuota()")
  @Test
  public void setUserQuota() {
  }

  @Ignore("See getAndSetBucketQuota()")
  @Test
  public void getBucketQuota() {
  }

  @Ignore("See getAndSetBucketQuota()")
  @Test
  public void setBucketQuota() {
  }

  @Test
  @Ignore("See userCapability()")
  public void addUserCapability() {
  }

  @Test
  public void userCapability() {
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
  public void removeUserCapability() {
  }

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
                  response.getS3Credentials().get(0).getAccessKey(),
                  response.getS3Credentials().get(0).getSecretKey());
          s3.createBucket(bucketName);

          ByteArrayInputStream input = new ByteArrayInputStream("Hello World!".getBytes());
          s3.putObject(bucketName, "hello.txt", input, new ObjectMetadata());

          RGW_ADMIN.removeBucket(bucketName);

          try {
            s3.headBucket(new HeadBucketRequest(bucketName));
            fail();
          } catch (Exception e) {
            assertEquals("Not Found", ((AmazonS3Exception) e).getErrorMessage());
          }
        });
  }

  @Test
  public void unlinkBucket() {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          AmazonS3 s3 =
              createS3(
                  v.getS3Credentials().get(0).getAccessKey(),
                  v.getS3Credentials().get(0).getSecretKey());
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
  public void linkBucket() {
    testWithAUser(
        v -> {
          String userId = "linkbkusr" + UUID.randomUUID().toString();
          String bucketName = "linkbkusrbk" + UUID.randomUUID().toString();
          User response = RGW_ADMIN.createUser(userId);
          AmazonS3 s3 =
              createS3(
                  response.getS3Credentials().get(0).getAccessKey(),
                  response.getS3Credentials().get(0).getSecretKey());
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

          try {
            RGW_ADMIN.linkBucket(bucketName, bucketId + "qq", adminUserId);
          } catch (RgwAdminException e) {
            assertEquals("InvalidArgument", e.getMessage());
          }
        });
  }

  @Test
  public void listBucketInfo() {
    testWithASubUser(
        v -> {
          AmazonS3 s3 =
              createS3(
                  v.getS3Credentials().get(0).getAccessKey(),
                  v.getS3Credentials().get(0).getSecretKey());
          for (int i = 0; i < 3; i++) {
            s3.createBucket(UUID.randomUUID().toString().toLowerCase());
          }

          List<BucketInfo> response;

          // all buckets
          response = RGW_ADMIN.listBucketInfo();
          assertTrue(response.size() >= 3);

          // bucket belong to the user
          response = RGW_ADMIN.listBucketInfo(v.getUserId());
          assertEquals(3, response.size());
        });
  }

  @Test
  public void listBucket() {
    testWithASubUser(
        v -> {
          AmazonS3 s3 =
              createS3(
                  v.getS3Credentials().get(0).getAccessKey(),
                  v.getS3Credentials().get(0).getSecretKey());
          for (int i = 0; i < 3; i++) {
            s3.createBucket(UUID.randomUUID().toString().toLowerCase());
          }

          List<String> response;

          // all buckets
          response = RGW_ADMIN.listBucket();
          assertTrue(response.size() >= 3);

          // bucket belong to the user
          response = RGW_ADMIN.listBucket(v.getUserId());
          assertEquals(3, response.size());
        });
  }

  @Test
  public void getBucketInfo() {
    testWithASubUser(
        v -> {
          AmazonS3 s3 =
              createS3(
                  v.getS3Credentials().get(0).getAccessKey(),
                  v.getS3Credentials().get(0).getSecretKey());
          String bucketName = UUID.randomUUID().toString().toLowerCase();
          s3.createBucket(bucketName);

          Optional<BucketInfo> response = RGW_ADMIN.getBucketInfo(bucketName);
          assertTrue(response.isPresent());
        });
  }

  @Test
  public void modifyUser() {
    String userId = "testModifyUserId" + UUID.randomUUID().toString();
    RGW_ADMIN.createUser(userId);

    // basic
    RGW_ADMIN.modifyUser(userId, ImmutableMap.of("max-buckets", String.valueOf(Integer.MAX_VALUE)));
    User response = RGW_ADMIN.getUserInfo(userId).get();
    assertEquals(Integer.valueOf(Integer.MAX_VALUE), response.getMaxBuckets());

    // user not exist
    RGW_ADMIN.modifyUser(
        userId + "qqqq", ImmutableMap.of("max-buckets", String.valueOf(Integer.MAX_VALUE)));

    // ignore call with wrong arguments
    RGW_ADMIN.modifyUser(userId, ImmutableMap.of("QQQQQ", String.valueOf(Integer.MAX_VALUE)));
    RGW_ADMIN.modifyUser(userId, ImmutableMap.of("max-buckets", "you-know-my-name"));
    assertEquals(Integer.valueOf(Integer.MAX_VALUE), response.getMaxBuckets());
  }

  @Test
  public void removeUser() {
    // The operation is success if the user is not exist in the system after the operation is
    // executed.
    String userId = "testRemoveUserId";
    RGW_ADMIN.createUser(userId);
    RGW_ADMIN.removeUser(userId);
    assertFalse(RGW_ADMIN.getUserInfo(userId).isPresent());

    // The operation does not throw exception even if the user is not exist in the beginning.
    RGW_ADMIN.removeUser(userId);
  }

  @Test
  public void createUser() {
    String userId = "bobx" + UUID.randomUUID().toString();
    try {
      // basic
      User response = RGW_ADMIN.createUser(userId);
      assertEquals(userId, response.getUserId());
      assertNotNull(response.getS3Credentials().get(0).getAccessKey());
      assertNotNull(response.getS3Credentials().get(0).getSecretKey());
      assertEquals(Integer.valueOf(0), response.getSuspended());
      assertEquals(Integer.valueOf(1000), response.getMaxBuckets());

      // create exist one should act like modification
      try {
        RGW_ADMIN.createUser(userId);
      } catch (RgwAdminException e) {
        assertEquals("UserAlreadyExists", e.getMessage());
      }

    } finally {
      RGW_ADMIN.removeUser(userId);
    }
  }

  @Test
  public void getUserInfo() {
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
  public void suspendUser() {
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
  public void userQuotaMaxObjects() {
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
                  v.getS3Credentials().get(0).getAccessKey(),
                  v.getS3Credentials().get(0).getSecretKey());
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
  public void userQuotaMaxSize() {
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
                  v.getS3Credentials().get(0).getAccessKey(),
                  v.getS3Credentials().get(0).getSecretKey());
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
  public void getAndSetUserQuota() {
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

    // user not exist
    try {
      RGW_ADMIN.getUserQuota(UUID.randomUUID().toString());
      fail("user not exist should throw exception");
    } catch (RgwAdminException e) {
      assertTrue(
          "InvalidArgument".equals(e.getMessage())
              || // kraken
              "NoSuchUser".equals(e.getMessage()) // luminous
      );
    }

    RGW_ADMIN.setUserQuota(UUID.randomUUID().toString(), 1, 1);
  }

  @Test
  public void getAndSetBucketQuota() {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();

          AmazonS3 s3 =
              createS3(
                  v.getS3Credentials().get(0).getAccessKey(),
                  v.getS3Credentials().get(0).getSecretKey());
          String bucketName = userId.toLowerCase();
          s3.createBucket(bucketName);

          Quota quota;

          // default false
          quota = RGW_ADMIN.getBucketQuota(userId).get();
          assertEquals(false, quota.getEnabled());
          assertEquals(Long.valueOf(-1), quota.getMaxObjects());
          assertTrue(
              quota.getMaxSizeKb() == -1 // jewel
                  || quota.getMaxSizeKb() == 0 // kraken
          );

          // set quota
          RGW_ADMIN.setBucketQuota(userId, 1, 1);

          // shown by getBucketQuota()
          quota = RGW_ADMIN.getBucketQuota(userId).get();
          assertEquals(true, quota.getEnabled());
          assertEquals(Long.valueOf(1), quota.getMaxObjects());
          assertEquals(Long.valueOf(1), quota.getMaxSizeKb());

          // not shown by getBucketInfo()
          quota = RGW_ADMIN.getBucketInfo(bucketName).get().getBucketQuota();
          assertEquals(false, quota.getEnabled());
          assertEquals(Long.valueOf(-1), quota.getMaxObjects());
          assertTrue(
              quota.getMaxSizeKb() == -1 // jewel
                  || quota.getMaxSizeKb() == 0 // kraken
          );
        });

    // user not exist
    try {
      RGW_ADMIN.getBucketQuota(UUID.randomUUID().toString());
      fail("user not exist should throw exception");
    } catch (RgwAdminException e) {
      assertTrue(
          "InvalidArgument".equals(e.getMessage())
              || // kraken
              "NoSuchUser".equals(e.getMessage()) // luminous
      );
    }

    RGW_ADMIN.setBucketQuota(UUID.randomUUID().toString(), 1, 1);
  }

  @Test
  public void getAndSetSpecificBucketQuota() {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();

          AmazonS3 s3 =
              createS3(
                  v.getS3Credentials().get(0).getAccessKey(),
                  v.getS3Credentials().get(0).getSecretKey());
          String bucketName = userId.toLowerCase();
          s3.createBucket(bucketName);

          Quota quota;

          // default false
          quota = RGW_ADMIN.getBucketInfo(bucketName).get().getBucketQuota();
          assertEquals(false, quota.getEnabled());
          assertEquals(Long.valueOf(-1), quota.getMaxObjects());
          assertTrue(
              quota.getMaxSizeKb() == -1 // jewel
                  || quota.getMaxSizeKb() == 0 // kraken
          );

          // set quota
          RGW_ADMIN.setIndividualBucketQuota(userId, bucketName, 1, 1);

          // not shown by getBucketQuota()
          quota = RGW_ADMIN.getBucketQuota(userId).get();
          assertEquals(false, quota.getEnabled());
          assertEquals(Long.valueOf(-1), quota.getMaxObjects());
          assertTrue(
              quota.getMaxSizeKb() == -1 // jewel
                  || quota.getMaxSizeKb() == 0 // kraken
          );

          // not shown by getBucketInfo()
          quota = RGW_ADMIN.getBucketInfo(bucketName).get().getBucketQuota();
          assertEquals(true, quota.getEnabled());
          assertEquals(Long.valueOf(1), quota.getMaxObjects());
          assertEquals(Long.valueOf(1), quota.getMaxSizeKb());
        });
  }

  @Test
  public void getObjectPolicy() {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          AmazonS3 s3 =
              createS3(
                  v.getS3Credentials().get(0).getAccessKey(),
                  v.getS3Credentials().get(0).getSecretKey());
          String bucketName = userId.toLowerCase();
          String objectKey = userId.toLowerCase();
          s3.createBucket(bucketName);
          s3.putObject(bucketName, objectKey, "qqq");
          String resp = RGW_ADMIN.getObjectPolicy(bucketName, objectKey).get();
          assertFalse(Strings.isNullOrEmpty(resp));
        });
  }

  @Test
  public void getBucketPolicy() {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          AmazonS3 s3 =
              createS3(
                  v.getS3Credentials().get(0).getAccessKey(),
                  v.getS3Credentials().get(0).getSecretKey());
          String bucketName = userId.toLowerCase();
          s3.createBucket(bucketName);
          String resp = RGW_ADMIN.getBucketPolicy(bucketName).get();
          assertFalse(Strings.isNullOrEmpty(resp));
        });
  }

  @Test
  public void removeObject() {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          AmazonS3 s3 =
              createS3(
                  v.getS3Credentials().get(0).getAccessKey(),
                  v.getS3Credentials().get(0).getSecretKey());
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
