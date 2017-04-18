package org.twonote.rgwadmin4j.examples;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.google.common.collect.ImmutableMap;
import org.javaswift.joss.exception.CommandException;
import org.javaswift.joss.model.Account;
import org.junit.Ignore;
import org.junit.Test;
import org.twonote.rgwadmin4j.impl.BaseTest;
import org.twonote.rgwadmin4j.model.S3Credential;
import org.twonote.rgwadmin4j.model.SubUser;
import org.twonote.rgwadmin4j.model.User;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

// Remove @Ignore before run
/** Created by hrchu on 2017/4/10. */
public class SubUserExample extends BaseTest {
  @Test
  @Ignore("Not a test")
  public void subuserWithSwiftCredentialPermission() throws Exception {
    // access=write
    testWithAUser(
        v -> {
          String access = "write";
          RGW_ADMIN.createSubUser(
              v.getUserId(), UUID.randomUUID().toString(), ImmutableMap.of("access", access));
          v = RGW_ADMIN.getUserInfo(v.getUserId()).get();
          try {
            Account childSwift =
                createSwift(
                    v.getSwiftCredentials().get(0).getUserId(),
                    v.getSwiftCredentials().get(0).getPassword());
            fail();
          } catch (CommandException e) {
            assertEquals(403, e.getHttpStatusCode());
          }
        });

    // access=read
    testWithAUser(
        v -> {
          String access = "read";
          RGW_ADMIN.createSubUser(
              v.getUserId(), UUID.randomUUID().toString(), ImmutableMap.of("access", access));
          v = RGW_ADMIN.getUserInfo(v.getUserId()).get();
          Account childSwift =
              createSwift(
                  v.getSwiftCredentials().get(0).getUserId(),
                  v.getSwiftCredentials().get(0).getPassword());

          try {
            createSomeObjects(childSwift);
            fail();
          } catch (CommandException e) {
            assertEquals(403, e.getHttpStatusCode());
          }
        });

    // Cannot distinguish difference between access=readwrite and access=full....
    testWithAUser(
        v -> {
          String access = "readwrite";
          RGW_ADMIN.createSubUser(
              v.getUserId(), UUID.randomUUID().toString(), ImmutableMap.of("access", access));
          v = RGW_ADMIN.getUserInfo(v.getUserId()).get();
          Account childSwift =
              createSwift(
                  v.getSwiftCredentials().get(0).getUserId(),
                  v.getSwiftCredentials().get(0).getPassword());
          createSomeObjects(childSwift);
          childSwift.getContainer(UUID.randomUUID().toString()).create().makePublic();
        });
  }

  @Test
  @Ignore("Not a test")
  public void subUserWithSwiftCredentialIncorporated() throws Exception {
    testWithASubUser(
        v -> {
          AmazonS3 parentS3 =
              createS3(
                  v.getS3Credentials().get(0).getAccessKey(),
                  v.getS3Credentials().get(0).getSecretKey());

          Account childSwift =
              createSwift(
                  v.getSwiftCredentials().get(0).getUserId(),
                  v.getSwiftCredentials().get(0).getPassword());

          createSomeObjects(parentS3);

          createSomeObjects(childSwift);

          // The S3 bucket created by parent user and the SwiftExample container created by child subuser are incorporated.
          assertEquals(2, parentS3.listBuckets().size());
          assertEquals(2, childSwift.list().size());

          // object is also incorporated.
          assertEquals(
              6,
              parentS3
                  .listBuckets()
                  .stream()
                  .flatMap(bk -> parentS3.listObjects(bk.getName()).getObjectSummaries().stream())
                  .count());
          assertEquals(6, childSwift.list().stream().flatMap(bk -> bk.list().stream()).count());

          // files uploaded by child are owned by parent...swift does not have concept of object owner?
          assertTrue(
              parentS3
                  .listBuckets()
                  .stream()
                  .flatMap(bk -> parentS3.listObjects(bk.getName()).getObjectSummaries().stream())
                  .allMatch(o -> o.getOwner().getId().equals(v.getUserId())));
        });
  }

  @Test
  @Ignore("Not a test")
  public void subuserWithS3CredentialIncorporated() throws Exception {
    testWithUserAndS3(
        (user, s3) -> {
          createSomeObjects(s3);

          List<SubUser> subUser =
              RGW_ADMIN.createSubUser(
                  user.getUserId(), "QQQ", ImmutableMap.of("key-type", "s3", "access", "full"));

          User userInfo = RGW_ADMIN.getUserInfo(user.getUserId()).get();

          S3Credential subUserKey =
              userInfo
                  .getS3Credentials()
                  .stream()
                  .filter(v -> v.getUserId().equals(subUser.get(0).getId()))
                  .findFirst()
                  .get();

          AmazonS3 subUserS3 = createS3(subUserKey.getAccessKey(), subUserKey.getSecretKey());

          createSomeObjects(subUserS3);

          // The S3 bucket created by parent user and created by child subuser are incorporated.
          assertEquals(s3.listBuckets().size(), subUserS3.listBuckets().size());

          for (String bucketName :
              s3.listBuckets().stream().map(v -> v.getName()).collect(Collectors.toList())) {
            assertEquals(
                s3.listObjects(bucketName).getObjectSummaries().toString(),
                subUserS3.listObjects(bucketName).getObjectSummaries().toString());
            subUserS3.getBucketAcl(bucketName);
            subUserS3.setBucketAcl(bucketName, CannedAccessControlList.AuthenticatedRead);
          }
        });
  }
}
