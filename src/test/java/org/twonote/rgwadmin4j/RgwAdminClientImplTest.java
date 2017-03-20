package org.twonote.rgwadmin4j;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.common.collect.ImmutableMap;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.twonote.rgwadmin4j.impl.RgwAdminClientImpl;
import org.twonote.rgwadmin4j.model.CreateUserResponse;
import org.twonote.rgwadmin4j.model.GetBucketInfoResponse;
import org.twonote.rgwadmin4j.model.GetUserInfoResponse;
import org.twonote.rgwadmin4j.model.Quota;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

public class RgwAdminClientImplTest {
    private static RgwAdminClient RGW_ADMIN_CLIENT;

    private static String adminUserId;
    private static String accessKey;
    private static String secretKey;
    private static String s3Endpoint;

    @BeforeClass
    public static void init() throws IOException {
        initPros();
        RGW_ADMIN_CLIENT = new RgwAdminClientImpl(accessKey, secretKey, s3Endpoint);
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
        s3Endpoint = properties.getProperty("radosgw.adminEndpoint");
    }

    /**
     * Creates a temporary file with text data to demonstrate uploading a file
     * to Amazon S3
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
            RGW_ADMIN_CLIENT.createUser(userId, false);

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
            } catch (Exception e) {}
        }
    }

    @Test
    public void deleteUserCapability() throws Exception {
        String userId = "test" + UUID.randomUUID().toString();
        String userCaps = "usage=read";

        // user not exist
        try {
            RGW_ADMIN_CLIENT.deleteUserCapability(userId, userCaps);
        } catch (RuntimeException e) {
            // 400
        }

        try {
            RGW_ADMIN_CLIENT.createUser(userId, false);

            // cap not exist
            RGW_ADMIN_CLIENT.deleteUserCapability(userId, userCaps);

            // basic
            RGW_ADMIN_CLIENT.addUserCapability(userId, userCaps);
            RGW_ADMIN_CLIENT.deleteUserCapability(userId, userCaps);
            GetUserInfoResponse response = RGW_ADMIN_CLIENT.getUserInfo(userId).get();
            assertEquals(0, response.getCaps().size());

            // do it again
            RGW_ADMIN_CLIENT.deleteUserCapability(userId, userCaps);
        } finally {
            try {
                RGW_ADMIN_CLIENT.removeUser(userId);
            } catch (Exception e) {}
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
            CreateUserResponse response = RGW_ADMIN_CLIENT.createUser(userId, false);
            AmazonS3 s3 = initS3(response.getKeys().get(0).getAccessKey(), response.getKeys().get(0).getSecretKey(), s3Endpoint);
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
    public void linkBucket() throws Exception {
        String userId = "linkbkusr" + UUID.randomUUID().toString();
        String bucketName = "linkbkusrbk" + UUID.randomUUID().toString();
        try {
            CreateUserResponse response = RGW_ADMIN_CLIENT.createUser(userId, false);
            AmazonS3 s3 = initS3(response.getKeys().get(0).getAccessKey(), response.getKeys().get(0).getSecretKey(), s3Endpoint);
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
            CreateUserResponse response = RGW_ADMIN_CLIENT.createUser(userId, false);
            AmazonS3 s3 = initS3(response.getKeys().get(0).getAccessKey(), response.getKeys().get(0).getSecretKey(), s3Endpoint);
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
        RGW_ADMIN_CLIENT.createUser(userId, false);

        // basic
        RGW_ADMIN_CLIENT.modifyUser(userId, ImmutableMap.of("max-buckets", String.valueOf(Integer.MAX_VALUE)));
        GetUserInfoResponse response = RGW_ADMIN_CLIENT.getUserInfo(userId).get();
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), response.getMaxBuckets());

        // user not exist
        RGW_ADMIN_CLIENT.modifyUser(userId + "qqqq", ImmutableMap.of("max-buckets", String.valueOf(Integer.MAX_VALUE)));

        // ignore call with wrong arguments
        RGW_ADMIN_CLIENT.modifyUser(userId, ImmutableMap.of("QQQQQ", String.valueOf(Integer.MAX_VALUE)));
        RGW_ADMIN_CLIENT.modifyUser(userId, ImmutableMap.of("max-buckets", "you-know-my-name"));
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), response.getMaxBuckets());

    }

    @Test
    public void removeUser() throws Exception {
        // The operation is success if the user is not exist in the system after the operation is executed.
        String userId = "testRemoveUserId";
        RGW_ADMIN_CLIENT.createUser(userId, false);
        RGW_ADMIN_CLIENT.removeUser(userId);
        assertFalse(RGW_ADMIN_CLIENT.getUserInfo(userId).isPresent());

        // The operation does not throw exception even if the user is not exist in the beginning.
        RGW_ADMIN_CLIENT.removeUser(userId);
    }

    // TODO: quota
    @Test
    public void createUser() throws Exception {
        String userId = "bobx" + UUID.randomUUID().toString();
        try {
            CreateUserResponse response = RGW_ADMIN_CLIENT.createUser(userId, false);
            assertEquals(userId, response.getUserId());
            assertNotNull(response.getKeys().get(0).getAccessKey());
            assertNotNull(response.getKeys().get(0).getSecretKey());
            assertEquals(Integer.valueOf(0), response.getSuspended());
            assertNotEquals(Integer.valueOf(1), response.getMaxBuckets());

            // create again / should no effect
            response = RGW_ADMIN_CLIENT.createUser(userId, false);
            assertEquals(userId, response.getUserId());
            assertNotNull(response.getKeys().get(0).getAccessKey());
            assertNotNull(response.getKeys().get(0).getSecretKey());
            assertEquals(Integer.valueOf(0), response.getSuspended());
            assertNotEquals(Integer.valueOf(1), response.getMaxBuckets());
        } finally {
            RGW_ADMIN_CLIENT.removeUser(userId);
        }


        // limit user
        String limitUserId = "bobx" + UUID.randomUUID().toString();
        try {
            CreateUserResponse response = RGW_ADMIN_CLIENT.createUser(userId, true);
            assertEquals(Integer.valueOf(1), response.getMaxBuckets());
        } finally {
            RGW_ADMIN_CLIENT.removeUser(limitUserId);
        }

    }

    @Test
    public void getUserInfo() throws Exception {
        // basic
        GetUserInfoResponse response = RGW_ADMIN_CLIENT.getUserInfo(adminUserId).get();
        assertEquals(Integer.valueOf(0), response.getSuspended());
        assertEquals(adminUserId, response.getUserId());
        List<Map<String, String>> caps =Arrays.asList(ImmutableMap.of("type", "users", "perm", "*"),
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

    //    @Test
    // FIXME: ceph rgw quota is buggy in 0.9, skip it.
    public void getAndSetUserQuota() throws Exception {
        String userId = "bobx" + UUID.randomUUID().toString();
        try {
            CreateUserResponse response = RGW_ADMIN_CLIENT.createUser(userId);
            Quota quota;

            // default false
            quota = RGW_ADMIN_CLIENT.getUserQuota(userId).get();
            assertEquals(false, quota.getEnabled());
            assertEquals(Integer.valueOf(-1), quota.getMaxObjects());
            assertEquals(Integer.valueOf(-1), quota.getMaxSizeKb());

            // set quota
            RGW_ADMIN_CLIENT.setUserQuota(userId, 1, 1);
            quota = RGW_ADMIN_CLIENT.getUserQuota(userId).get();
            assertEquals(true, quota.getEnabled());
            assertEquals(Integer.valueOf(1), quota.getMaxObjects());
            assertEquals(Integer.valueOf(4), quota.getMaxSizeKb());

            AmazonS3 s3 = initS3(response.getKeys().get(0).getAccessKey(), response.getKeys().get(0).getSecretKey(), s3Endpoint);
            s3.createBucket(userId);

            // deny obj in 6 bytes
            try {
                s3.putObject(userId, userId, "qqqqqq");
            } catch (AmazonS3Exception e) {
                assertEquals("QuotaExceeded", e.getErrorCode());
            }

            // allow 1 obj
            s3.putObject(userId, userId, "qqqq");

            // deny 2 obj
            try {
                s3.putObject(userId, userId, "qqqq");
            } catch (AmazonS3Exception e) {
                assertEquals("QuotaExceeded", e.getErrorCode());
            }


        } finally {
            RGW_ADMIN_CLIENT.removeUser(userId);
        }

        // not exist
        RGW_ADMIN_CLIENT.setUserQuota(UUID.randomUUID().toString(), 1, 1);

    }

}
