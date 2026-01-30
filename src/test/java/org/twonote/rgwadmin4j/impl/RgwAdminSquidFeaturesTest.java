package org.twonote.rgwadmin4j.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Ignore;
import org.junit.Test;
import org.twonote.rgwadmin4j.model.Account;
import org.twonote.rgwadmin4j.model.ClusterInfo;
import org.twonote.rgwadmin4j.model.User;

/**
 * Tests for new Ceph Squid features: Account Management and Info endpoint.
 *
 * <p>Note: These tests require a Ceph Squid or later instance with proper capabilities configured.
 */
public class RgwAdminSquidFeaturesTest extends BaseTest {

  /**
   * Test the /info endpoint to retrieve cluster information.
   *
   * <p>This test is ignored by default as it requires Ceph Squid or later and the 'info=read'
   * capability.
   */
  @Test
  @Ignore("Requires Ceph Squid or later with info=read capability")
  public void testGetClusterInfo() {
    Optional<ClusterInfo> clusterInfo = RGW_ADMIN.getClusterInfo();

    assertTrue("Cluster info should be present", clusterInfo.isPresent());
    assertNotNull("Cluster ID should not be null", clusterInfo.get().getClusterId());
  }

  /**
   * Test creating an account with auto-generated ID.
   *
   * <p>This test is ignored by default as it requires Ceph Squid or later.
   */
  @Test
  @Ignore("Requires Ceph Squid or later")
  public void testCreateAccountBasic() {
    String accountName = "test-account-" + UUID.randomUUID().toString();

    try {
      Account account = RGW_ADMIN.createAccount(accountName);

      assertNotNull("Account should not be null", account);
      assertEquals("Account name should match", accountName, account.getName());
      assertNotNull("Account ID should be generated", account.getId());
      assertTrue(
          "Account ID should have RGW prefix",
          account.getId() != null && account.getId().startsWith("RGW"));
    } finally {
      try {
        // Clean up - retrieve account ID and remove
        // Note: This is a simplified cleanup; actual implementation may need account listing
      } catch (Exception e) {
        // Ignore cleanup errors in test
      }
    }
  }

  /**
   * Test creating an account with custom parameters.
   *
   * <p>This test is ignored by default as it requires Ceph Squid or later.
   */
  @Test
  @Ignore("Requires Ceph Squid or later")
  public void testCreateAccountWithParameters() {
    String accountName = "test-account-" + UUID.randomUUID().toString();
    String accountId = null;

    try {
      Map<String, String> parameters =
          ImmutableMap.of(
              "email", "test@example.com",
              "max-users", "10",
              "max-buckets", "100");

      Account account = RGW_ADMIN.createAccount(accountName, parameters);
      accountId = account.getId();

      assertNotNull("Account should not be null", account);
      assertEquals("Account name should match", accountName, account.getName());
      assertEquals("Email should match", "test@example.com", account.getEmail());
      assertEquals("Max users should match", Integer.valueOf(10), account.getMaxUsers());
      assertEquals("Max buckets should match", Integer.valueOf(100), account.getMaxBuckets());
    } finally {
      if (accountId != null) {
        try {
          RGW_ADMIN.removeAccount(accountId);
        } catch (Exception e) {
          // Ignore cleanup errors in test
        }
      }
    }
  }

  /**
   * Test getting account information.
   *
   * <p>This test is ignored by default as it requires Ceph Squid or later.
   */
  @Test
  @Ignore("Requires Ceph Squid or later")
  public void testGetAccountInfo() {
    String accountName = "test-account-" + UUID.randomUUID().toString();
    String accountId = null;

    try {
      Account created = RGW_ADMIN.createAccount(accountName);
      accountId = created.getId();

      Optional<Account> retrieved = RGW_ADMIN.getAccountInfo(accountId);

      assertTrue("Account should be found", retrieved.isPresent());
      assertEquals("Account ID should match", accountId, retrieved.get().getId());
      assertEquals("Account name should match", accountName, retrieved.get().getName());
    } finally {
      if (accountId != null) {
        try {
          RGW_ADMIN.removeAccount(accountId);
        } catch (Exception e) {
          // Ignore cleanup errors in test
        }
      }
    }
  }

  /**
   * Test modifying an existing account.
   *
   * <p>This test is ignored by default as it requires Ceph Squid or later.
   */
  @Test
  @Ignore("Requires Ceph Squid or later")
  public void testModifyAccount() {
    String accountName = "test-account-" + UUID.randomUUID().toString();
    String accountId = null;

    try {
      Account created = RGW_ADMIN.createAccount(accountName);
      accountId = created.getId();

      Map<String, String> modifications =
          ImmutableMap.of(
              "email", "modified@example.com",
              "max-buckets", "200");

      Account modified = RGW_ADMIN.modifyAccount(accountId, modifications);

      assertEquals("Account ID should remain the same", accountId, modified.getId());
      assertEquals("Email should be updated", "modified@example.com", modified.getEmail());
      assertEquals("Max buckets should be updated", Integer.valueOf(200), modified.getMaxBuckets());
    } finally {
      if (accountId != null) {
        try {
          RGW_ADMIN.removeAccount(accountId);
        } catch (Exception e) {
          // Ignore cleanup errors in test
        }
      }
    }
  }

  /**
   * Test removing an account.
   *
   * <p>This test is ignored by default as it requires Ceph Squid or later.
   */
  @Test
  @Ignore("Requires Ceph Squid or later")
  public void testRemoveAccount() {
    String accountName = "test-account-" + UUID.randomUUID().toString();

    Account created = RGW_ADMIN.createAccount(accountName);
    String accountId = created.getId();

    RGW_ADMIN.removeAccount(accountId);

    Optional<Account> retrieved = RGW_ADMIN.getAccountInfo(accountId);
    assertFalse("Account should not exist after removal", retrieved.isPresent());
  }

  /**
   * Test getUserInfo with parameters for enhanced security.
   *
   * <p>This test verifies that the new getUserInfo overload with parameters works correctly.
   */
  @Test
  public void testGetUserInfoWithParameters() {
    String userId = "rgwAdmin4jTest-" + UUID.randomUUID().toString();

    try {
      User created = RGW_ADMIN.createUser(userId);

      // Test with empty parameters - should behave like the original method
      Map<String, String> parameters = ImmutableMap.of();
      Optional<User> retrieved = RGW_ADMIN.getUserInfo(userId, parameters);

      assertTrue("User should be found", retrieved.isPresent());
      assertEquals("User ID should match", userId, retrieved.get().getUserId());
      assertNotNull("S3 credentials should be present", retrieved.get().getS3Credentials());
      assertFalse("S3 credentials should not be empty", retrieved.get().getS3Credentials().isEmpty());
    } finally {
      RGW_ADMIN.removeUser(userId);
    }
  }

  /**
   * Test getUserInfo with stats parameter.
   *
   * <p>This test verifies parameter passing works correctly.
   */
  @Test
  public void testGetUserInfoWithStatsParameter() {
    String userId = "rgwAdmin4jTest-" + UUID.randomUUID().toString();

    try {
      User created = RGW_ADMIN.createUser(userId);

      Map<String, String> parameters = ImmutableMap.of("stats", "true");
      Optional<User> retrieved = RGW_ADMIN.getUserInfo(userId, parameters);

      assertTrue("User should be found", retrieved.isPresent());
      assertEquals("User ID should match", userId, retrieved.get().getUserId());
    } finally {
      RGW_ADMIN.removeUser(userId);
    }
  }
}
