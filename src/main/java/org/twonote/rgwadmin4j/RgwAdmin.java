package org.twonote.rgwadmin4j;

import org.twonote.rgwadmin4j.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Radosgw administrator
 *
 * <p>Administer the Ceph Object Storage (a.k.a. Radosgw) service with user management, access
 * controls, quotas and usage tracking among other features.
 *
 * <p>Note that to some operations needs proper configurations on radosgw, and require that the
 * requester holds special administrative capabilities.
 *
 * <p>Created by petertc on 3/14/17.
 */
@SuppressWarnings("SameParameterValue")
public interface RgwAdmin {
  /**
   * Remove usage information for specified user. With no dates specified, removes all usage
   * information.
   *
   * <p>There are few parameters available to filter the query result as you wish, includes:
   *
   * <ul>
   * <li>start: Date and (optional) time that specifies the start time of the requested data.
   *     Example: 2012-09-25 16:00:00
   * <li>end: Date and (optional) time that specifies the end time of the requested data
   *     (non-inclusive). Example: 2012-09-25 16:00:00
   * </ul>
   *
   * @param userId The user for which the information is requested.
   * @param parameters optional parameters to filter the usage to delete.
   */
  void trimUserUsage(String userId, Map<String, String> parameters);

  /**
   * Remove usage information. With no dates specified, removes all usage information.
   *
   * <p>There are few parameters available to filter the query result as you wish, includes:
   *
   * <ul>
   * <li>start: Date and (optional) time that specifies the start time of the requested data.
   *     Example: 2012-09-25 16:00:00
   * <li>end: Date and (optional) time that specifies the end time of the requested data
   *     (non-inclusive). Example: 2012-09-25 16:00:00
   * </ul>
   *
   * @param parameters optional parameters to filter the usage to delete.
   */
  void trimUsage(Map<String, String> parameters);

  /**
   * Request bandwidth usage information for specified user.
   *
   * <p>See {@link #getUsage(Map)}
   */
  Optional<UsageInfo> getUserUsage(String userId);

  /**
   * Request bandwidth usage information for specified user.
   *
   * <p>See {@link #getUsage(Map)}
   */
  Optional<UsageInfo> getUserUsage(String userId, Map<String, String> parameters);

  /**
   * Request bandwidth usage information.
   *
   * <p>See {@link #getUsage(Map)}
   */
  Optional<UsageInfo> getUsage();

  /**
   * Request bandwidth usage information.
   *
   * <p>Note that radosgw does not enable usage collection in default. To get usage, you need the
   * following option:
   *
   * <pre>
   *   rgw enable usage log = true
   * </pre>
   *
   * <p>There are few parameters available to filter the query result as you wish, includes:
   *
   * <ul>
   * <li>start: Date and (optional) time that specifies the start time of the requested data.
   *     Example: 2012-09-25 16:00:00
   * <li>end: Date and (optional) time that specifies the end time of the requested data
   *     (non-inclusive). Example: 2012-09-25 16:00:00
   * <li>show-entries: Specifies whether data entries should be returned. Example: False [True]
   * <li>show-summary: Specifies whether data summary should be returned. Example: False [True]
   * </ul>
   *
   * @param parameters optional parameters to filter the query result.
   * @return Request bandwidth usage information.
   */
  Optional<UsageInfo> getUsage(Map<String, String> parameters);

  /**
   * Add an administrative capability to a specified user.
   *
   * <p>Note that you can get the capability by {@link #getUserInfo(String)}
   *
   * @param userId The user ID to add an administrative capability to.
   * @param userCaps The administrative capability to add to the user.
   * @return The user's capabilities after the operation.
   */
  List<Cap> addUserCapability(String userId, List<Cap> userCaps);

  /**
   * Remove an administrative capability from a specified user.
   *
   * <p>Note that you can get the capability by {@link #getUserInfo(String)}
   *
   * @param userId The user ID to remove an administrative capability from.
   * @param userCaps The administrative capabilities to remove from the user.
   * @return The user's capabilities after the operation.
   */
  List<Cap> removeUserCapability(String userId, List<Cap> userCaps);

  /**
   * Create a new subuser
   *
   * <p>Available parameters includes:
   *
   * <ul>
   * <li>access-key: Specify access key.
   * <li>secret-key: Specify secret key.
   * <li>key-type: Key type to be generated, options are: swift (default), s3.
   * <li>access: Set access permissions for sub-user, should be one of read, write, readwrite, full,
   *     "".
   * <li>generate-secret: Generate the secret key. Default: False
   * </ul>
   *
   * <p>Tips:
   *
   * <ol>
   * <li>In general for a subuser to be useful, it must be granted permissions by specifying access.
   * <li>You can get subuser credentials by {@link #getUserInfo(String)} after creation.
   * <li>To create subuser for S3, you need ceph v11.2.0-kraken or above.
   * </ol>
   *
   * @param userId The user ID under which a subuser is to be created.
   * @param subUserId Specify the subuser ID to be created.
   * @param parameters The subuser parameters.
   * @return Subusers associated with the user account.
   */
  List<SubUser> createSubUser(String userId, String subUserId, Map<String, String> parameters);

  /**
   * Create a new sub-user
   *
   * <p>This method will create the sub-user with the automatic generated credential.
   *
   * <p>Tips:
   *
   * <ol>
   * <li>In general for a subuser to be useful, it must be granted permissions by specifying access.
   * <li>You can get subuser credentials by {@link #getUserInfo(String)} after creation.
   * <li>To create subuser for S3, you need ceph v11.2.0-kraken or above.
   * </ol>
   *
   * @param userId The user ID under which a subuser is to be created.
   * @param subUserId Specify the subuser ID to be created. Should be in the relative form, i.e.,
   *     does not contains the user id.
   * @param permission The subuser permission.
   * @param credentialType Specify credential type to be generated.
   * @return The created subuser information.
   */
  SubUser createSubUser(
      String userId,
      String subUserId,
      SubUser.Permission permission,
      CredentialType credentialType);

  /**
   * Modify an existing subuser permission.
   *
   * <p>Note that you can get the permission by {@link #getSubUserInfo(String, String)}
   *
   * @param userId The user ID under which a subuser is to be created.
   * @param subUserId Specify the subuser ID to be created.
   * @param permission Specify the subuser permission.
   * @return Subusers associated with the user account.
   */
  List<SubUser> setSubUserPermission(
      String userId, String subUserId, SubUser.Permission permission);

  /**
   * Retrieve the sub-user information under a user
   *
   * @param userId The user ID under which subusers we interested to
   * @return subusers
   */
  List<SubUser> listSubUserInfo(String userId);

  /**
   * Get sub-user information.
   *
   * @param userId The user ID.
   * @param subUserId The subuser ID
   * @return The subuser information.
   */
  Optional<SubUser> getSubUserInfo(String userId, String subUserId);

  /**
   * Remove an existing subuser.
   *
   * <p>Note that the operation also removes credentials belonging to the subuser.
   *
   * @param userId The user ID under which the subuser is to be removed.
   * @param subUserId The subuser ID to be removed.
   */
  void removeSubUser(String userId, String subUserId);

  /**
   * Create a new S3 credential for the specified user.
   *
   * @param userId the specified user.
   * @param accessKey S3 access key
   * @param secretKey S3 secret key
   * @return S3 credentials associated with this user account.
   */
  List<S3Credential> createS3Credential(String userId, String accessKey, String secretKey);

  /**
   * Create a new S3 credential pair for the specified user.
   *
   * <p>The credential will be automatically generated for the user. If you want to specify the
   * credential, please use {@link #createS3Credential(String, String, String)}
   *
   * @param userId the specified user.
   * @return S3 credentials associated with this user account.
   */
  List<S3Credential> createS3Credential(String userId);

  /**
   * Remove an existing S3 credential from the specified user.
   *
   * @param userId The specified user.
   * @param accessKey The access key belonging to the credential to remove.
   */
  void removeS3Credential(String userId, String accessKey);

  /**
   * Create a new S3 credential for the specified subuser.
   *
   * @param userId The specified user.
   * @param subUserId the specified sub user. Should not contain user id, i.e., bar instead of
   *     foo:bar.
   * @param accessKey S3 access key.
   * @param secretKey S3 secret key.
   * @return S3 credentials associated with this subuser account.
   */
  List<S3Credential> createS3CredentialForSubUser(
      String userId, String subUserId, String accessKey, String secretKey);

  /**
   * Create a new S3 credential for the specified sub user.
   *
   * <p>The credential will be automatically generated for the user. If you want to specify the
   * credential, please use {@link #createS3CredentialForSubUser(String, String, String, String)}
   *
   * @param userId the specified user.
   * @param subUserId the specified sub user. Should not contain user id, i.e., bar instead of
   *     foo:bar.
   * @return S3 credentials created associated with this subuser account.
   */
  List<S3Credential> createS3CredentialForSubUser(String userId, String subUserId);

  /**
   * Remove an existing S3 credential from the specified sub user.
   *
   * @param userId the specified user.
   * @param subUserId the specified sub user. Should not contain user id, i.e., bar instead of
   *     foo:bar.
   * @param accessKey The access key belonging to the credential to remove.
   */
  void removeS3CredentialFromSubUser(String userId, String subUserId, String accessKey);

  /**
   * Create a new swift credential for the specified sub user.
   *
   * <p>Tip: a subuser can have only one swift credential.
   *
   * @param userId The specified user.
   * @param subUserId the specified sub user. Should not contain user id, i.e., bar instead of
   *     foo:bar.
   * @param password The specified swift password.
   * @return The Swift credentials associated with this subuser account.
   */
  SwiftCredential createSwiftCredentialForSubUser(String userId, String subUserId, String password);

  /**
   * Create a new swift credential for the specified sub user.
   *
   * <p>The credential will be automatically generated for the user. If you want to specify it,
   * please use {@link #createSwiftCredentialForSubUser(String, String, String)}
   *
   * <p>Tip: a subuser can have only one swift credential.
   *
   * @param userId The specified user.
   * @param subUserId The specified sub user. Should not contain user id, i.e., bar instead of
   *     foo:bar.
   * @return The Swift credential associated with this subuser account.
   */
  SwiftCredential createSwiftCredentialForSubUser(String userId, String subUserId);

  /**
   * Remove the credential from the specified sub user.
   *
   * @param userId the specified user.
   * @param subUserId the specified sub user. Should not contain user id, i.e., bar instead of
   *     foo:bar.
   */
  void removeSwiftCredentialFromSubUser(String userId, String subUserId);

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
   * @return Status of bucket index.
   */
  Optional<String> checkBucketIndex(String bucketName, boolean isCheckObjects, boolean isFix);

  /**
   * List buckets belong to a user.
   *
   * @param userId The bucket owner we interested.
   * @return Bucket list.
   */
  List<String> listBucket(String userId);

  /**
   * Get information about buckets under a given user.
   *
   * @param userId The user to retrieve bucket information for.
   * @return The desired bucket information.
   */
  List<BucketInfo> listBucketInfo(String userId);

  /**
   * Get information about a bucket.
   *
   * @param bucketName The bucket to return info on.
   * @return The desired bucket information.
   */
  Optional<BucketInfo> getBucketInfo(String bucketName);

  /**
   * Create a new user.
   *
   * <p>A S3 key pair will be created automatically and returned in the response. If you want to
   * customize user properties or create swift user, use {@link #createUser(String, Map)} instead.
   *
   * @param userId The user ID to be created.
   * @return The user information.
   */
  User createUser(String userId);

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
  User createUser(String userId, Map<String, String> parameters);

  /**
   * Get user information.
   *
   * @param userId The user for which the information is requested.
   * @return The user information.
   */
  Optional<User> getUserInfo(String userId);

  /**
   * Get the user list
   *
   * @return Id of users in the system.
   */
  List<String> listUser();

  /**
   * Get the subuser list under a user
   *
   * @param userId The user ID under which subusers we interested to.
   * @return Id of subusers under the given user.
   */
  List<String> listSubUser(String userId);

  /**
   * Get information about users exist in the system
   *
   * @return Information of users in the system.
   */
  List<User> listUserInfo();

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
   * @param parameters Optional parameters.
   * @return The user information.
   */
  User modifyUser(String userId, Map<String, String> parameters);

  /**
   * Suspend or resume a user
   *
   * @param userId The user ID to be suspended or resumed.
   * @param suspend Set true to suspend the user, and vice versa.
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
   * Read the policy of an object.
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
   * @param bucketName The bucket to which the object belong to.
   * @param objectKey The object to read the policy from.
   * @return If successful, returns the policy.
   */
  Optional<String> getObjectPolicy(String bucketName, String objectKey);

  /**
   * Read the policy of an bucket.
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
   * @return If successful, returns the policy.
   */
  Optional<String> getBucketPolicy(String bucketName);
}
