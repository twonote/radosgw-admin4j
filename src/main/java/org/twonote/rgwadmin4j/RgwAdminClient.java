package org.twonote.rgwadmin4j;

import org.twonote.rgwadmin4j.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Created by petertc on 3/14/17. */
public interface RgwAdminClient {
  /**
   * Add an administrative capability to a specified user.
   *
   * <p>The capability is in forms of [users|buckets|metadata|usage|zone]=[*|read|write|read, write]
   *
   * <p>Note that you can get the capability by {@link #getUserInfo(String)}
   *
   * @param uid The user ID to add an administrative capability to.
   * @param userCaps The administrative capability to add to the user. Example: usage=read,write
   */
  void addUserCapability(String uid, String userCaps);

  /**
   * Remove an administrative capability from a specified user.
   *
   * <p>The capability is in forms of [users|buckets|metadata|usage|zone]=[*|read|write|read, write]
   *
   * <p>Note that you can get the capability by {@link #getUserInfo(String)}
   *
   * @param uid The user ID to remove an administrative capability from.
   * @param userCaps The administrative capabilities to remove from the user. Example:
   *     usage=read,write
   */
  void removeUserCapability(String uid, String userCaps);

  /**
   * Create a new subuser
   *
   * <p>Note that in general for a subuser to be useful, it must be granted permissions by
   * specifying access. As with user creation if subuser is specified without secret, then a secret
   * key will be automatically generated.
   *
   * <p>Available parameters includes:
   *
   * <ul>
   * <li>secret-key: Specify secret key.
   * <li>key-type: Key type to be generated, options are: swift (default), s3.
   * <li>access: Set access permissions for sub-user, should be one of read, write, readwrite, full.
   * <li>generate-secret: Generate the secret key. Default: False
   * </ul>
   *
   * <p>Note that you can get subuser (swift) keys and other information by {@link
   * #getUserInfo(String)}
   *
   * <p>Note that to create subuser for S3, you need ceph v11.2.0-kraken or above.
   *
   * @param uid The user ID under which a subuser is to be created.
   * @param subUserId Specify the subuser ID to be created.
   * @param parameters The subuser parameters.
   * @return The subuser information.
   */
  List<SubUser> createSubUser(String uid, String subUserId, Map<String, String> parameters);

  /**
   * Create a new subuser for Swift use.
   *
   * <p>Note that the subuser will has "full" control permission.
   *
   * <p>Note that you can get subuser (swift) keys and other information by {@link
   * #getUserInfo(String)}
   *
   * @param uid
   * @param subUserId
   * @return
   */
  List<SubUser> createSubUserForSwift(String uid, String subUserId);

  /**
   * Modify an existing subuser.
   *
   * <p>Available parameters includes:
   *
   * <ul>
   * <li>secret-key: Specify secret key.
   * <li>key-type: Key type to be generated, options are: swift (default), s3.
   * <li>access: Set access permissions for sub-user, should be one of read, write, readwrite, full.
   * <li>generate-secret: Generate the secret key. Default: False
   * </ul>
   *
   * @param uid The user ID under which a subuser is to be created.
   * @param subUserId Specify the subuser ID to be created.
   * @param parameters The subuser parameters.
   * @return The subuser information.
   */
  List<SubUser> modifySubUser(String uid, String subUserId, Map<String, String> parameters);

  /**
   * Remove an existing subuser.
   *
   * <p>Note that the operation also removes keys belonging to the subuser.
   *
   * @param uid The user ID under which the subuser is to be removed.
   * @param subUserId The subuser ID to be removed.
   */
  void removeSubUser(String uid, String subUserId);

  /**
   * Create a new key.
   *
   * <p>If a subuser is specified then by default created keys will be swift type. If only one of
   * access-key or secret-key is provided the committed key will be automatically generated, that is
   * if only secret-key is specified then access-key will be automatically generated. By default, a
   * generated key is added to the keyring without replacing an existing key pair. If access-key is
   * specified and refers to an existing key owned by the user then it will be modified. The
   * response is a container listing all keys of the same type as the key created. Note that when
   * creating a swift key, specifying the option access-key will have no effect. Additionally, only
   * one swift key may be held by each user or subuser.
   *
   * <p>Available parameters are:
   *
   * <ul>
   * <li>subuser: The subuser ID to receive the new key.
   * <li>key-type: Key type to be generated, options are: swift, s3 (default).
   * <li>access-key: Specify the access key.
   * <li>secret-key: Specify the secret key.
   * </ul>
   *
   * @param uid The user ID to receive the new key.
   * @param parameters Create key options.
   * @return Create key response.
   */
  List<CreateKeyResponse> createKey(String uid, Map<String, String> parameters);

  /**
   * Create a new key.
   *
   * <p>The S3 key will be automatically generated for the user. If you want to specify the key,
   * create swift key for the subuser or do other customizations, please use {@link
   * #createKey(String, Map)}
   *
   * @param uid The user ID to receive the new key.
   * @return Create key response.
   */
  List<CreateKeyResponse> createKey(String uid);

  /**
   * Remove an existing key.
   *
   * @param accessKey The access key belonging to the key pair to remove.
   * @param keyType Key type to be removed, options are: swift, s3.
   */
  void removeKey(String accessKey, String keyType);

  /**
   * Delete an existing bucket.
   *
   * <p>Note that the operation ask radosgw to purge objects in the bucket before deletion.
   *
   * @param bucketName The bucket to remove.
   */
  void removeBucket(String bucketName);

  /**
   * Link a bucket to a specified user, unlinking the bucket from any previous user.
   *
   * @param bucketName The bucket name to unlink.
   * @param bucketId The bucket id to unlink. Example: dev.6607669.420. (You can get this by {@link
   *     #getBucketInfo(String)})
   * @param userId The user ID to link the bucket to.
   */
  void linkBucket(String bucketName, String bucketId, String userId);

  /**
   * Unlink a bucket from a specified user. Primarily useful for changing bucket ownership.
   *
   * @param bucketName The bucket to unlink.
   * @param userId The user ID to unlink the bucket from.
   */
  void unlinkBucket(String bucketName, String userId);

  /**
   * Check the index of an existing bucket.
   *
   * <p>NOTE: to check multipart object accounting with check-objects, fix must be set to True.
   *
   * <p>Example response:
   *
   * <pre>
   * [
   * ]{
   * }{
   *    "existing_header":{
   *       "usage":{
   *       }
   *    },
   *    "calculated_header":{
   *       "usage":{
   *       }
   *    }
   * }
   * </pre>
   *
   * @param bucketName The bucket to return info on.
   * @param isCheckObjects Check multipart object accounting. Example: True [False]
   * @param isFix Also fix the bucket index when checking. Example: False [False]
   */
  Optional<String> checkBucketIndex(String bucketName, boolean isCheckObjects, boolean isFix);

  // TODO: list bucket info by user id

  /**
   * Get information about a bucket.
   *
   * @param bucketName The bucket to return info on.
   * @return The desired bucket information.
   */
  Optional<GetBucketInfoResponse> getBucketInfo(String bucketName);

  /**
   * Create a new user.
   *
   * <p>A S3 key pair will be created automatically and returned in the response. If you want to
   * customize user properties or create swift user, use {@link #createUser(String, Map)} instead.
   *
   * @param userId The user ID to be created.
   * @return The user information.
   */
  CreateUserResponse createUser(String userId);

  /**
   * Create a new user.
   *
   * <p>You can customize user properties or create swift user by set the parameters. Available
   * parameters includes:
   *
   * <ul>
   * <li>display-name: The display name of the user to be created.
   * <li>email: The email address associated with the user.
   * <li>key-type: Key type to be generated, options are: swift, s3
   * <li>access-key: Specify access key.
   * <li>secret-key: Specify secret key.
   * <li>user-caps: User capabilities. Example: usage=read, write; users=read
   * <li>generate-key: Generate a new key pair and add to the existing keyring. Example: True [True]
   * <li>max-buckets: Specify the maximum number of buckets the user can own. Example: 500 [1000]
   * <li>suspended: Specify whether the user should be suspended. Example: False [False]
   * </ul>
   *
   * <p>If only one of access-key or secret-key is provided, the omitted key will be automatically
   * generated. By default, a generated key is added to the keyring without replacing an existing
   * key pair. If access-key is specified and refers to an existing key owned by the user then it
   * will be modified.
   *
   * @param userId The user ID to be created.
   * @param parameters The user properties.
   * @return The user information.
   */
  CreateUserResponse createUser(String userId, Map<String, String> parameters);

  // TODO: list users

  /**
   * Get user information.
   *
   * @param userId The user for which the information is requested.
   * @return The user information.
   */
  Optional<GetUserInfoResponse> getUserInfo(String userId);

  /**
   * Modify a user.
   *
   * <p>Available parameters includes:
   *
   * <ul>
   * <li>display-name: The display name of the user to be created.
   * <li>email: The email address associated with the user.
   * <li>key-type: Key type to be generated, options are: swift, s3
   * <li>access-key: Specify access key.
   * <li>secret-key: Specify secret key.
   * <li>user-caps: User capabilities. Example: usage=read, write; users=read
   * <li>generate-key: Generate a new key pair and add to the existing keyring. Example: True [True]
   * <li>max-buckets: Specify the maximum number of buckets the user can own. Example: 500 [1000]
   * <li>suspended: Specify whether the user should be suspended. Example: False [False]
   * </ul>
   *
   * @param userId The user ID to be modified.
   * @param parameters
   */
  void modifyUser(String userId, Map<String, String> parameters);

  /**
   * Suspend a user
   *
   * @param userId The user ID to be suspended.
   */
  @Deprecated
  void suspendUser(String userId);

  /**
   * Suspend or resume a user
   *
   * @param userId The user ID to be suspended or resumed.
   * @param suspend switch suspended or resumed.
   */
  void suspendUser(String userId, boolean suspend);

  /**
   * Remove an existing user.
   *
   * <p>Note that the buckets and objects belonging to the user will also be removed.
   *
   * @param userId The user ID to be removed.
   */
  void removeUser(String userId);

  /**
   * Get user quota.
   *
   * @param userId The user ID to get quota.
   * @return user quota.
   */
  Optional<Quota> getUserQuota(String userId);

  /**
   * Set, modify or disable user quota.
   *
   * @param userId The user ID to set quota.
   * @param maxObjects The max-objects setting allows you to specify the maximum number of objects.
   *     A negative value disables this setting.
   * @param maxSizeKB The max-size option allows you to specify a quota for the maximum number of
   *     bytes. A negative value disables this setting.
   */
  void setUserQuota(String userId, long maxObjects, long maxSizeKB);

  /**
   * Remove an existing object.
   *
   * <p>NOTE: Does not require owner to be non-suspended.
   *
   * @param bucketName The bucket containing the object to be removed.
   * @param objectKey The object to remove.
   */
  void removeObject(String bucketName, String objectKey);

  /**
   * Read the policy of an object or bucket.
   *
   * <p>Note that the term "policy" here is not stand for "S3 bucket policy". Instead, it represents
   * S3 Access Control Policy (ACP).
   *
   * <p>We return json string instead of the concrete model here due to the server returns the
   * internal data structure which is not well defined. For example:
   *
   * <pre>
   * {
   *    "acl":{
   *       "acl_user_map":[
   *          {
   *             "user":"rgwAdmin4jTest-6d6a2645-0219-4e49-8493-0bdc8cb00e19",
   *             "acl":15
   *          }
   *       ],
   *       "acl_group_map":[
   *       ],
   *       "grant_map":[
   *          {
   *             "id":"rgwAdmin4jTest-6d6a2645-0219-4e49-8493-0bdc8cb00e19",
   *             "grant":{
   *                "type":{
   *                   "type":0
   *                },
   *                "id":"rgwAdmin4jTest-6d6a2645-0219-4e49-8493-0bdc8cb00e19",
   *                "email":"",
   *                "permission":{
   *                   "flags":15
   *                },
   *                "name":"rgwAdmin4jTest-6d6a2645-0219-4e49-8493-0bdc8cb00e19",
   *                "group":0,
   *                "url_spec":""
   *             }
   *          }
   *       ]
   *    },
   *    "owner":{
   *       "id":"rgwAdmin4jTest-6d6a2645-0219-4e49-8493-0bdc8cb00e19",
   *       "display_name":"rgwAdmin4jTest-6d6a2645-0219-4e49-8493-0bdc8cb00e19"
   *    }
   * }
   * </pre>
   *
   * @param bucketName The bucket to read the policy from.
   * @param objectKey The object to read the policy from. Set to null if you want to get policy of
   *     bucket.
   * @return If successful, returns the object or bucket policy.
   */
  Optional<String> getPolicy(String bucketName, String objectKey);
}
