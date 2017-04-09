[![Build Status](https://travis-ci.org/twonote/radosgw-admin4j.svg?branch=master)](https://travis-ci.org/twonote/radosgw-admin4j)  [![License](https://img.shields.io/badge/license-Apache%202-blue.svg)]()

# About
A Ceph Object Storage Admin SDK / Client Library for Java

# Hightlight
* **Fully support all [operations](http://docs.ceph.com/docs/master/radosgw/adminops/)** in the least Ceph version. (Jewel LTS/Kraken stable currently)
* [Continuous Integration](https://travis-ci.org/twonote/radosgw-admin4j) and tests aganst the least Ceph version for quality and compatiabiliy
* Uses the [Maven](http://maven.apache.org/) build system, Java 8 is required.
* **All contributions are welcome! Feel free here~**

# Start using 
You can obtain radosgw-admim4j from Maven Central using the following identifier:
* [io.github.twonote.radosgw-admin4j:0.0.5](https://search.maven.org/#artifactdetails%7Cio.github.twonote%7Cradosgw-admin4j%7C0.0.5%7Cjar)

## Usage

Please check more operations in [java doc](https://twonote.github.io/radosgw-admin4j/apidocs/index.html)!

```
RgwAdminClient RGW_ADMIN_CLIENT = new RgwAdminClientImpl(accessKey, secretKey, adminEndpoint);

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
$ sudo docker run -d --net=host -v /etc/ceph/:/etc/ceph/ -e MON_IP=127.0.0.1 -e CEPH_PUBLIC_NETWORK=127.0.0.0/24 -e CEPH_DEMO_UID=qqq -e CEPH_DEMO_ACCESS_KEY=qqq -e CEPH_DEMO_SECRET_KEY=qqq -e CEPH_DEMO_BUCKET=qqq --name rgw ceph/demo@sha256:7734ac78571ee0530724181c5b2db2e5a7ca0ff0e246c10c223d3ca9665c74ba
$ sleep 10
$ sudo docker exec -it rgw radosgw-admin --id admin caps add --caps="buckets=*;users=*;usage=*" --uid=qqq
```

Check the setup succeeded by the following command:
```
$ sudo docker ps | grep rgw
```

(Optionally) You can also *enter* the running container to take a look, watch log, execute the radosgw-admin management tool with the following command:
```
$ sudo docker exec -it rgw /bin/bash
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
$ radosgw-admin key create --uid=qqq --key-type=s3 --access-key=qqq --secret-key=qqq
$ radosgw-admin --id admin caps add --caps="buckets=*;users=*;usage=*" --uid=qqq
```

Second, enter the key pair (qqq,qqq) and your radosgw endpoint to the [config file](https://github.com/twonote/radosgw-admin4j/blob/master/src/test/resources/rgwadmin.properties)

Note that radosgw does not enable [usage collection](http://docs.ceph.com/docs/master/radosgw/admin/#usage) in default. If you need the feature (or run test cases), be sure that you enable the usage in ceph config file. Example ceph.conf: 
```
...
[client.radosgw.gateway]
rgw enable usage log = true
rgw usage log tick interval = 1
rgw usage log flush threshold = 1
rgw usage max shards = 32
rgw usage max user shards = 1
...
```

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
