package org.twonote.rgwadmin4j.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.collect.ImmutableMap;
import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.client.factory.AuthenticationMethod;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.junit.BeforeClass;
import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.RgwAdminBuilder;
import org.twonote.rgwadmin4j.model.S3Credential;
import org.twonote.rgwadmin4j.model.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Created by hrchu on 2017/4/10. */
public class BaseTest {
  protected static RgwAdmin RGW_ADMIN;
  protected static String adminUserId;
  protected static String adminAccessKey;
  protected static String adminSecretKey;
  protected static String s3Endpoint;
  protected static String swiftEndpoint;
  protected static String adminEndpoint;

  protected static void createSomeObjects(Account account) {
    Container container =
        account.getContainer("container-" + UUID.randomUUID().toString().toLowerCase());
    container.create();
    for (int i = 0; i < 3; i++) {
      container
          .getObject("OBJECT-" + UUID.randomUUID())
          .uploadObject(createString(4096).getBytes());
    }
  }

  protected static void createSomeObjects(AmazonS3 s3) {
    String bucketName = "bucket-" + UUID.randomUUID().toString().toLowerCase();
    s3.createBucket(bucketName);
    for (int i = 0; i < 3; i++) {

      s3.putObject(bucketName, "OBJECT-" + UUID.randomUUID(), createString(4096));
    }
    // Usage data are generated in the async way, hope it will be available after wait.
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  protected static void createSomeObjects(User v) {
    AmazonS3 s3 = createS3(v.getS3Credentials().get(0).getAccessKey(), v.getS3Credentials().get(0).getSecretKey());
    createSomeObjects(s3);
  }

  protected static void testSwiftConnectivity(String username, String password) {
    Account account = createSwift(username, password);
    createSomeObjects(account);
  }

  protected static Account createSwift(String username, String password) {
    AccountConfig config = new AccountConfig();
    config.setUsername(username);
    config.setPassword(password);
    config.setAuthUrl(swiftEndpoint);
    config.setAuthenticationMethod(AuthenticationMethod.BASIC);
    return new AccountFactory(config).createAccount();
  }

  @BeforeClass
  public static void init() throws IOException {
    initPros();

    RGW_ADMIN =
        new RgwAdminBuilder()
            .accessKey(adminAccessKey)
            .secretKey(adminSecretKey)
            .endpoint(adminEndpoint)
            .build();

    testRgwConnectivity();
  }

  private static void testRgwConnectivity() {
    try {
      AmazonS3 s3 = createS3(adminAccessKey, adminSecretKey);
      s3.listBuckets();
    } catch (Exception e) {
      System.out.println(
          "Cannot make communication with radosgw S3 endpoint: " + e.getLocalizedMessage());
      System.exit(0);
    }
    try {
      //noinspection ResultOfMethodCallIgnored
      RGW_ADMIN.getUserInfo(adminUserId).get();
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
    properties.load(RgwAdminImplTest.class.getResourceAsStream("/rgwadmin.properties" + env));

    adminUserId = properties.getProperty("radosgw.adminId");
    adminAccessKey = properties.getProperty("radosgw.adminAccessKey");
    adminSecretKey = properties.getProperty("radosgw.adminSecretKey");
    s3Endpoint = properties.getProperty("radosgw.endpoint");
    adminEndpoint = properties.getProperty("radosgw.adminEndpoint");
    swiftEndpoint = s3Endpoint + "/auth/1.0";
  }

  protected static AmazonS3 createS3(S3Credential key) {
    return createS3(key.getAccessKey(), key.getSecretKey());
  }

  protected static AmazonS3 createS3(String accessKey, String secretKey) {
    AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
    ClientConfiguration clientConfig = new ClientConfiguration();
    clientConfig.setProtocol(Protocol.HTTP);
    clientConfig.withSignerOverride("S3SignerType");
    //noinspection deprecation
    AmazonS3 s3 = new AmazonS3Client(credentials, clientConfig);
    s3.setEndpoint(s3Endpoint);
    return s3;
  }

  protected static String createString(int size) {
    char[] chars = new char[size];
    Arrays.fill(chars, 'f');
    return new String(chars);
  }

  protected static void testWithUserAndS3(BiConsumer<User, AmazonS3> test) {
    String userId = "rgwAdmin4jTest-" + UUID.randomUUID().toString();
    try {
      User user = RGW_ADMIN.createUser(userId);
      AmazonS3 s3 =
          createS3(user.getS3Credentials().get(0).getAccessKey(), user.getS3Credentials().get(0).getSecretKey());

      test.accept(user, s3);
    } finally {
      RGW_ADMIN.removeUser(userId);
    }
  }

  protected static void testWithAUser(Consumer<User> test) {
    String userId = "rgwAdmin4jTest-" + UUID.randomUUID().toString();
    try {
      User response = RGW_ADMIN.createUser(userId);
      test.accept(response);
    } finally {
      RGW_ADMIN.removeUser(userId);
    }
  }

  /**
   * Prepare a full control subuser for test
   *
   * @param test The test logic.
   */
  protected static void testWithASubUser(Consumer<User> test) {
    String subUserId = UUID.randomUUID().toString();
    testWithAUser(
        v -> {
          RGW_ADMIN.createSubUser(v.getUserId(), subUserId, ImmutableMap.of("access", "readwrite"));
          User user = RGW_ADMIN.getUserInfo(v.getUserId()).get();
          test.accept(user);
        });
  }
}
