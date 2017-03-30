package org.twonote.rgwadmin4j;

import org.twonote.rgwadmin4j.model.CreateUserResponse;
import org.twonote.rgwadmin4j.model.GetBucketInfoResponse;
import org.twonote.rgwadmin4j.model.GetUserInfoResponse;
import org.twonote.rgwadmin4j.model.Quota;

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
   * Delete an existing bucket.
   *
   * <p>
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
   * <p>
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
   * <p>
   *
   * <p>Note that the term "policy" here is not stand for "S3 bucket policy". Instead, it represents
   * S3 Access Control Policy (ACP).
   *
   * <p>
   *
   * <p>We return json string instead of the concrete model here due to the server returns the
   * internal data structure which is not well defined. For example:
   *
   * <p>
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
   *
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
