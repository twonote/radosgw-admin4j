package org.twonote.rgwadmin4j.examples;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Ignore;
import org.junit.Test;
import org.twonote.rgwadmin4j.impl.BaseTest;
import org.twonote.rgwadmin4j.model.Account;
import org.twonote.rgwadmin4j.model.User;

/**
 * Example of Account Management operations (Ceph Squid+)
 *
 * <p>Account management provides IAM-like multi-tenancy with organizational resource governance.
 * Accounts are hierarchical containers: Account → Users → Buckets
 */
public class AccountExample extends BaseTest {

  /*
   * Account management
   */
  private void accountManagement(String accountName, String userId) {
    // Create an account
    Account account = RGW_ADMIN.createAccount(accountName, "admin@mycompany.com");

    // Get account information
    Optional<Account> accountInfo = RGW_ADMIN.getAccountInfo(account.getAccountId());

    // List all accounts
    List<Account> accounts = RGW_ADMIN.listAccounts();

    // Create an account root user (admin user for the account)
    User rootUser =
        RGW_ADMIN.createUser(
            userId,
            ImmutableMap.of(
                "display-name",
                "Admin User",
                "email",
                "admin@mycompany.com",
                "account-id",
                account.getAccountId(),
                "account-root",
                "true"));

    // Modify account
    Account modifiedAccount =
        RGW_ADMIN.modifyAccount(
            account.getAccountId(), ImmutableMap.of("email", "newemail@mycompany.com"));

    // Remove account
    RGW_ADMIN.removeAccount(account.getAccountId());
  }

  // Remove @Ignore before run
  @Test
  @Ignore("Not a test")
  public void run() {
    String accountName = "example-account-" + UUID.randomUUID().toString();
    String userId = "example-user-" + UUID.randomUUID().toString();
    try {
      accountManagement(accountName, userId);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        RGW_ADMIN.removeUser(userId);
      } catch (Exception e) {
        // Ignore cleanup failures
      }
      try {
        List<Account> accounts = RGW_ADMIN.listAccounts();
        accounts.stream()
            .filter(a -> accountName.equals(a.getAccountName()))
            .findFirst()
            .ifPresent(a -> RGW_ADMIN.removeAccount(a.getAccountId()));
      } catch (Exception e) {
        // Ignore cleanup failures
      }
    }
  }
}
