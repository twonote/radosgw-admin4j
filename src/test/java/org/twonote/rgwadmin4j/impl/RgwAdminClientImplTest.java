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
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.twonote.rgwadmin4j.RgwAdminException;
import org.twonote.rgwadmin4j.model.*;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class RgwAdminClientImplTest {
  private static RgwAdminClientImpl RGW_ADMIN_CLIENT;
  private static String adminUserId;
  private static String accessKey;
  private static String secretKey;
  private static String s3Endpoint;
  private static String adminEndpoint;

  @BeforeClass
  public static void init() throws IOException {
    initPros();
    RGW_ADMIN_CLIENT = new RgwAdminClientImpl(accessKey, secretKey, adminEndpoint);
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
  }

  /**
   * Creates a temporary file with text data to demonstrate uploading a file to Amazon S3
   *
   * @return A newly created temporary file with text data.
   * @throws IOException
   */
  private static File createSampleFile() throws IOException {
    File file = File.createTempFile("aws-java-sdk-", ".txt");
    file.deleteOnExit();

    Writer writer = new OutputStreamWriter(new FileOutputStream(file));
    writer.write("abcdefghijklmnopqrstuvwxyz\n");
    writer.write("01234567890112345678901234\n");
    writer.write("!@#$%^&*()-=[]{};':',.<>/?\n");
    writer.write("01234567890112345678901234\n");
    writer.write("abcdefghijklmnopqrstuvwxyz\n");
    writer.close();

    return file;
  }

  private static AmazonS3 initS3(String accessKey, String secretKey, String endPoint) {
    AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
    ClientConfiguration clientConfig = new ClientConfiguration();
    clientConfig.setProtocol(Protocol.HTTP);
    clientConfig.withSignerOverride("S3SignerType");
    AmazonS3 s3 = new AmazonS3Client(credentials, clientConfig);
    s3.setEndpoint(endPoint);
    return s3;
  }

  private static String createString(int size) {
    char[] chars = new char[size];
    Arrays.fill(chars, 'f');
    return new String(chars);
  }

  private static void testWithAUser(Consumer<CreateUserResponse> test) {
    String userId = "rgwAdmin4jTest-" + UUID.randomUUID().toString();
    try {
      CreateUserResponse response = RGW_ADMIN_CLIENT.createUser(userId);
      test.accept(response);
    } finally {
      RGW_ADMIN_CLIENT.removeUser(userId);
    }
  }

  @Test
  public void createKey() throws Exception {
    testWithAUser(
        v -> {
          List<CreateKeyResponse> response;

          // basic
          response = RGW_ADMIN_CLIENT.createKey(v.getUserId());
          assertEquals(2, response.size());
          assertEquals(2, RGW_ADMIN_CLIENT.getUserInfo(v.getUserId()).get().getKeys().size());

          // specify the key
          Map<String, String> paras = ImmutableMap.of("access-key", UUID.randomUUID().toString());
          response = RGW_ADMIN_CLIENT.createKey(v.getUserId(), paras);
          assertTrue(
              response.stream().anyMatch(v1 -> paras.get("access-key").equals(v1.getAccess_key())));

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
          RGW_ADMIN_CLIENT.removeKey(accessKey, "s3");
          assertEquals(0, RGW_ADMIN_CLIENT.getUserInfo(v.getUserId()).get().getKeys().size());

          // not exist
          try {
            RGW_ADMIN_CLIENT.removeKey(accessKey, "s3");
          } catch (RgwAdminException e) {
            assertEquals(
                403, e.status()); // ceph version 11.2.0 (f223e27eeb35991352ebc1f67423d4ebc252adb7)
          }
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

  @Ignore
  @Test
  public void getUserQuota() throws Exception {}

  @Ignore
  @Test
  public void setUserQuota() throws Exception {}

  @Test
  public void addUserCapability() throws Exception {
    String userId = "test" + UUID.randomUUID().toString();
    String userCaps = "usage=read,write;user=write";

    // user not exist
    try {
      RGW_ADMIN_CLIENT.addUserCapability(userId, userCaps);
    } catch (RuntimeException e) {
      // 400
    }

    try {
      RGW_ADMIN_CLIENT.createUser(userId);

      // basic
      RGW_ADMIN_CLIENT.addUserCapability(userId, userCaps);
      GetUserInfoResponse response = RGW_ADMIN_CLIENT.getUserInfo(userId).get();
      assertEquals("usage", response.getCaps().get(0).get("type"));
      assertEquals("*", response.getCaps().get(0).get("perm"));
      assertEquals("user", response.getCaps().get(1).get("type"));
      assertEquals("write", response.getCaps().get(1).get("perm"));

      // do it again
      RGW_ADMIN_CLIENT.addUserCapability(userId, userCaps);

    } finally {
      try {
        RGW_ADMIN_CLIENT.removeUser(userId);
      } catch (Exception e) {
      }
    }
  }

  @Test
  public void removeUserCapability() throws Exception {
    String userId = "test" + UUID.randomUUID().toString();
    String userCaps = "usage=read";

    // user not exist
    try {
      RGW_ADMIN_CLIENT.removeUserCapability(userId, userCaps);
    } catch (RuntimeException e) {
      // 400
    }

    try {
      RGW_ADMIN_CLIENT.createUser(userId);

      // cap not exist
      RGW_ADMIN_CLIENT.removeUserCapability(userId, userCaps);

      // basic
      RGW_ADMIN_CLIENT.addUserCapability(userId, userCaps);
      RGW_ADMIN_CLIENT.removeUserCapability(userId, userCaps);
      GetUserInfoResponse response = RGW_ADMIN_CLIENT.getUserInfo(userId).get();
      assertEquals(0, response.getCaps().size());

      // do it again
      RGW_ADMIN_CLIENT.removeUserCapability(userId, userCaps);
    } finally {
      try {
        RGW_ADMIN_CLIENT.removeUser(userId);
      } catch (Exception e) {
      }
    }
  }

  @Test
  public void removeBucket() throws Exception {
    String userId = "testremovebk" + UUID.randomUUID().toString();
    String bucketName = "testremovebkbk" + UUID.randomUUID().toString();

    // remove bucket not exist
    Thread.sleep(3000);
    RGW_ADMIN_CLIENT.removeBucket(bucketName);

    try {
      CreateUserResponse response = RGW_ADMIN_CLIENT.createUser(userId);
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
    } finally {
      try {
        RGW_ADMIN_CLIENT.removeUser(userId);
      } catch (Exception e) {

      }
    }
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
    String userId = "linkbkusr" + UUID.randomUUID().toString();
    String bucketName = "linkbkusrbk" + UUID.randomUUID().toString();
    try {
      CreateUserResponse response = RGW_ADMIN_CLIENT.createUser(userId);
      AmazonS3 s3 =
          initS3(
              response.getKeys().get(0).getAccessKey(),
              response.getKeys().get(0).getSecretKey(),
              s3Endpoint);
      s3.createBucket(bucketName);

      GetBucketInfoResponse _response = RGW_ADMIN_CLIENT.getBucketInfo(bucketName).get();

      // basic
      String bucketId = _response.getId();
      RGW_ADMIN_CLIENT.linkBucket(bucketName, bucketId, adminUserId);
      GetBucketInfoResponse __response = RGW_ADMIN_CLIENT.getBucketInfo(bucketName).get();
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

    } finally {
      AmazonS3 adminS3 = initS3(accessKey, secretKey, s3Endpoint);
      try {
        adminS3.deleteBucket(bucketName);
      } catch (Exception e) {

      }
      try {
        RGW_ADMIN_CLIENT.removeUser(userId);
      } catch (Exception e) {

      }
    }
  }

  @Test
  public void getBucketInfo() throws Exception {
    String userId = "testgetbucketinfouserx";
    String bucketName = "testgetbucketinfo";

    // not exist
    assertFalse(RGW_ADMIN_CLIENT.getBucketInfo(bucketName).isPresent());

    try {
      CreateUserResponse response = RGW_ADMIN_CLIENT.createUser(userId);
      AmazonS3 s3 =
          initS3(
              response.getKeys().get(0).getAccessKey(),
              response.getKeys().get(0).getSecretKey(),
              s3Endpoint);
      s3.createBucket(bucketName);

      GetBucketInfoResponse _response = RGW_ADMIN_CLIENT.getBucketInfo(bucketName).get();
      assertNotNull(_response.getId());

    } finally {
      try {
        RGW_ADMIN_CLIENT.removeUser(userId);
      } catch (Exception e) {

      }
    }
  }

  @Test
  public void modifyUser() throws Exception {
    String userId = "testModifyUserId";
    RGW_ADMIN_CLIENT.createUser(userId);

    // basic
    RGW_ADMIN_CLIENT.modifyUser(
        userId, ImmutableMap.of("max-buckets", String.valueOf(Integer.MAX_VALUE)));
    GetUserInfoResponse response = RGW_ADMIN_CLIENT.getUserInfo(userId).get();
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
      CreateUserResponse response = RGW_ADMIN_CLIENT.createUser(userId);
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
    GetUserInfoResponse response = RGW_ADMIN_CLIENT.getUserInfo(adminUserId).get();
    assertEquals(Integer.valueOf(0), response.getSuspended());
    assertEquals(adminUserId, response.getUserId());
    List<Map<String, String>> caps =
        Arrays.asList(
            ImmutableMap.of("type", "users", "perm", "*"),
            ImmutableMap.of("type", "buckets", "perm", "*"));
    assertTrue(caps.containsAll(response.getCaps()));

    // not exist
    assertFalse(RGW_ADMIN_CLIENT.getUserInfo(UUID.randomUUID().toString()).isPresent());
  }

  @Test
  public void suspendUser() throws Exception {
    String userId = "bobx" + UUID.randomUUID().toString();
    try {
      RGW_ADMIN_CLIENT.createUser(userId);
      RGW_ADMIN_CLIENT.suspendUser(userId);
      GetUserInfoResponse response = RGW_ADMIN_CLIENT.getUserInfo(userId).get();
      assertEquals(Integer.valueOf(1), response.getSuspended());
    } finally {
      RGW_ADMIN_CLIENT.removeUser(userId);
    }

    // not exist
    RGW_ADMIN_CLIENT.suspendUser(UUID.randomUUID().toString());
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
          assertEquals(Integer.valueOf(-1), quota.getMaxObjects());
          assertTrue(
              quota.getMaxSizeKb() == -1 // jewel
                  || quota.getMaxSizeKb() == 0 // kraken
              );

          // set quota
          RGW_ADMIN_CLIENT.setUserQuota(userId, 1, 1);
          quota = RGW_ADMIN_CLIENT.getUserQuota(userId).get();
          assertEquals(true, quota.getEnabled());
          assertEquals(Integer.valueOf(1), quota.getMaxObjects());
          assertEquals(Integer.valueOf(1), quota.getMaxSizeKb());
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
  public void getPolicy() throws Exception {
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
          String resp = RGW_ADMIN_CLIENT.getPolicy(bucketName, objectKey).get();
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
