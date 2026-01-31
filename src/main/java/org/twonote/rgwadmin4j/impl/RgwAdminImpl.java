package org.twonote.rgwadmin4j.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.model.BucketInfo;
import org.twonote.rgwadmin4j.model.Cap;
import org.twonote.rgwadmin4j.model.ClusterInfo;
import org.twonote.rgwadmin4j.model.CredentialType;
import org.twonote.rgwadmin4j.model.Quota;
import org.twonote.rgwadmin4j.model.S3Credential;
import org.twonote.rgwadmin4j.model.SubUser;
import org.twonote.rgwadmin4j.model.SwiftCredential;
import org.twonote.rgwadmin4j.model.UsageInfo;
import org.twonote.rgwadmin4j.model.User;

/**
 * Radosgw administrator implementation
 *
 * <p>Created by petertc on 2/16/17.
 */
public class RgwAdminImpl implements RgwAdmin {

  private static final Gson gson = new Gson();
  private static final JsonParser jsonParser = new JsonParser();

  private static final RequestBody emptyBody = RequestBody.create(null, new byte[]{});

  private final String endpoint;
  private final OkHttpClient client;

  /**
   * Create a Radosgw administrator implementation
   *
   * @param accessKey Access key of the admin who have proper administrative capabilities.
   * @param secretKey Secret key of the admin who have proper administrative capabilities.
   * @param endpoint  Radosgw admin API endpoint, e.g., http://127.0.0.1:80/admin
   */
  public RgwAdminImpl(String accessKey, String secretKey, String endpoint) {
    validEndpoint(endpoint);
    this.client =
        new OkHttpClient().newBuilder().addInterceptor(new S3Auth(accessKey, secretKey)).build();
    this.endpoint = endpoint;
  }

  private static void validEndpoint(String endpoint) {
    if (HttpUrl.parse(endpoint) == null) {
      throw new IllegalArgumentException("endpoint is invalid");
    }
  }

  private static void appendParameters(Map<String, String> parameters, HttpUrl.Builder urlBuilder) {
    if (parameters != null) {
      parameters.forEach(urlBuilder::addQueryParameter);
    }
  }

  private static String absSubUserId(String userId, String subUserId) {
    return String.join(":", userId, subUserId);
  }

  private static <T> Type setModelAndGetCorrespondingList2(Class<T> type) {
    return new TypeToken<ArrayList<T>>() {
    }.where(new TypeParameter<T>() {
    }, type).getType();
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
    Type type = new TypeToken<List<Cap>>() {
    }.getType();
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
    Type type = new TypeToken<List<Cap>>() {
    }.getType();
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
            .addQueryParameter("subuser", subUserId);

    appendParameters(parameters, urlBuilder);

    Request request = new Request.Builder().put(emptyBody).url(urlBuilder.build()).build();

    String resp = safeCall(request);
    Type type = new TypeToken<List<SubUser>>() {
    }.getType();
    return gson.fromJson(resp, type);
  }

  @Override
  public SubUser createSubUser(
      String userId,
      String subUserId,
      SubUser.Permission permission,
      CredentialType credentialType) {
    List<SubUser> subUser =
        createSubUser(
            userId,
            subUserId,
            ImmutableMap.of(
                "access",
                permission.toString(),
                "key-type",
                credentialType.toString(),
                "generate-secret",
                "True"));
    String absSubUserId = absSubUserId(userId, subUserId);
    return subUser.stream().filter(u -> absSubUserId.equals(u.getId())).findFirst().get();
  }

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
    Type type = new TypeToken<List<SubUser>>() {
    }.getType();
    return gson.fromJson(resp, type);
  }

  @Override
  public List<SubUser> setSubUserPermission(
      String userId, String subUserId, SubUser.Permission permission) {
    return modifySubUser(userId, subUserId, ImmutableMap.of("access", permission.toString()));
  }

  @Override
  public List<SubUser> listSubUserInfo(String userId) {
    Optional<User> userInfo = getUserInfo(userId);
    if (userInfo.isPresent()) {
      return userInfo.get().getSubusers();
    } else {
      return new ArrayList<>();
    }
  }

  @Override
  public Optional<SubUser> getSubUserInfo(String userId, String subUserId) {
    String absSubUserId = absSubUserId(userId, subUserId);
    List<SubUser> subUsers = listSubUserInfo(userId);
    return subUsers.stream().filter(u -> absSubUserId.equals(u.getId())).findFirst();
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

  private <T> List<T> _createKey(String uid, Map<String, String> parameters, Class<T> returnModel) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("key")
            .addQueryParameter("uid", uid);

    appendParameters(parameters, urlBuilder);

    Request request = new Request.Builder().put(emptyBody).url(urlBuilder.build()).build();

    String resp = safeCall(request);

    // Fit luminous behavior
    if (resp == null) {
      throw new RgwAdminException(404, "NoSuchUser");
    }

    Type type = setModelAndGetCorrespondingList2(returnModel);
    return gson.fromJson(resp, type);
  }

  private List<S3Credential> _createKey(String uid, Map<String, String> parameters) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("key")
            .addQueryParameter("uid", uid);

    appendParameters(parameters, urlBuilder);

    Request request = new Request.Builder().put(emptyBody).url(urlBuilder.build()).build();

    String resp = safeCall(request);
    Type type = new TypeToken<List<S3Credential>>() {
    }.getType();
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

    String resp = safeCall(request);

    // Fit luminous behavior
    if (resp == null) {
      throw new RgwAdminException(404, "NoSuchUser");
    }
  }

  @Override
  public List<S3Credential> createS3Credential(String userId, String accessKey, String secretKey) {
    return _createKey(
        userId,
        ImmutableMap.of(
            "access-key", accessKey,
            "secret-key", secretKey),
        S3Credential.class);
  }

  @Override
  public List<S3Credential> createS3Credential(String userId) {
    return _createKey(userId, ImmutableMap.of("generate-key", "True"), S3Credential.class);
  }

  @Override
  public void removeS3Credential(String userId, String accessKey) {
    _removeKey(userId, ImmutableMap.of("access-key", accessKey));
  }

  @Override
  public List<S3Credential> createS3CredentialForSubUser(
      String userId, String subUserId, String accessKey, String secretKey) {
    List<S3Credential> s3Credentials =
        _createKey(
            userId,
            ImmutableMap.of(
                "subuser", subUserId,
                "access-key", accessKey,
                "secret-key", secretKey,
                "key-type", "s3"),
            S3Credential.class);

    return s3Credentials
        .stream()
        .filter(k -> absSubUserId(userId, subUserId).equals(k.getUserId()))
        .collect(Collectors.toList());
  }

  @Override
  public List<S3Credential> createS3CredentialForSubUser(String userId, String subUserId) {
    List<S3Credential> s3Credentials =
        _createKey(
            userId,
            ImmutableMap.of(
                "subuser", subUserId,
                "key-type", "s3",
                "generate-key", "True"),
            S3Credential.class);

    return s3Credentials
        .stream()
        .filter(k -> absSubUserId(userId, subUserId).equals(k.getUserId()))
        .collect(Collectors.toList());
  }

  @Override
  public void removeS3CredentialFromSubUser(String userId, String subUserId, String accessKey) {
    _removeKey(
        userId,
        ImmutableMap.of(
            "subuser", subUserId,
            "key-type", "s3",
            "access-key", accessKey));
  }

  @Override
  public SwiftCredential createSwiftCredentialForSubUser(
      String userId, String subUserId, String password) {
    List<SwiftCredential> swiftCredentials =
        _createKey(
            userId,
            ImmutableMap.of(
                "subuser", subUserId,
                "secret-key", password,
                "key-type", "swift"),
            SwiftCredential.class);
    return swiftCredentials
        .stream()
        .filter(k -> absSubUserId(userId, subUserId).equals(k.getUserId()))
        .collect(Collectors.toList())
        .get(0);
  }

  @Override
  public SwiftCredential createSwiftCredentialForSubUser(String userId, String subUserId) {
    List<SwiftCredential> swiftCredentials =
        _createKey(
            userId,
            ImmutableMap.of(
                "subuser", subUserId,
                "key-type", "swift",
                "generate-key", "True"),
            SwiftCredential.class);
    return swiftCredentials
        .stream()
        .filter(k -> absSubUserId(userId, subUserId).equals(k.getUserId()))
        .collect(Collectors.toList())
        .get(0);
  }

  @Override
  public void removeSwiftCredentialFromSubUser(String userId, String subUserId) {
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
  public List<String> listBucket() {
    return listBucketInfo().stream().map(BucketInfo::getBucket).collect(Collectors.toList());
  }

  @Override
  public List<String> listBucket(String userId) {
    return listBucketInfo(userId)
        .stream()
        .map(BucketInfo::getBucket)
        .collect(Collectors.toList());
  }

  @Override
  public List<BucketInfo> listBucketInfo() {
    return _getBucketInfo(ImmutableMap.of("stats", "True"));
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
    if (parameters.containsKey("bucket")) {
      BucketInfo response = gson.fromJson(resp, BucketInfo.class);
      List<BucketInfo> ret = new ArrayList<>();
      if (response != null) {
        ret.add(response);
      }
      return ret;
    }

    Type type = new TypeToken<List<BucketInfo>>() {
    }.getType();
    return gson.fromJson(resp, type);
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

  private String call(Request request) {
    try (Response response = client.newCall(request).execute()) {
      if (response.code() == 404) {
        throw new RgwAdminException(404, "not found");
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
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .addQueryParameter("uid", userId);

    Request request = new Request.Builder().get().url(urlBuilder.build()).build();

    String resp = safeCall(request);
    return Optional.ofNullable(gson.fromJson(resp, User.class));
  }

  /**
   * Retrieve keys in a given metadata type
   *
   * <p>Equivalent to radosgw-admin metadata list --metadata-key bucket.instance
   *
   * @param metadataType Specify the metadata type.
   * @return A list of radosgw internal metadata keys in the given metadata type.
   */
  private List<String> listMetadata(MetadataType metadataType) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("metadata")
            .addPathSegment(metadataType.toString());
    Request request = new Request.Builder().get().url(urlBuilder.build()).build();
    String resp = safeCall(request);
    Type type = new TypeToken<List<String>>() {
    }.getType();
    return gson.fromJson(resp, type);
  }

  /**
   * Retrieve radosgw internal metadata content.
   *
   * <p>Equivalent to radosgw-admin metadata get --metadata-key bucket.instance:dsvdvdsv
   *
   * @param metadataType Specify the metadata type.
   * @param key          Specify the metadata key.
   * @return Content of metadata in a json string.
   */
  private <T> T getMetadata(MetadataType metadataType, String key, Class<T> returnType) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("metadata")
            .addQueryParameter("key", String.join(":", metadataType.toString(), key));

    Request request = new Request.Builder().get().url(urlBuilder.build()).build();
    String resp = safeCall(request);

    assert resp != null;
    JsonObject jo = (JsonObject) jsonParser.parse(resp);
    return gson.fromJson(jo.get("data").toString(), returnType);
  }

  @Override
  public List<String> listUser() {
    return listMetadata(MetadataType.USER);
  }

  @Override
  public List<String> listSubUser(String userId) {
    return listSubUserInfo(userId).stream().map(SubUser::getId).collect(Collectors.toList());
  }

  @Override
  public List<User> listUserInfo() {
    List<String> userIds = listMetadata(MetadataType.USER);
    return userIds
        .stream()
        .map(i -> getMetadata(MetadataType.USER, i, User.class))
        .collect(Collectors.toList());
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
    return getQuota(userId, "user");
  }

  @Override
  public Optional<Quota> getBucketQuota(String userId) {
    return getQuota(userId, "bucket");
  }

  public Optional<Quota> getQuota(String userId, String quotaType) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("quota")
            .addQueryParameter("uid", userId)
            .addQueryParameter("quota-type", quotaType);

    Request request = new Request.Builder().get().url(urlBuilder.build()).build();

    String resp = safeCall(request);

    // Fit luminous behavior
    if (resp == null) {
      throw new RgwAdminException(404, "NoSuchUser");
    }

    return Optional.ofNullable(gson.fromJson(resp, Quota.class));
  }

  @Override
  public void setIndividualBucketQuota(String userId, String bucket, long maxObjects,
      long maxSizeKB) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("bucket")
            .query("quota")
            .addQueryParameter("uid", userId)
            .addQueryParameter("bucket", bucket);

    Request request =
        new Request.Builder()
            .put(RequestBody.create(null, buildQuotaConfig(maxObjects, maxSizeKB)))
            .url(urlBuilder.build())
            .build();

    call(request);
  }

  @Override
  public void setBucketQuota(String userId, long maxObjects, long maxSizeKB) {
    setUserQuota(userId, "bucket", maxObjects, maxSizeKB);
  }

  @Override
  public void setUserQuota(String userId, long maxObjects, long maxSizeKB) {
    setUserQuota(userId, "user", maxObjects, maxSizeKB);
  }

  public void setUserQuota(String userId, String quotaType, long maxObjects, long maxSizeKB) {
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse(endpoint)
            .newBuilder()
            .addPathSegment("user")
            .query("quota")
            .addQueryParameter("uid", userId)
            .addQueryParameter("quota-type", quotaType);

    Request request =
        new Request.Builder()
            .put(RequestBody.create(null, buildQuotaConfig(maxObjects, maxSizeKB)))
            .url(urlBuilder.build())
            .build();

    call(request);
  }

  private String buildQuotaConfig(long maxObjects, long maxSizeKB) {
    return gson.toJson(
        ImmutableMap.of(
            "max_objects", String.valueOf(maxObjects),
            "max_size_kb", String.valueOf(maxSizeKB),
            "enabled", "true"));
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

  @Override
  public Optional<ClusterInfo> getInfo() {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(endpoint).newBuilder().addPathSegment("info");

    Request request = new Request.Builder().get().url(urlBuilder.build()).build();

    String resp = safeCall(request);
    ClusterInfo clusterInfo = gson.fromJson(resp, ClusterInfo.class);
    
    return Optional.ofNullable(clusterInfo);
  }

  enum MetadataType {
    USER("user"),
    BUCKET("bucket"),
    BUCKET_INSTANCE("bucket.instance");

    String s;

    MetadataType(String s) {
      this.s = s;
    }

    @Override
    public String toString() {
      return s;
    }
  }
}
