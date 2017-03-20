package org.twonote.rgwadmin4j;

import org.twonote.rgwadmin4j.model.CreateUserResponse;
import org.twonote.rgwadmin4j.model.GetBucketInfoResponse;
import org.twonote.rgwadmin4j.model.GetUserInfoResponse;
import org.twonote.rgwadmin4j.model.Quota;

import java.util.Map;
import java.util.Optional;

/**
 * Created by petertc on 3/14/17.
 */
public interface RgwAdminClient {
    void addUserCapability(String uid, String userCaps);

  void deleteUserCapability(String uid, String userCaps);

  void removeBucket(String bucketName);

    void linkBucket(String bucketName, String bucketId, String userId);

    Optional<GetBucketInfoResponse> getBucketInfo(String bucketName);

    CreateUserResponse createUser(String userId);

    CreateUserResponse createUser(String userId, boolean isLimit);

    Optional<GetUserInfoResponse> getUserInfo(String userId);

    void modifyUser(String userId, Map<String, String> parameters);

    void suspendUser(String userId);

    void removeUser(String userId);

    Optional<Quota> getUserQuota(String userId);

    void setUserQuota(String userId, long maxObjects, long maxSize);
}
