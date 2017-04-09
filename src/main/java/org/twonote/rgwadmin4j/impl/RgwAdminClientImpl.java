package org.twonote.rgwadmin4j.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import okhttp3.*;
import org.twonote.rgwadmin4j.RgwAdminClient;
import org.twonote.rgwadmin4j.model.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Radosgw administrator implementation
 *
 * <p>Created by petertc on 2/16/17.
 */
public class RgwAdminClientImpl implements RgwAdminClient {
  private static final Gson gson = new Gson();

  private static final RequestBody emptyBody = RequestBody.create(null, new byte[] {});

  private final String endpoint;
  private final OkHttpClient client;

  /**
   * Create a Radosgw administrator implementation
   *
   * @param accessKey Access key of the admin who have proper administrative capabilities.
   * @param secretKey Secret key of the admin who have proper administrative capabilities.
   * @param endpoint Radosgw admin API endpoint, e.g., http://127.0.0.1:80/admin
   */
  public RgwAdminClientImpl(String accessKey, String secretKey, String endpoint) {
    this.client =
        new OkHttpClient().newBuilder().addInterceptor(new S3Auth(accessKey, secretKey)).build();
    this.endpoint = endpoint;
  }

  private static void appendParameters(Map<String, String> parameters, HttpUrl.Builder urlBuilder) {
    if (parameters != null) {
      parameters.forEach(urlBuilder::addQueryParameter);
    }
  }

  @Override
  public void trimUserUsage(String userId, Map<String, String> parameters) {
    if (parameters == null) {
      parameters = new HashMap<>();
    }
    parameters.put("uid", userId);
    trimUsage(parameters);
  }

  @Override
  public void trimUsage(Map<String, String> parameters) {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(endpoint).newBuilder().addPathSegment("usage");

    if (parameters == null) {
      parameters = new HashMap<>();
    }

    parameters.put("remove-all", "True");

    appendParameters(parameters, urlBuilder);

    Request request = new Request.Builder().delete().url(urlBuilder.build()).build();

    safeCall(request);
  }

  @Override
  public Optional<UsageInfo> getUserUsage(String userId) {
    return getUserUsage(userId, null);
  }

  @Override
  public Optional<UsageInfo> getUserUsage(String userId, Map<String, String> parameters) {
    if (parameters == null) {
      parameters = new HashMap<>();
    }
    parameters.put("uid", userId);
    return getUsage(parameters);
  }

  @Override
  public Optional<UsageInfo> getUsage() {
    return getUsage(null);
  }

  @Override
  public Optional<UsageInfo> getUsage(Map<String, String> parameters) {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(endpoint).newBuilder().addPathSegment("usage");

    appendParameters(parameters, urlBuilder);

    Request request = new Request.Builder().get().url(urlBuilder.build()).build();

    String resp = safeCall(request);
    return Optional.ofNullable(gson.fromJson(resp, UsageInfo.class));
  }

  @Override
  public List<Cap> addUserCapability(String userId, List<Cap> userCaps) {
    Request request =
        new Request.Builder()
            .put(emptyBody)
            .url(
                HttpUrl.parse(endpoint)
                    .newBuilder()
                    .addPathSegment("user")
                    .query("caps")
                    .addQueryParameter("uid", userId)
                    .addQueryParameter("user-caps", Joiner.on(";").join(userCaps))
                    .build())
            .build();

    String resp = safeCall(request);
    Type type = new TypeToken<List<Cap>>() {}.getType();
    return gson.fromJson(resp, type);
  }

  @Override
  public List<Cap> removeUserCapability(String userId, List<Cap> userCaps) {
    Request request =
        new Request.Builder()
            .delete()
            .url(
                HttpUrl.parse(endpoint)
                    .newBuilder()
                    .addPathSegment("user")
                    .query("caps")
                    .addQueryParameter("uid", userId)
                    .addQueryParameter("user-caps", Joiner.on(";").join(userCaps))
                    .build())
            .build();

    String resp = safeCall(request);
    Type type = new TypeToken<List<Cap>>() {}.getType();
    return gson.fromJson(resp, type);
  }

  @Override
  public List<SubUser> createSubUser(
      String userId, String subUserId, Map<String, String> parameters) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("subuser")
            .addQueryParameter("uid", userId)
            // TODO:
            .addQueryParameter("generate-secret", "true")
            .addQueryParameter("subuser", subUserId);

    appendParameters(parameters, urlBuilder);

    Request request = new Request.Builder().put(emptyBody).url(urlBuilder.build()).build();

    String resp = safeCall(request);
    Type type = new TypeToken<List<SubUser>>() {}.getType();
    return gson.fromJson(resp, type);
  }

  @Override
  public List<SubUser> createSubUserForSwift(String userId, String subUserId) {
    return createSubUser(userId, subUserId, ImmutableMap.of("access", "full"));
  }

  @Override
  public List<SubUser> modifySubUser(
      String userId, String subUserId, Map<String, String> parameters) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("subuser")
            .addQueryParameter("uid", userId)
            .addQueryParameter("subuser", subUserId);

    appendParameters(parameters, urlBuilder);

    Request request = new Request.Builder().post(emptyBody).url(urlBuilder.build()).build();

    String resp = safeCall(request);
    Type type = new TypeToken<List<SubUser>>() {}.getType();
    return gson.fromJson(resp, type);
  }

  @Override
  public void removeSubUser(String userId, String subUserId) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("subuser")
            .addQueryParameter("uid", userId)
            .addQueryParameter("subuser", subUserId);

    Request request = new Request.Builder().delete().url(urlBuilder.build()).build();

    safeCall(request);
  }

  private List<Key> _createKey(String uid, Map<String, String> parameters) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("key")
            .addQueryParameter("uid", uid);

    appendParameters(parameters, urlBuilder);

    Request request = new Request.Builder().put(emptyBody).url(urlBuilder.build()).build();

    String resp = safeCall(request);
    Type type = new TypeToken<List<Key>>() {}.getType();
    return gson.fromJson(resp, type);
  }

  private void _removeKey(String uid, Map<String, String> parameters) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("key")
            .addQueryParameter("uid", uid);

    appendParameters(parameters, urlBuilder);

    Request request = new Request.Builder().delete().url(urlBuilder.build()).build();

    safeCall(request);
  }

  @Override
  public List<Key> createKey(String userId, String accessKey, String secretKey) {
    return _createKey(
        userId,
        ImmutableMap.of(
            "access-key", accessKey,
            "secret-key", secretKey));
  }

  @Override
  public List<Key> createKey(String userId) {
    return _createKey(userId, ImmutableMap.of("generate-key", "True"));
  }

  @Override
  public void removeKey(String userId, String accessKey) {
    _removeKey(userId, ImmutableMap.of("access-key", accessKey));
  }

  @Override
  public List<Key> createKeyForSubUser(
      String userId, String subUserId, String accessKey, String secretKey) {
    return _createKey(
        userId,
        ImmutableMap.of(
            "subuser", subUserId,
            "access-key", accessKey,
            "secret-key", secretKey,
            "key-type", "s3"));
  }

  @Override
  public List<Key> createKeyForSubUser(String userId, String subUserId) {
    return _createKey(
        userId,
        ImmutableMap.of(
            "subuser", subUserId,
            "key-type", "s3",
            "generate-key", "True"));
  }

  @Override
  public void removeKeyFromSubUser(String userId, String subUserId, String accessKey) {
    _removeKey(
        userId,
        ImmutableMap.of(
            "subuser", subUserId,
            "key-type", "s3",
            "access-key", accessKey));
  }

  @Override
  public List<Key> createSecretForSubUser(String userId, String subUserId, String secret) {
    return _createKey(
        userId,
        ImmutableMap.of(
            "subuser", subUserId,
            "secret-key", secret,
            "key-type", "swift"));
  }

  @Override
  public List<Key> createSecretForSubUser(String userId, String subUserId) {
    return _createKey(
        userId,
        ImmutableMap.of(
            "subuser", subUserId,
            "key-type", "swift",
            "generate-key", "True"));
  }

  @Override
  public void removeSecretFromSubUser(String userId, String subUserId) {
    _removeKey(userId, ImmutableMap.of("subuser", subUserId, "key-type", "swift"));
  }

  /*
   * The operation is success if the target is not exist in the system after the operation is
   * executed. The operation does not throw exception even if the target is not exist in the
   * beginning.
   */
  @Override
  public void removeBucket(String bucketName) {
    Request request =
        new Request.Builder()
            .delete()
            .url(
                HttpUrl.parse(endpoint)
                    .newBuilder()
                    .addPathSegment("bucket")
                    .addQueryParameter("bucket", bucketName)
                    .addQueryParameter("purge-objects", "true")
                    .build())
            .build();

    safeCall(request);
  }

  @Override
  public void linkBucket(String bucketName, String bucketId, String userId) {
    Request request =
        new Request.Builder()
            .put(emptyBody)
            .url(
                HttpUrl.parse(endpoint)
                    .newBuilder()
                    .addPathSegment("bucket")
                    .addQueryParameter("bucket", bucketName)
                    .addQueryParameter("bucket-id", bucketId)
                    .addQueryParameter("uid", userId)
                    .build())
            .build();

    safeCall(request);
  }

  @Override
  public void unlinkBucket(String bucketName, String userId) {
    Request request =
        new Request.Builder()
            .post(emptyBody)
            .url(
                HttpUrl.parse(endpoint)
                    .newBuilder()
                    .addPathSegment("bucket")
                    .addQueryParameter("bucket", bucketName)
                    .addQueryParameter("uid", userId)
                    .build())
            .build();

    safeCall(request);
  }

  @Override
  public Optional<String> checkBucketIndex(
      String bucketName, boolean isCheckObjects, boolean isFix) {
    Request request =
        new Request.Builder()
            .get()
            .url(
                HttpUrl.parse(endpoint)
                    .newBuilder()
                    .addPathSegment("bucket")
                    .query("index")
                    .addQueryParameter("bucket", bucketName)
                    .addQueryParameter("check-objects", Boolean.toString(isCheckObjects))
                    .addQueryParameter("fix", Boolean.toString(isFix))
                    .build())
            .build();

    String resp = safeCall(request);
    return Optional.ofNullable(resp);
  }

  @Override
  public List<String> listBucket(String userId) {
    Request request =
        new Request.Builder()
            .get()
            .url(
                HttpUrl.parse(endpoint)
                    .newBuilder()
                    .addPathSegment("bucket")
                    .addQueryParameter("uid", userId)
                    .addQueryParameter("stats", "False")
                    .build())
            .build();

    String resp = safeCall(request);
    Type type = new TypeToken<List<String>>() {}.getType();

    return gson.fromJson(resp, type);
  }

  @Override
  public List<BucketInfo> listBucketInfo(String userId) {
    return _getBucketInfo(ImmutableMap.of("uid", userId, "stats", "True"));
  }

  private List<BucketInfo> _getBucketInfo(Map<String, String> parameters) {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(endpoint).newBuilder().addPathSegment("bucket");

    appendParameters(parameters, urlBuilder);

    Request request = new Request.Builder().get().url(urlBuilder.build()).build();

    String resp = safeCall(request);

    // ugly part...
    if (parameters.containsKey("uid")) {
      Type type = new TypeToken<List<BucketInfo>>() {}.getType();
      return gson.fromJson(resp, type);
    } else if (parameters.containsKey("bucket")) {
      BucketInfo response = gson.fromJson(resp, BucketInfo.class);
      List<BucketInfo> ret = new ArrayList<>();
      if (response != null) {
        ret.add(response);
      }
      return ret;
    }

    throw new RuntimeException("Parameters should have either uid or bucket");
  }

  @Override
  public Optional<BucketInfo> getBucketInfo(String bucketName) {
    List<BucketInfo> responses =
        _getBucketInfo(ImmutableMap.of("bucket", bucketName, "stats", "True"));
    if (responses.size() == 0) {
      return Optional.empty();
    } else if (responses.size() == 1) {
      return Optional.of(responses.get(0));
    }
    throw new RuntimeException("Server should not return more than one bucket");
  }

  /**
   * Guarantee that the request is execute success and the connection is closed
   *
   * @param request request
   * @return resp body in str; null if no body or status code == 404
   * @throws RgwAdminException if resp code != (200||404)
   */
  private String safeCall(Request request) {
    try (Response response = client.newCall(request).execute()) {
      if (response.code() == 404) {
        return null;
      }
      if (!response.isSuccessful()) {
        throw ErrorUtils.parseError(response);
      }
      ResponseBody body = response.body();
      if (body != null) {
        return response.body().string();
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new RgwAdminException(500, "IOException", e);
    }
  }

  @Override
  public User createUser(String userId) {
    return createUser(userId, null);
  }

  @Override
  public User createUser(String userId, Map<String, String> options) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .addQueryParameter("uid", userId)
            .addQueryParameter("display-name", userId);

    appendParameters(options, urlBuilder);

    Request request = new Request.Builder().put(emptyBody).url(urlBuilder.build()).build();

    String resp = safeCall(request);
    return gson.fromJson(resp, User.class);
  }

  @Override
  public Optional<User> getUserInfo(String userId) {
    Request request =
        new Request.Builder()
            .get()
            .url(
                HttpUrl.parse(endpoint)
                    .newBuilder()
                    .addPathSegment("user")
                    .addQueryParameter("uid", userId)
                    .build())
            .build();

    String resp = safeCall(request);
    return Optional.ofNullable(gson.fromJson(resp, User.class));
  }

  @Override
  public User modifyUser(String userId, Map<String, String> parameters) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .addQueryParameter("uid", userId);

    parameters.forEach(urlBuilder::addQueryParameter);

    Request request = new Request.Builder().post(emptyBody).url(urlBuilder.build()).build();

    String resp = safeCall(request);
    return gson.fromJson(resp, User.class);
  }

  @Override
  public void suspendUser(String userId) {
    modifyUser(userId, ImmutableMap.of("suspended", "true"));
  }

  @Override
  public void suspendUser(String userId, boolean suspend) {
    modifyUser(userId, ImmutableMap.of("suspended", Boolean.toString(suspend)));
  }

  /*
   * The operation is success if the user is not exist in the system after the operation is
   * executed. The operation does not throw exception even if the user is not exist in the
   * beginning.
   */
  @Override
  public void removeUser(String userId) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .addQueryParameter("uid", userId)
            .addQueryParameter("purge-data", "true");

    Request request = new Request.Builder().delete().url(urlBuilder.build()).build();

    safeCall(request);
  }

  @Override
  public Optional<Quota> getUserQuota(String userId) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("quota")
            .addQueryParameter("uid", userId)
            .addQueryParameter("quota-type", "user");

    Request request = new Request.Builder().get().url(urlBuilder.build()).build();

    String resp = safeCall(request);
    return Optional.ofNullable(gson.fromJson(resp, Quota.class));
  }

  @Override
  public void setUserQuota(String userId, long maxObjects, long maxSizeKB) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("quota")
            .addQueryParameter("uid", userId)
            .addQueryParameter("quota-type", "user");

    String body =
        gson.toJson(
            ImmutableMap.of(
                "max_objects", String.valueOf(maxObjects),
                "max_size_kb", String.valueOf(maxSizeKB),
                "enabled", "true"));

    Request request =
        new Request.Builder().put(RequestBody.create(null, body)).url(urlBuilder.build()).build();

    safeCall(request);
  }

  @Override
  public void removeObject(String bucketName, String objectKey) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("bucket")
            .query("object")
            .addQueryParameter("bucket", bucketName)
            .addQueryParameter("object", objectKey);

    Request request = new Request.Builder().delete().url(urlBuilder.build()).build();

    safeCall(request);
  }

  @Override
  public Optional<String> getObjectPolicy(String bucketName, String objectKey) {
    return _getPolicy(bucketName, objectKey);
  }

  @Override
  public Optional<String> getBucketPolicy(String bucketName) {
    return _getPolicy(bucketName, null);
  }

  private Optional<String> _getPolicy(String bucketName, String objectKey) {
    if (Strings.isNullOrEmpty(bucketName)) {
      throw new IllegalArgumentException("no bucketName");
    }

    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("bucket")
            .query("policy")
            .addQueryParameter("bucket", bucketName);

    if (!Strings.isNullOrEmpty(objectKey)) {
      urlBuilder.addQueryParameter("object", objectKey);
    }

    Request request = new Request.Builder().get().url(urlBuilder.build()).build();

    return Optional.ofNullable(safeCall(request));
  }
}
