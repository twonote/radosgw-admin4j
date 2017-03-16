# About
A Ceph Object Storage Admin SDK / Client Library for Java

# Hightlight
* Supports the least Ceph version. (Jewel LTS currently)
* Continuous Integration and integration tests for quality and compatiabiliy
* Uses the [Maven](http://maven.apache.org/) build system, Java 8 is required.
* **All contributions are welcome! Feel free here~**

# Start using 
You can obtain radosgw-admim4j from Maven Central using the following identifier:
* [io.github.twonote.radosgw-admin4j:0.0.3](https://search.maven.org/#artifactdetails%7Cio.github.twonote%7Cradosgw-admin4j%7C0.0.3%7Cjar)

## Usage
```
RgwAdminClient RGW_ADMIN_CLIENT = new RgwAdminClientImpl(accessKey, secretKey, s3Endpoint);

// create user
CreateUserResponse response = RGW_ADMIN_CLIENT.createUser(userId);

// Get user info 
GetUserInfoResponse response = RGW_ADMIN_CLIENT.getUserInfo(adminUserId).get();

// Allow the user owns more buckets
RGW_ADMIN_CLIENT.modifyUser(userId, ImmutableMap.of("max-buckets", String.valueOf(Integer.MAX_VALUE)));

// create bucket by the new user
AmazonS3 s3 = initS3(response.getKeys().get(0).getAccessKey(), response.getKeys().get(0).getSecretKey(), s3Endpoint);
s3.createBucket(bucketName);

// get bucket info
GetBucketInfoResponse _response = RGW_ADMIN_CLIENT.getBucketInfo(bucketName).get();

// Change bucket owner
RGW_ADMIN_CLIENT.linkBucket(bucketName, bucketId, adminUserId);

// Remove bucket
RGW_ADMIN_CLIENT.removeBucket(bucketName);

// Suspend user
RGW_ADMIN_CLIENT.suspendUser(userId);

// Remove user
RGW_ADMIN_CLIENT.removeUser(userId);
```
## Setup radosgw and do integration test
Since this artifact is a client of radosgw, you also need one ready to use radosgw instance and one radosgw account with admin capabilities.

### “I do not have a radosgw setup currently”
You can refer the [Ceph official manual](http://docs.ceph.com/docs/master/start/) to setup a Ceph cluster with radosgw *quickly*. In fact, in my experience it is not a piece of cake if you do not familiar with Ceph. Things will be easier if you have **docker** in your environment. To setup a setup instance with an admin account powered by the [ceph demo image](https://hub.docker.com/r/ceph/demo/), follow instructions below:
```
$ docker run -d --net=host -v /etc/ceph/:/etc/ceph/ -e MON_IP=10.0.2.15 -e CEPH_PUBLIC_NETWORK=10.0.2.0/24 -e CEPH_DEMO_UID=qqq -e CEPH_DEMO_ACCESS_KEY=qqq -e CEPH_DEMO_SECRET_KEY=qqq -e CEPH_DEMO_BUCKET=qqq --name rgw ceph/demo
$ # Change IP and NETWORK in the above command to fit your network setting
$ docker exec -it rgw radosgw-admin --id admin caps add --caps="buckets=*,users=*" --uid=qqq
```

Check the setup succeeded by the following command:
```
$ docker ps |grep rgw
```

(Optionally) You can also *enter* the running container to take a look, watch log, execute the radosgw-admin management tool with the following command:
```
$ docker exec -it rgw /bin/bash
```

Once the above procedure is done, you can now run radosgw-admin4j tests without any config on the client side, since the [default config](https://github.com/twonote/radosgw-admin4j/blob/master/src/test/resources/rgwadmin.properties) is meet the case. Run tests by:
```
$ git clone https://github.com/twonote/radosgw-admin4j.git
$ cd radosgw-admin4j
$ mvn test
```

### “I already have a radosgw instance on hand”
First, you need an admin account. If you not yet have it, create the account with the following command:
```
$ radosgw-admin user create --uid=qqq --display-name="qqq"
$ radosgw-admin key create --uid=qqq --key-type=s3 --gen-access-key --gen-secret
$ radosgw-admin --id admin caps add --caps="buckets=*,users=*" --uid=qqq
```

Second, enter the key pair and the radosgw endpoint to the [config file](https://github.com/twonote/radosgw-admin4j/blob/master/src/test/resources/rgwadmin.properties)

Done!

# Legal
Copyright 2016 [twonote](http://twonote.github.io/) & The "radosgw-admin4j" contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
