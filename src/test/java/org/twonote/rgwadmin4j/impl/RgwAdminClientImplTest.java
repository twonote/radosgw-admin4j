package org.twonote.rgwadmin4j.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.client.factory.AuthenticationMethod;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.twonote.rgwadmin4j.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class RgwAdminClientImplTest {

  private static RgwAdminClientImpl RGW_ADMIN_CLIENT;
  private static String adminUserId;
  private static String accessKey;
  private static String secretKey;
  private static String s3Endpoint;
  private static String swiftEndpoint;
  private static String adminEndpoint;

  private static void doSomething(User v) {
    String userId = v.getUserId();
    // Do something to let usage log generated.
    AmazonS3 s3 =
        initS3(v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey(), s3Endpoint);
    String bucketName = userId.toLowerCase();
    s3.createBucket(bucketName);

    s3.putObject(bucketName, userId + "1", createString(40960));
    s3.putObject(bucketName, userId + "2", createString(40960));
    s3.putObject(bucketName, userId + "3", createString(40960));

    // Usage data are generated in the async way, hope it will be available after wait.
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static void testSwiftConnectivity(String username, String password) {
    AccountConfig config = new AccountConfig();
    config.setUsername(username);
    config.setPassword(password);
    config.setAuthUrl(swiftEndpoint);
    config.setAuthenticationMethod(AuthenticationMethod.BASIC);
    Account account = new AccountFactory(config).createAccount();
    Container container = account.getContainer(UUID.randomUUID().toString().toLowerCase());
    container.create();
  }

  @BeforeClass
  public static void init() throws IOException {
    initPros();
    RGW_ADMIN_CLIENT = new RgwAdminClientImpl(accessKey, secretKey, adminEndpoint);
    testRgwConnectivity();
  }

  private static void testRgwConnectivity() {
    try {
      AmazonS3 s3 = initS3(accessKey, secretKey, s3Endpoint);
      s3.listBuckets();
    } catch (Exception e) {
      System.out.println(
          "Cannot make communication with radosgw S3 endpoint: " + e.getLocalizedMessage());
      System.exit(0);
    }
    try {
      //noinspection ResultOfMethodCallIgnored
      RGW_ADMIN_CLIENT.getUserInfo(adminUserId).get();
    } catch (NoSuchElementException | RgwAdminException e) {
      System.out.println(
          "Cannot make communication with radosgw admin endpoint: " + e.getLocalizedMessage());
      System.exit(0);
    }
  }

  private static void initPros() throws IOException {
    String env = System.getProperty("env", "");
    if (!"".equals(env)) {
      env = "." + env;
    }
    Properties properties = new Properties();
    properties.load(RgwAdminClientImplTest.class.getResourceAsStream("/rgwadmin.properties" + env));

    adminUserId = properties.getProperty("radosgw.adminId");
    accessKey = properties.getProperty("radosgw.adminAccessKey");
    secretKey = properties.getProperty("radosgw.adminSecretKey");
    s3Endpoint = properties.getProperty("radosgw.endpoint");
    adminEndpoint = properties.getProperty("radosgw.adminEndpoint");
    swiftEndpoint = s3Endpoint + "/auth/1.0";
  }

  private static AmazonS3 initS3(String accessKey, String secretKey, String endPoint) {
    AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
    ClientConfiguration clientConfig = new ClientConfiguration();
    clientConfig.setProtocol(Protocol.HTTP);
    clientConfig.withSignerOverride("S3SignerType");
    //noinspection deprecation
    AmazonS3 s3 = new AmazonS3Client(credentials, clientConfig);
    s3.setEndpoint(endPoint);
    return s3;
  }

  private static String createString(int size) {
    char[] chars = new char[size];
    Arrays.fill(chars, 'f');
    return new String(chars);
  }

  private static void testWithAUser(Consumer<User> test) {
    String userId = "rgwAdmin4jTest-" + UUID.randomUUID().toString();
    try {
      User response = RGW_ADMIN_CLIENT.createUser(userId);
      test.accept(response);
    } finally {
      RGW_ADMIN_CLIENT.removeUser(userId);
    }
  }

  private static void testWithASubUser(Consumer<User> test) {
    String subUserId = UUID.randomUUID().toString();
    testWithAUser(
        v -> {
          RGW_ADMIN_CLIENT.createSubUser(v.getUserId(), subUserId, null);
          User user = RGW_ADMIN_CLIENT.getUserInfo(v.getUserId()).get();
          test.accept(user);
        });
  }

  @Test
  public void createKey() throws Exception {
    testWithAUser(
        v -> {
          List<Key> response;

          // basic
          response = RGW_ADMIN_CLIENT.createKey(v.getUserId());
          assertEquals(2, response.size());
          assertEquals(2, RGW_ADMIN_CLIENT.getUserInfo(v.getUserId()).get().getKeys().size());

          // specify the key
          String accessKey = v.getUserId() + "-accessKey";
          String secretKey = v.getUserId() + "-secretKey";
          response = RGW_ADMIN_CLIENT.createKey(v.getUserId(), accessKey, secretKey);
          assertTrue(
              response
                  .stream()
                  .anyMatch(
                      v1 ->
                          accessKey.equals(v1.getAccessKey())
                              && secretKey.equals(v1.getSecretKey())));

          // user not exist
          try {
            RGW_ADMIN_CLIENT.createKey(UUID.randomUUID().toString());
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
          RGW_ADMIN_CLIENT.removeKey(v.getUserId(), accessKey);
          assertEquals(0, RGW_ADMIN_CLIENT.getUserInfo(v.getUserId()).get().getKeys().size());

          // key not exist
          try {
            RGW_ADMIN_CLIENT.removeKey(v.getUserId(), UUID.randomUUID().toString());
          } catch (RgwAdminException e) {
            assertEquals(
                403, e.status()); // ceph version 11.2.0 (f223e27eeb35991352ebc1f67423d4ebc252adb7)
          }

          // user not exist
          try {
            RGW_ADMIN_CLIENT.removeKey(UUID.randomUUID().toString(), UUID.randomUUID().toString());
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
          response = RGW_ADMIN_CLIENT.createKeyForSubUser(userId, subUserId);
          assertTrue(response.stream().anyMatch(vv -> absSubUserId.equals(vv.getUser())));

          // specify the key
          String accessKey = v.getUserId() + "-accessKey";
          String secretKey = v.getUserId() + "-secretKey";
          response = RGW_ADMIN_CLIENT.createKeyForSubUser(userId, subUserId, accessKey, secretKey);
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
          RGW_ADMIN_CLIENT.createKeyForSubUser(userId, "XXXXXXX");
        });
  }

  @Test
  public void removeKeyFromSubUser() throws Exception {
    testWithASubUser(
        v -> {
          String absSubUserId = v.getSubusers().get(0).getId(); // In forms of "foo:bar"
          String userId = absSubUserId.split(":")[0];
          String subUserId = absSubUserId.split(":")[1];

          List<Key> response = RGW_ADMIN_CLIENT.createKeyForSubUser(userId, subUserId);
          Key keyToDelete =
              response.stream().filter(vv -> absSubUserId.equals(vv.getUser())).findFirst().get();

          // basic
          RGW_ADMIN_CLIENT.removeKeyFromSubUser(userId, subUserId, keyToDelete.getAccessKey());
          assertFalse(
              RGW_ADMIN_CLIENT
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
            RGW_ADMIN_CLIENT.removeKeyFromSubUser(userId, subUserId, UUID.randomUUID().toString());
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
          response = RGW_ADMIN_CLIENT.createSecretForSubUser(userId, subUserId);
          assertTrue(response.stream().anyMatch(vv -> absSubUserId.equals(vv.getUser())));

          // specify the key
          String secret = v.getUserId() + "-secret";
          response = RGW_ADMIN_CLIENT.createSecretForSubUser(userId, subUserId, secret);
          assertTrue(
              response
                  .stream()
                  .anyMatch(
                      v1 -> absSubUserId.equals(v1.getUser()) && secret.equals(v1.getSecretKey())));

          // sub user not exist
          // Create a orphan key without user in ceph version 11.2.0 (f223e27eeb35991352ebc1f67423d4ebc252adb7)
          RGW_ADMIN_CLIENT.createSecretForSubUser(userId, subUserId);
        });
  }

  @Test
  public void removeSecretFromSubUser() throws Exception {
    testWithASubUser(
        v -> {
          String absSubUserId = v.getSubusers().get(0).getId(); // In forms of "foo:bar"
          String userId = absSubUserId.split(":")[0];
          String subUserId = absSubUserId.split(":")[1];

          RGW_ADMIN_CLIENT.createSecretForSubUser(userId, subUserId);

          // basic
          RGW_ADMIN_CLIENT.removeSecretFromSubUser(userId, subUserId);
          assertFalse(
              RGW_ADMIN_CLIENT
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
          doSomething(v);

          String userId = v.getUserId();

          UsageInfo response;
          response = RGW_ADMIN_CLIENT.getUserUsage(userId).get();
          if (response.getSummary().stream().noneMatch(vv -> userId.equals(vv.getUser()))) {
            fail("No usage log corresponding to the given user id...need more sleep?");
          }

          RGW_ADMIN_CLIENT.trimUserUsage(userId, null);

          // Usage data are generated in the async way, hope it will be available after wait.
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

          response = RGW_ADMIN_CLIENT.getUserUsage(userId).get();
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
          doSomething(v);

          UsageInfo response;

          response = RGW_ADMIN_CLIENT.getUsage(null).get();
          if (response.getSummary().stream().noneMatch(vv -> userId.equals(vv.getUser()))) {
            fail("No usage log corresponding to the given user id...need more sleep?");
          }

          response = RGW_ADMIN_CLIENT.getUserUsage(userId, null).get();
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
          List<SubUser> response = RGW_ADMIN_CLIENT.createSubUser(v.getUserId(), subUserId, null);
          assertEquals("<none>", response.get(0).getPermissions());
          response =
              RGW_ADMIN_CLIENT.modifySubUser(
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
          RGW_ADMIN_CLIENT.createSubUserForSwift(v.getUserId(), subUserId);
          User response2 = RGW_ADMIN_CLIENT.getUserInfo(v.getUserId()).get();
          assertEquals(1, response2.getSwiftKeys().size());
          RGW_ADMIN_CLIENT.removeSubUser(v.getUserId(), subUserId);
          response2 = RGW_ADMIN_CLIENT.getUserInfo(v.getUserId()).get();
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
              RGW_ADMIN_CLIENT.createSubUser(
                  v.getUserId(), subUserId, ImmutableMap.of("key-type", "s3", "access", "full"));
          assertEquals(1, response.size());
          String fullSubUserId = v.getUserId() + ":" + subUserId;
          assertEquals(fullSubUserId, response.get(0).getId());
          assertEquals("full-control", response.get(0).getPermissions());

          // exist in get user info response
          User response2 = RGW_ADMIN_CLIENT.getUserInfo(v.getUserId()).get();
          assertEquals(fullSubUserId, response2.getSubusers().get(0).getId());

          // test subuser in s3
          Key key =
              response2
                  .getKeys()
                  .stream()
                  .filter(e -> fullSubUserId.equals(e.getUser()))
                  .findFirst()
                  .get();
          AmazonS3 s3 = initS3(key.getAccessKey(), key.getSecretKey(), s3Endpoint);
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
          List<SubUser> response = RGW_ADMIN_CLIENT.createSubUserForSwift(v.getUserId(), subUserId);
          assertEquals(1, response.size());
          assertEquals(v.getUserId() + ":" + subUserId, response.get(0).getId());
          assertEquals("full-control", response.get(0).getPermissions());

          // test subuser in swift
          User response2 = RGW_ADMIN_CLIENT.getUserInfo(v.getUserId()).get();
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
              initS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey(), s3Endpoint);
          String bucketName = userId.toLowerCase();

          // not exist
          RGW_ADMIN_CLIENT.checkBucketIndex(bucketName, true, true);

          s3.createBucket(bucketName);
          // Do not know how to check the behavior...
          Optional result = RGW_ADMIN_CLIENT.checkBucketIndex(bucketName, true, true);
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
          retUserCaps = RGW_ADMIN_CLIENT.addUserCapability(userId, userCaps);
          assertEquals(userCaps, retUserCaps);

          // remove
          List<Cap> toRemove = userCaps.subList(0, 1);
          List<Cap> toRemain = userCaps.subList(1, 2);
          retUserCaps = RGW_ADMIN_CLIENT.removeUserCapability(userId, toRemove);
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
    RGW_ADMIN_CLIENT.removeBucket(bucketName);

    testWithAUser(
        v -> {
          String userId = "testremovebk" + UUID.randomUUID().toString();

          User response = RGW_ADMIN_CLIENT.createUser(userId);
          AmazonS3 s3 =
              initS3(
                  response.getKeys().get(0).getAccessKey(),
                  response.getKeys().get(0).getSecretKey(),
                  s3Endpoint);
          s3.createBucket(bucketName);

          ByteArrayInputStream input = new ByteArrayInputStream("Hello World!".getBytes());
          s3.putObject(bucketName, "hello.txt", input, new ObjectMetadata());

          RGW_ADMIN_CLIENT.removeBucket(bucketName);

          try {
            s3.headBucket(new HeadBucketRequest(bucketName));
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
              initS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey(), s3Endpoint);
          String bucketName = userId.toLowerCase();

          // not exist
          RGW_ADMIN_CLIENT.unlinkBucket(bucketName, userId);

          s3.createBucket(bucketName);

          // basic
          RGW_ADMIN_CLIENT.unlinkBucket(bucketName, userId);
          assertEquals(0, s3.listBuckets().size());

          // head is ok...
          s3.headBucket(new HeadBucketRequest(bucketName));

          // again
          RGW_ADMIN_CLIENT.unlinkBucket(bucketName, userId);
        });
  }

  @Test
  public void linkBucket() throws Exception {
    testWithAUser(
        v -> {
          String userId = "linkbkusr" + UUID.randomUUID().toString();
          String bucketName = "linkbkusrbk" + UUID.randomUUID().toString();
          User response = RGW_ADMIN_CLIENT.createUser(userId);
          AmazonS3 s3 =
              initS3(
                  response.getKeys().get(0).getAccessKey(),
                  response.getKeys().get(0).getSecretKey(),
                  s3Endpoint);
          s3.createBucket(bucketName);

          BucketInfo _response = RGW_ADMIN_CLIENT.getBucketInfo(bucketName).get();

          // basic
          String bucketId = _response.getId();
          RGW_ADMIN_CLIENT.linkBucket(bucketName, bucketId, adminUserId);
          BucketInfo __response = RGW_ADMIN_CLIENT.getBucketInfo(bucketName).get();
          assertEquals(adminUserId, __response.getOwner());

          // execute again
          // Ceph 9.2.x throw exception; ceph 10.2.2 returns 404 so no exception will show.
          //            exception.expect(RuntimeException.class);
          RGW_ADMIN_CLIENT.linkBucket(bucketName, bucketId, adminUserId);

          // bad argument
          //            exception.expect(RuntimeException.class);
          RGW_ADMIN_CLIENT.linkBucket(bucketName + "qq", bucketId, adminUserId);

          //            exception.expect(RuntimeException.class);
          RGW_ADMIN_CLIENT.linkBucket(bucketName, bucketId, adminUserId + "qqq");

          //            exception.expect(RuntimeException.class);
          RGW_ADMIN_CLIENT.linkBucket(bucketName, bucketId + "qq", adminUserId);
        });
  }

  @Test
  public void listBucketInfo() throws Exception {
    testWithASubUser(
        v -> {
          AmazonS3 s3 =
              initS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey(), s3Endpoint);
          for (int i = 0; i < 3; i++) {
            s3.createBucket(UUID.randomUUID().toString().toLowerCase());
          }
          List<BucketInfo> response = RGW_ADMIN_CLIENT.listBucketInfo(v.getUserId());
          assertEquals(3, response.size());
        });
  }

  @Test
  public void listBucket() throws Exception {
    testWithASubUser(
        v -> {
          AmazonS3 s3 =
              initS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey(), s3Endpoint);
          for (int i = 0; i < 3; i++) {
            s3.createBucket(UUID.randomUUID().toString().toLowerCase());
          }
          List<String> response = RGW_ADMIN_CLIENT.listBucket(v.getUserId());
          assertEquals(3, response.size());
        });
  }

  @Test
  public void getBucketInfo() throws Exception {
    testWithASubUser(
        v -> {
          AmazonS3 s3 =
              initS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey(), s3Endpoint);
          String bucketName = UUID.randomUUID().toString().toLowerCase();
          s3.createBucket(bucketName);

          Optional<BucketInfo> response = RGW_ADMIN_CLIENT.getBucketInfo(bucketName);
          assertTrue(response.isPresent());
        });
  }

  @Test
  public void modifyUser() throws Exception {
    String userId = "testModifyUserId";
    RGW_ADMIN_CLIENT.createUser(userId);

    // basic
    RGW_ADMIN_CLIENT.modifyUser(
        userId, ImmutableMap.of("max-buckets", String.valueOf(Integer.MAX_VALUE)));
    User response = RGW_ADMIN_CLIENT.getUserInfo(userId).get();
    assertEquals(Integer.valueOf(Integer.MAX_VALUE), response.getMaxBuckets());

    // user not exist
    RGW_ADMIN_CLIENT.modifyUser(
        userId + "qqqq", ImmutableMap.of("max-buckets", String.valueOf(Integer.MAX_VALUE)));

    // ignore call with wrong arguments
    RGW_ADMIN_CLIENT.modifyUser(
        userId, ImmutableMap.of("QQQQQ", String.valueOf(Integer.MAX_VALUE)));
    RGW_ADMIN_CLIENT.modifyUser(userId, ImmutableMap.of("max-buckets", "you-know-my-name"));
    assertEquals(Integer.valueOf(Integer.MAX_VALUE), response.getMaxBuckets());
  }

  @Test
  public void removeUser() throws Exception {
    // The operation is success if the user is not exist in the system after the operation is executed.
    String userId = "testRemoveUserId";
    RGW_ADMIN_CLIENT.createUser(userId);
    RGW_ADMIN_CLIENT.removeUser(userId);
    assertFalse(RGW_ADMIN_CLIENT.getUserInfo(userId).isPresent());

    // The operation does not throw exception even if the user is not exist in the beginning.
    RGW_ADMIN_CLIENT.removeUser(userId);
  }

  @Test
  public void createUser() throws Exception {
    String userId = "bobx" + UUID.randomUUID().toString();
    try {
      // basic
      User response = RGW_ADMIN_CLIENT.createUser(userId);
      assertEquals(userId, response.getUserId());
      assertNotNull(response.getKeys().get(0).getAccessKey());
      assertNotNull(response.getKeys().get(0).getSecretKey());
      assertEquals(Integer.valueOf(0), response.getSuspended());
      assertEquals(Integer.valueOf(1000), response.getMaxBuckets());

      // create exist one should act like modification
      response = RGW_ADMIN_CLIENT.createUser(userId, ImmutableMap.of("max-buckets", "1"));
      assertEquals(Integer.valueOf(1), response.getMaxBuckets());

    } finally {
      RGW_ADMIN_CLIENT.removeUser(userId);
    }
  }

  @Test
  public void getUserInfo() throws Exception {
    // basic
    User response = RGW_ADMIN_CLIENT.getUserInfo(adminUserId).get();
    assertEquals(Integer.valueOf(0), response.getSuspended());
    assertEquals(adminUserId, response.getUserId());
    List<Cap> caps =
        Arrays.asList(
            new Cap(Cap.Type.USERS, Cap.Perm.READ_WRITE),
            new Cap(Cap.Type.BUCKETS, Cap.Perm.READ_WRITE));
    assertTrue(response.getCaps().containsAll(caps));

    // not exist
    assertFalse(RGW_ADMIN_CLIENT.getUserInfo(UUID.randomUUID().toString()).isPresent());
  }

  @Test
  public void suspendUser() throws Exception {
    testWithASubUser(
        v -> {
          String userId = v.getUserId();
          User response;

          // suspend
          RGW_ADMIN_CLIENT.suspendUser(userId, true);
          response = RGW_ADMIN_CLIENT.getUserInfo(userId).get();
          assertEquals(Integer.valueOf(1), response.getSuspended());

          // resume
          RGW_ADMIN_CLIENT.suspendUser(userId, false);
          response = RGW_ADMIN_CLIENT.getUserInfo(userId).get();
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
          RGW_ADMIN_CLIENT.setUserQuota(userId, 2, -1);
          quota = RGW_ADMIN_CLIENT.getUserQuota(userId).get();
          assertEquals(true, quota.getEnabled());

          AmazonS3 s3 =
              initS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey(), s3Endpoint);
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
          RGW_ADMIN_CLIENT.setUserQuota(userId, -1, 12);
          quota = RGW_ADMIN_CLIENT.getUserQuota(userId).get();
          assertEquals(true, quota.getEnabled());

          AmazonS3 s3 =
              initS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey(), s3Endpoint);
          String bucketName = userId.toLowerCase();
          s3.createBucket(bucketName);

          // ok, ok, ok, since the total to used size exceed 12KiB
          s3.putObject(bucketName, userId + "1", createString(4096));
          s3.putObject(bucketName, userId + "2", createString(4096));
          s3.putObject(bucketName, userId + "3", createString(4096));

          // not ok, since the total to used size exceed 12KiB +1
          try {
            s3.putObject(bucketName, userId + "4", createString(1));
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
          quota = RGW_ADMIN_CLIENT.getUserQuota(userId).get();
          assertEquals(false, quota.getEnabled());
          assertEquals(Long.valueOf(-1), quota.getMaxObjects());
          assertTrue(
              quota.getMaxSizeKb() == -1 // jewel
                  || quota.getMaxSizeKb() == 0 // kraken
              );

          // set quota
          RGW_ADMIN_CLIENT.setUserQuota(userId, 1, 1);
          quota = RGW_ADMIN_CLIENT.getUserQuota(userId).get();
          assertEquals(true, quota.getEnabled());
          assertEquals(Long.valueOf(1), quota.getMaxObjects());
          assertEquals(Long.valueOf(1), quota.getMaxSizeKb());
        });

    // not exist
    try {
      RGW_ADMIN_CLIENT.getUserQuota(UUID.randomUUID().toString());
    } catch (RgwAdminException e) {
      assertEquals(400, e.status());
      assertEquals("InvalidArgument", e.getMessage());
    }

    RGW_ADMIN_CLIENT.setUserQuota(UUID.randomUUID().toString(), 1, 1);
  }

  @Test
  public void getObjectPolicy() throws Exception {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          AmazonS3 s3 =
              initS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey(), s3Endpoint);
          String bucketName = userId.toLowerCase();
          String objectKey = userId.toLowerCase();
          s3.createBucket(bucketName);
          s3.putObject(bucketName, objectKey, "qqq");
          String resp = RGW_ADMIN_CLIENT.getObjectPolicy(bucketName, objectKey).get();
          assertFalse(Strings.isNullOrEmpty(resp));
        });
  }

  @Test
  public void getBucketPolicy() throws Exception {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          AmazonS3 s3 =
              initS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey(), s3Endpoint);
          String bucketName = userId.toLowerCase();
          s3.createBucket(bucketName);
          String resp = RGW_ADMIN_CLIENT.getBucketPolicy(bucketName).get();
          assertFalse(Strings.isNullOrEmpty(resp));
        });
  }

  @Test
  public void removeObject() throws Exception {
    testWithAUser(
        (v) -> {
          String userId = v.getUserId();
          AmazonS3 s3 =
              initS3(
                  v.getKeys().get(0).getAccessKey(), v.getKeys().get(0).getSecretKey(), s3Endpoint);
          String bucketName = userId.toLowerCase();
          s3.createBucket(bucketName);
          String objectKey = userId.toLowerCase();
          s3.putObject(bucketName, objectKey, "qqq");

          // basic
          RGW_ADMIN_CLIENT.removeObject(bucketName, objectKey);
          try {
            s3.getObjectMetadata(bucketName, objectKey);
          } catch (AmazonS3Exception e) {
            assertEquals(404, e.getStatusCode());
          }

          // not exist
          RGW_ADMIN_CLIENT.removeObject(bucketName, objectKey);
        });
  }
}
