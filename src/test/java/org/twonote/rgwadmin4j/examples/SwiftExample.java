package org.twonote.rgwadmin4j.examples;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;
import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.client.factory.AuthenticationMethod;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.impl.RgwAdminImpl;
import org.twonote.rgwadmin4j.model.CredentialType;
import org.twonote.rgwadmin4j.model.SubUser;
import org.twonote.rgwadmin4j.model.User;

/**
 * Created by petertc on 3/31/17.
 */
public class SwiftExample {

  private static final String userId = "rgwAdmin4jTest-" + UUID.randomUUID().toString();
  private static String username;
  private static String password;
  private static String authUrl;
  private static RgwAdmin RGW_ADMIN_CLIENT;

  private static void example() {
    // Create a Connection
    AccountConfig config = new AccountConfig();
    config.setUsername(username);
    config.setPassword(password);
    config.setAuthUrl(authUrl);
    config.setAuthenticationMethod(AuthenticationMethod.BASIC);
    Account account = new AccountFactory(config).createAccount();

    // Create a Container
    Container container = account.getContainer("my-new-container");
    container.create();

    // Create an Object
    StoredObject object = container.getObject("foo.txt");
    object.uploadObject(new File("src/test/resources/foo.txt"));

    // Add/Update Object Metadata
    Map<String, Object> metadata = new TreeMap<>();
    metadata.put("key", "value");
    object.setMetadata(metadata);

    // List Owned Containers
    Collection<Container> containers = account.list();
    for (Container currentContainer : containers) {
      System.out.println(currentContainer.getName());
    }

    // List a Container's Content
    Collection<StoredObject> objects = container.list();
    for (StoredObject currentObject : objects) {
      System.out.println(currentObject.getName());
    }

    // Retrieve an Object's Metadata
    Map<String, Object> returnedMetadata = object.getMetadata();
    for (String name : returnedMetadata.keySet()) {
      System.out.println("META / " + name + ": " + returnedMetadata.get(name));
    }

    // Retrieve an Object
    object.downloadObject(new File("/tmp/outfile.txt"));

    // Delete an Object
    object.delete();

    // Delete a Container
    container.delete();
  }

  private static void init() throws IOException {
    String env = System.getProperty("env", "");
    if (!"".equals(env)) {
      env = "." + env;
    }
    Properties properties = new Properties();
    properties.load(SwiftExample.class.getResourceAsStream("/rgwadmin.properties" + env));

    String adminUserId = properties.getProperty("radosgw.adminId");
    String accessKey = properties.getProperty("radosgw.adminAccessKey");
    String secretKey = properties.getProperty("radosgw.adminSecretKey");
    String s3Endpoint = properties.getProperty("radosgw.endpoint");
    String adminEndpoint = properties.getProperty("radosgw.adminEndpoint");
    authUrl = s3Endpoint + "/auth/1.0";

    RGW_ADMIN_CLIENT = new RgwAdminImpl(accessKey, secretKey, adminEndpoint);
    User response = RGW_ADMIN_CLIENT.createUser(userId);

    String subUserId = UUID.randomUUID().toString();
    RGW_ADMIN_CLIENT.createSubUser(
        response.getUserId(), subUserId, SubUser.Permission.FULL, CredentialType.SWIFT);

    User response2 = RGW_ADMIN_CLIENT.getUserInfo(response.getUserId()).get();
    username = response2.getSwiftCredentials().get(0).getUsername();
    password = response2.getSwiftCredentials().get(0).getPassword();
  }

  public static void main(String[] args) throws IOException {
    init();
    try {
      example();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      RGW_ADMIN_CLIENT.removeUser(userId);
    }
  }
}
