[![Build Status](https://travis-ci.org/twonote/radosgw-admin4j.svg?branch=master)](https://travis-ci.org/twonote/radosgw-admin4j)  [![License](https://img.shields.io/badge/license-Apache%202-blue.svg)]()
[![Javadocs](https://www.javadoc.io/badge/io.github.twonote/radosgw-admin4j.svg)](https://www.javadoc.io/static/io.github.twonote/radosgw-admin4j/2.0.2/org/twonote/rgwadmin4j/RgwAdmin.html)

# About
radosgw-admin4j is a Ceph object storage admin client that allows provisioning and control of a Ceph object storage deployment. Features include user/sub user management, quota management, usage report, bucket/object management, etc.

# Highlight
* **Fully support all [operations](http://docs.ceph.com/docs/master/radosgw/adminops/)** includes **sub user**, **quota** and more in the latest Ceph version.
* An easier way to manage radosgw. Avoid troubles when working with radosgw admin APIs, especially that docs are a bit confusing and inconsistent with the code base.
* Quality and compatibility - [Continuous Integration](https://travis-ci.org/twonote/radosgw-admin4j) and tests against Ceph LTS versions ([**Mimic/Nautilus**](https://docs.ceph.com/docs/master/releases/general/) currently.)
* Contributor friendly - typical contribution process, no weird policies, all contributions are welcome!

# Start using 

## Add dependency

You can obtain radosgw-admim4j from Maven Central using the following identifier:
* [io.github.twonote.radosgw-admin4j:2.0.2](https://search.maven.org/#artifactdetails%7Cio.github.twonote%7Cradosgw-admin4j%7C2.0.2%7Cjar)

## Configuration

```
RgwAdmin RGW_ADMIN =
              new RgwAdminBuilder()
                  .accessKey("administrator access key")
                  .secretKey("administrator secret key")
                  .endpoint("radosgw admin endpoint, e.g., http://127.0.0.1:8080/admin")
                  .build();
```

## Usage example

We support all types of operation includes **User**, **Subuser**, **Key**, **Bucket**, **Capability**, **Quota** and **Usage**. Please check all available operations in [![Javadocs](https://www.javadoc.io/badge/io.github.twonote/radosgw-admin4j.svg)](https://www.javadoc.io/static/io.github.twonote/radosgw-admin4j/2.0.2/org/twonote/rgwadmin4j/RgwAdmin.html)


### User management

```
// List user in the system
List<User> users = RGW_ADMIN.listUserInfo();

// Create user
RGW_ADMIN.createUser(userId);

// Get user information and show keys
User user = RGW_ADMIN.getUserInfo(userId).get();
user.getS3Credentials().stream().peek(System.out::println);

// Create subuser
SubUser subUser = RGW_ADMIN.createSubUser(userId, "subUserId", SubUser.Permission.FULL, CredentialType.SWIFT);

// Suspend a user
RGW_ADMIN.suspendUser(userId, true);

// Remove a user
RGW_ADMIN.removeUser(userId);
```

### Quota management

```
// Allow the user owns more buckets
RGW_ADMIN.modifyUser(userId, ImmutableMap.of("max-buckets", String.valueOf(Integer.MAX_VALUE)));

// Set the quota that causes the user can have at most one thousand objects, and the maximal usage is 1 GiB
RGW_ADMIN.setUserQuota(userId, 1000, 1048576);
```

### Bucket management

```
// Transfer the bucket owner from the user just created to the administrator
BucketInfo bucketInfo = RGW_ADMIN.getBucketInfo(bucketName).get();
RGW_ADMIN.linkBucket(bucketName, bucketInfo.getId(), adminUserId);

// Remove a bucket
RGW_ADMIN.removeBucket(bucketName);
```

### Usage report

```
// Retrieve and show the usage report for a given user
UsageInfo userUsage = RGW_ADMIN.getUserUsage(userId).get();
userUsage.getSummary().stream().peek(System.out::println);
```

## One more thing: Radosgw setup
To kick off, you need one ready to use radosgw instance and one radosgw account with proper admin capabilities. Follow the guide below to have a radowgw setup then you can fire the example code.

### “I do not have a radosgw setup currently”
You could refer the [Ceph official manual](http://docs.ceph.com/docs/master/start/) to setup a Ceph cluster with radosgw *quickly*. In fact, it is not a piece of cake if you do not familiar with Ceph. Things will be easier if you have **docker** in your environment. To setup a setup instance with an admin account powered by the [Ceph daemon image](https://hub.docker.com/r/ceph/daemon/), follow instructions below:
```
$ sudo docker rm -f rgwn; rm -rf /etc/cephn rgwn.log; sudo docker run -d -p 80:80 -v /etc/cephn/:/etc/ceph/ -e CEPH_DEMO_UID=qqq -e CEPH_DEMO_ACCESS_KEY=qqq -e CEPH_DEMO_SECRET_KEY=qqq -e  RGW_CIVETWEB_PORT=80  -e NETWORK_AUTO_DETECT=4 --name rgwn ceph/daemon:v4.0.1-stable-4.0-nautilus-centos-7 demo; 
```

Note that port 80 should be available.

Check the setup succeeded by the following command:
```
$ timeout 60 bash -c "until docker logs rgwn &> rgwn.log && grep SUCCESS rgwn.log; do sleep 1; done"
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
$ radosgw-admin user create --uid=qqq --display-name="qqq" --access-key=qqq --secret-key=qqq
$ radosgw-admin --id admin caps add --caps="buckets=*;users=*;usage=*;metadata=*" --uid=qqq
```

Second, enter the key pair (qqq,qqq) and add your radosgw endpoint to the [config file](https://github.com/twonote/radosgw-admin4j/blob/master/src/test/resources/rgwadmin.properties)

Note that radosgw does not enable [usage log](http://docs.ceph.com/docs/master/radosgw/admin/#usage) in default. If you need the feature (or run test cases), be sure that you enable the usage log in Ceph config file. Example ceph.conf: 
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

That's all!

# Contributing
All contributions are welcome. Our code style is [Google java style](https://google.github.io/styleguide/javaguide.html) and we use [google-java-format](https://github.com/google/google-java-format) to do code formatting. Nothing else special.

# Legal
Copyright 2017-2019 [twonote](http://twonote.github.io/) & The "radosgw-admin4j" contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
