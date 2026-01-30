package org.twonote.rgwadmin4j.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.twonote.rgwadmin4j.model.Account;

/**
 * Tests for Ceph Squid Account Management operations.
 *
 * <p>Note: These tests require a Ceph Squid or later instance with proper capabilities configured.
 * Tests are automatically enabled for Squid+ versions and skipped for older versions.
 */
public class RgwAdminAccountManagementTest extends BaseTest {

  private static final String CEPH_VERSION_PROPERTY = "ceph.version";
  
  /**
   * Check if the current Ceph version supports Account Management (Squid or later).
   * Tests will be skipped if running on older Ceph versions.
   */
  @Before
  public void checkCephVersion() {
    String cephVersion = System.getProperty(CEPH_VERSION_PROPERTY, "");
    boolean isSquidOrLater = cephVersion.contains("squid") || cephVersion.contains("tentacle");
    assumeTrue("Account Management tests require Ceph Squid or later. Current version: " + cephVersion, 
               isSquidOrLater);
  }

  /**
   * Test creating an account with auto-generated ID.
   */
  @Test
  public void testCreateAccountBasic() {
    String accountName = "test-account-" + UUID.randomUUID().toString();
    String accountId = null;

    try {
      Account account = RGW_ADMIN.createAccount(accountName);

      assertNotNull("Account should not be null", account);
      assertEquals("Account name should match", accountName, account.getName());
      assertNotNull("Account ID should be generated", account.getId());
      assertTrue(
          "Account ID should have RGW prefix",
          account.getId() != null && account.getId().startsWith("RGW"));
      
      accountId = account.getId();
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
   * Test creating an account with custom parameters.
   */
  @Test
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
   */
  @Test
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
   */
  @Test
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
   */
  @Test
  public void testRemoveAccount() {
    String accountName = "test-account-" + UUID.randomUUID().toString();
    String accountId = null;

    try {
      Account created = RGW_ADMIN.createAccount(accountName);
      accountId = created.getId();

      RGW_ADMIN.removeAccount(accountId);

      Optional<Account> retrieved = RGW_ADMIN.getAccountInfo(accountId);
      assertFalse("Account should not exist after removal", retrieved.isPresent());
      
      // Mark as cleaned up so finally block doesn't attempt double removal
      accountId = null;
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
}
