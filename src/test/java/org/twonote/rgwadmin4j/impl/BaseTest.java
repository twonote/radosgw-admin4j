package org.twonote.rgwadmin4j.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.client.factory.AuthenticationMethod;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.junit.BeforeClass;
import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.RgwAdminBuilder;
import org.twonote.rgwadmin4j.model.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by hrchu on 2017/4/10.
 */
public class BaseTest {
  protected static RgwAdmin RGW_ADMIN;
  protected static String adminUserId;
  protected static String adminAccessKey;
  protected static String adminSecretKey;
  protected static String s3Endpoint;
  protected static String swiftEndpoint;
  protected static String adminEndpoint;

  protected static void doSomething(AmazonS3 s3) {
    String userId = UUID.randomUUID().toString();
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

  protected static void doSomething(User v) {
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

  protected static void testSwiftConnectivity(String username, String password) {
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
      AmazonS3 s3 = initS3(adminAccessKey, adminSecretKey, s3Endpoint);
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

  protected static AmazonS3 initS3(String accessKey, String secretKey, String endPoint) {
    AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
    ClientConfiguration clientConfig = new ClientConfiguration();
    clientConfig.setProtocol(Protocol.HTTP);
    clientConfig.withSignerOverride("S3SignerType");
    //noinspection deprecation
    AmazonS3 s3 = new AmazonS3Client(credentials, clientConfig);
    s3.setEndpoint(endPoint);
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
          initS3(user.getKeys().get(0).getAccessKey(), user.getKeys().get(0).getSecretKey(), s3Endpoint);

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

  protected static void testWithASubUser(Consumer<User> test) {
    String subUserId = UUID.randomUUID().toString();
    testWithAUser(
        v -> {
          RGW_ADMIN.createSubUser(v.getUserId(), subUserId, null);
          User user = RGW_ADMIN.getUserInfo(v.getUserId()).get();
          test.accept(user);
        });
  }
}
