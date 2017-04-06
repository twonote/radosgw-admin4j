package org.twonote.rgwadmin4j.impl;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import okhttp3.*;
import org.twonote.rgwadmin4j.RgwAdminClient;
import org.twonote.rgwadmin4j.RgwAdminException;
import org.twonote.rgwadmin4j.model.*;
import org.twonote.rgwadmin4j.model.usage.GetUsageResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Created by petertc on 2/16/17. */
public class RgwAdminClientImpl implements RgwAdminClient {
  private static final Gson gson = new Gson();
  private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

  private static final RequestBody emptyBody = RequestBody.create(null, new byte[] {});

  private final String endpoint;
  private final OkHttpClient client;

  public RgwAdminClientImpl(String accessKey, String secretKey, String endpoint) {
    this.client =
        new OkHttpClient().newBuilder().addInterceptor(new S3Auth(accessKey, secretKey)).build();
    this.endpoint = endpoint;
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
  public Optional<GetUsageResponse> getUserUsage(String userId) {
    return getUserUsage(userId, null);
  }

  @Override
  public Optional<GetUsageResponse> getUserUsage(String userId, Map<String, String> parameters) {
    if (parameters == null) {
      parameters = new HashMap<>();
    }
    parameters.put("uid", userId);
    return getUsage(parameters);
  }

  @Override
  public Optional<GetUsageResponse> getUsage() {
    return getUsage(null);
  }

  @Override
  public Optional<GetUsageResponse> getUsage(Map<String, String> parameters) {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(endpoint).newBuilder().addPathSegment("usage");

    appendParameters(parameters, urlBuilder);

    Request request = new Request.Builder().get().url(urlBuilder.build()).build();

    String resp = safeCall(request);
    return Optional.ofNullable(gson.fromJson(resp, GetUsageResponse.class));
  }

  @Override
  public void addUserCapability(String uid, String userCaps) {
    Request request =
        new Request.Builder()
            .put(emptyBody)
            .url(
                HttpUrl.parse(endpoint)
                    .newBuilder()
                    .addPathSegment("user")
                    .query("caps")
                    .addQueryParameter("uid", uid)
                    .addQueryParameter("user-caps", userCaps)
                    .build())
            .build();

    safeCall(request);
  }

  @Override
  public void removeUserCapability(String uid, String userCaps) {
    Request request =
        new Request.Builder()
            .delete()
            .url(
                HttpUrl.parse(endpoint)
                    .newBuilder()
                    .addPathSegment("user")
                    .query("caps")
                    .addQueryParameter("uid", uid)
                    .addQueryParameter("user-caps", userCaps)
                    .build())
            .build();

    safeCall(request);
  }

  @Override
  public List<SubUser> createSubUser(String uid, String subUserId, Map<String, String> parameters) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("subuser")
            .addQueryParameter("uid", uid)
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
  public List<SubUser> createSubUserForSwift(String uid, String subUserId) {
    return createSubUser(uid, subUserId, ImmutableMap.of("access", "full"));
  }

  @Override
  public List<SubUser> modifySubUser(String uid, String subUserId, Map<String, String> parameters) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("subuser")
            .addQueryParameter("uid", uid)
            .addQueryParameter("subuser", subUserId);

    appendParameters(parameters, urlBuilder);

    Request request = new Request.Builder().post(emptyBody).url(urlBuilder.build()).build();

    String resp = safeCall(request);
    Type type = new TypeToken<List<SubUser>>() {}.getType();
    return gson.fromJson(resp, type);
  }

  @Override
  public void removeSubUser(String uid, String subUserId) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("subuser")
            .addQueryParameter("uid", uid)
            .addQueryParameter("subuser", subUserId);

    Request request = new Request.Builder().delete().url(urlBuilder.build()).build();

    safeCall(request);
  }

  @Override
  public List<CreateKeyResponse> createKey(String uid, Map<String, String> parameters) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("key")
            .addQueryParameter("uid", uid);

    appendParameters(parameters, urlBuilder);

    Request request = new Request.Builder().put(emptyBody).url(urlBuilder.build()).build();

    String resp = safeCall(request);
    Type type = new TypeToken<List<CreateKeyResponse>>() {}.getType();
    return gson.fromJson(resp, type);
  }

  private static void appendParameters(Map<String, String> parameters, HttpUrl.Builder urlBuilder) {
    if (parameters != null) {
      parameters.forEach((k, v) -> urlBuilder.addQueryParameter(k, v));
    }
  }

  @Override
  public List<CreateKeyResponse> createKey(String uid) {
    return createKey(uid, null);
  }

  @Override
  public void removeKey(String accessKey, String keyType) {
    Request request =
        new Request.Builder()
            .delete()
            .url(
                HttpUrl.parse(endpoint)
                    .newBuilder()
                    .addPathSegment("user")
                    .query("key")
                    .addQueryParameter("access-key", accessKey)
                    .addQueryParameter("key-type", keyType)
                    .build())
            .build();

    safeCall(request);
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
  public Optional<GetBucketInfoResponse> getBucketInfo(String bucketName) {
    Request request =
        new Request.Builder()
            .get()
            .url(
                HttpUrl.parse(endpoint)
                    .newBuilder()
                    .addPathSegment("bucket")
                    .addQueryParameter("bucket", bucketName)
                    .build())
            .build();

    String resp = safeCall(request);
    return Optional.ofNullable(gson.fromJson(resp, GetBucketInfoResponse.class));
  }

  /**
   * Guarantee that the request is execute success and the connection is closed
   *
   * @param request
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
  public void modifyUser(String userId, Map<String, String> parameters) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .addQueryParameter("uid", userId);

    parameters
        .entrySet()
        .forEach(entry -> urlBuilder.addQueryParameter(entry.getKey(), entry.getValue()));

    Request request = new Request.Builder().post(emptyBody).url(urlBuilder.build()).build();

    safeCall(request);
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
  public Optional<String> getPolicy(String bucketName, String objectKey) {
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
