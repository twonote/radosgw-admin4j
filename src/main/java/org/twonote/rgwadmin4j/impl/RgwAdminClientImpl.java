package org.twonote.rgwadmin4j.impl;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import okhttp3.*;
import org.twonote.rgwadmin4j.RgwAdminClient;
import org.twonote.rgwadmin4j.model.CreateUserResponse;
import org.twonote.rgwadmin4j.model.GetBucketInfoResponse;
import org.twonote.rgwadmin4j.model.GetUserInfoResponse;
import org.twonote.rgwadmin4j.model.Quota;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A.K.A. S3 admin as you (should) know...
 * <p>
 * Created by petertc on 2/16/17.
 */
public class RgwAdminClientImpl implements RgwAdminClient {

    private final String accessKey;
    private final String secretKey;
    private final String endpoint;

    private static final OkHttpClient client = new OkHttpClient();
    private static final RequestBody emptyBody = RequestBody.create(null, new byte[]{});
    private static final Gson gson = new Gson();

    public RgwAdminClientImpl(String accessKey, String secretKey, String endpoint) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.endpoint = endpoint;
    }

    private String sign(String httpVerb, String date, String resource) {
        return sign(httpVerb, "", "", date, resource, null);
    }

    private String sign(String httpVerb, String contentMD5,
                        String contentType, String date, String resource,
                        Map<String, String> metas) {

        String stringToSign = httpVerb + "\n"
                + CharMatcher.whitespace().trimFrom(contentMD5) + "\n"
                + CharMatcher.whitespace().trimFrom(contentType) + "\n" + date + "\n";
        if (metas != null) {
            for (Map.Entry<String, String> entity : metas.entrySet()) {
                stringToSign += CharMatcher.whitespace().trimFrom(entity.getKey()) + ":"
                        + CharMatcher.whitespace().trimFrom(entity.getValue()) + "\n";
            }
        }
        stringToSign += resource;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            byte[] keyBytes = secretKey.getBytes("UTF8");
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
            mac.init(signingKey);
            byte[] signBytes = mac.doFinal(stringToSign.getBytes("UTF8"));
            String signature = encodeBase64(signBytes);
            return "AWS" + " " + accessKey + ":" + signature;
        } catch (Exception e) {
            throw new RuntimeException("MAC CALC FAILED.");
        }

    }

    private static String encodeBase64(byte[] data) {
        String base64 = new String(Base64.getEncoder().encodeToString(data));
        if (base64.endsWith("\r\n"))
            base64 = base64.substring(0, base64.length() - 2);
        if (base64.endsWith("\n"))
            base64 = base64.substring(0, base64.length() - 1);

        return base64;
    }

    /**
     *
     * @param uid
     * @param userCaps In forms of [users|buckets|metadata|usage|zone]=[*|read|write|read, write]
     */
    @Override
    public void addUserCapability(String uid, String userCaps) {
        String resource = "/admin/user/";
        String url = endpoint + resource + "?caps"
            + "&uid=" + uid
            + "&user-caps=" + userCaps;
        String method = "PUT";
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
        String sign = sign(method, date, resource);

        Request request = new Request.Builder().put(emptyBody)
            .url(url)
            .header("Date", date)
            .header("Authorization", sign)
            .build();

        safeCall(request);

    }

    /**
     *
     * @param uid
     * @param userCaps In forms of [users|buckets|metadata|usage|zone]=[*|read|write|read, write]
     */
    @Override
    public void deleteUserCapability(String uid, String userCaps) {
        String resource = "/admin/user/";
        String url = endpoint + resource + "?caps"
            + "&uid=" + uid
            + "&user-caps=" + userCaps;
        String method = "DELETE";
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
        String sign = sign(method, date, resource);

        Request request = new Request.Builder().delete()
            .url(url)
            .header("Date", date)
            .header("Authorization", sign)
            .build();

        safeCall(request);

    }

    /**
     * The operation is success if the target is not exist in the system after the operation is executed.
     * The operation does not throw exception even if the target is not exist in the beginning.
     *
     * @param bucketName
     */
    @Override
    public void removeBucket(String bucketName) {
        String resource = "/admin/bucket/";
        String url = endpoint + resource + "?"
                + "bucket=" + bucketName
                + "&purge-objects=true";
        String method = "DELETE";
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
        String sign = sign(method, date, resource);

        Request request = new Request.Builder().delete()
                .url(url)
                .header("Date", date)
                .header("Authorization", sign)
                .build();

        safeCall(request);

    }

    @Override
    public void linkBucket(String bucketName, String bucketId, String userId) {
        String resource = "/admin/bucket/";
        String url = endpoint + resource + "?"
                + "bucket=" + bucketName
                + "&bucket-id=" + bucketId
                + "&uid=" + userId;
        String method = "PUT";
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
        String sign = sign(method, date, resource);

        Request request = new Request.Builder().put(emptyBody)
                .url(url)
                .header("Date", date)
                .header("Authorization", sign)
                .build();

        safeCall(request);
    }

    @Override
    public Optional<GetBucketInfoResponse> getBucketInfo(String bucketName) {
        String resource = "/admin/bucket/";
        String url = endpoint + resource + "?"
                + "bucket=" + bucketName;
        String method = "GET";
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
        String sign = sign(method, date, resource);

        Request request = new Request.Builder()
                .url(url)
                .header("Date", date)
                .header("Authorization", sign)
                .build();

        String resp = safeCall(request);
        return Optional.ofNullable(gson.fromJson(resp, GetBucketInfoResponse.class));
    }

    /**
     * Guarantee that the request is execute success and the connection is closed
     *
     * @param request
     * @return resp body in str; null if no body or status code == 404
     * @throws RuntimeException if resp code != 200 or anything is wrong
     */
    private String safeCall(Request request) {
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 404) {
                return null;
            }
            if (!response.isSuccessful()) {
                throw new RuntimeException("Unexpected code: message: " + response + "body:" + response.body().string());
            }
            ResponseBody body = response.body();
            if (body != null) {
                return response.body().string();
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CreateUserResponse createUser(String userId) {
        return createUser(userId, false);
    }

    /**
     * Create user with limit
     *
     * @param userId
     * @param isLimit if specify, user can only have one bucket, and quota is 1TiB
     * @return
     */
    // TODO: quota
    @Override
    public CreateUserResponse createUser(String userId, boolean isLimit) {
        String resource = "/admin/user/";
        String url = endpoint + resource + "?"
                + "uid=" + userId
                + "&display-name=" + userId
                //                  + "&email=abc@xxx.com"
                //					+ "&key-type=s3"
//                + "&access-key=AOA"
//                + "&secret-key=AOA"
                + "&suspended=False";

        if (isLimit) {
            url += "&max-buckets=1";
        }

        String method = "PUT";
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
        String sign = sign(method, date, resource);

        Request request = new Request.Builder().put(emptyBody)
                .url(url)
                .header("Date", date)
                .header("Authorization", sign)
                .build();

        String resp = safeCall(request);
        return gson.fromJson(resp, CreateUserResponse.class);
    }

    @Override
    public Optional<GetUserInfoResponse> getUserInfo(String userId) {
        String resource = "/admin/user/";
        String url = endpoint + resource + "?"
                + "uid=" + userId;
        String method = "GET";
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
        String sign = sign(method, date, resource);

        Request request = new Request.Builder()
                .url(url)
                .header("Date", date)
                .header("Authorization", sign)
                .build();

        String resp = safeCall(request);
        return Optional.ofNullable(gson.fromJson(resp, GetUserInfoResponse.class));
    }

    @Override
    public void modifyUser(String userId, Map<String, String> parameters) {
        String resource = "/admin/user/";
        String url = endpoint + resource + "?"
                + "uid=" + userId;
        String _parameters = parameters.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        url += "&" + _parameters;

        String method = "POST";
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
        String sign = sign(method, date, resource);

        Request request = new Request.Builder().post(emptyBody)
                .url(url)
                .header("Date", date)
                .header("Authorization", sign)
                .build();

        safeCall(request);
    }

    @Override
    public void suspendUser(String userId) {
        String resource = "/admin/user/";
        String url = endpoint + resource + "?"
                + "uid=" + userId
//                + "&display-name=" + userId
                //                  + "&email=abc@xxx.com"
                //					+ "&key-type=s3"
//                + "&access-key=AOA"
//                + "&secret-key=AOA"
//                + "&max-buckets=1"
                + "&suspended=True";
        String method = "POST";
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
        String sign = sign(method, date, resource);

        Request request = new Request.Builder().post(emptyBody)
                .url(url)
                .header("Date", date)
                .header("Authorization", sign)
                .build();

        safeCall(request);
    }

    /**
     * The operation is success if the user is not exist in the system after the operation is executed.
     * The operation does not throw exception even if the user is not exist in the beginning.
     *
     * @param userId
     */
    @Override
    public void removeUser(String userId) {
        String resource = "/admin/user/";
        String url = endpoint + resource + "?"
                + "uid=" + userId
                + "&purge-data=true";
        String method = "DELETE";
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
        String sign = sign(method, date, resource);

        Request request = new Request.Builder().delete()
                .url(url)
                .header("Date", date)
                .header("Authorization", sign)
                .build();

        safeCall(request);

    }

    @Override
    public Optional<Quota> getUserQuota(String userId) {
        String resource = "/admin/user/";
        String url = endpoint + resource + "?"
                + "quota"
                + "&uid=" + userId
                + "&quota-type=bucket";

        String method = "GET";
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
        String sign = sign(method, date, resource);

        Request request = new Request.Builder().get()
                .url(url)
                .header("Date", date)
                .header("Authorization", sign)
                .build();

        String resp = safeCall(request);
        return Optional.ofNullable(gson.fromJson(resp, Quota.class));

    }

    /**
     * @param userId
     * @param maxObjects The max-objects setting allows you to specify the maximum number of objects. A negative value disables this setting.
     * @param maxSize    The max-size option allows you to specify a quota for the maximum number of bytes. A negative value disables this setting.
     */
    @Override
    public void setUserQuota(String userId, long maxObjects, long maxSize) {
        String resource = "/admin/user/";
        String url = endpoint + resource + "?"
                + "quota"
                + "&uid=" + userId
                + "&quota-type=bucket";


        String method = "PUT";
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
        String sign = sign(method, date, resource);

        String body = gson.toJson(ImmutableMap.of(
                "max_objects", String.valueOf(maxObjects),
                "max_size_kb", String.valueOf(maxSize),
                "enabled", "true"));

        Request request = new Request.Builder().put(RequestBody.create(null, body))
                .url(url)
                .header("Date", date)
                .header("Authorization", sign)
                .build();

        safeCall(request);

    }
}
