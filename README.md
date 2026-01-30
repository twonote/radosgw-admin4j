[![Build Status](https://github.com/twonote/radosgw-admin4j/actions/workflows/maven.yml/badge.svg)](https://github.com/twonote/radosgw-admin4j/actions)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://github.com/twonote/radosgw-admin4j/blob/master/LICENSE)
[![Javadocs](https://www.javadoc.io/badge/io.github.twonote/radosgw-admin4j.svg)](https://www.javadoc.io/doc/io.github.twonote/radosgw-admin4j/latest/org/twonote/rgwadmin4j/RgwAdmin.html)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.twonote/radosgw-admin4j.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.twonote/radosgw-admin4j)

# About
**radosgw-admin4j** is a powerful Ceph object storage admin client designed for provisioning and managing Ceph object storage deployments. It offers a wide range of features, including user and subuser management, quota control, usage reporting, and bucket/object management, among others.

# Highlights
- **Support for all [Operations](http://docs.ceph.com/docs/master/radosgw/adminops/)**: We fully support all operations, including subuser and quota management, for the latest Ceph versions.
- Simplified Radosgw Management: Manage your radosgw instance with ease, avoiding the complexities often associated with radosgw admin APIs.
- Quality and Compatibility: Our codebase undergoes continuous integration and testing against active Ceph releases, including **Tentacle**.
- Contributor-Friendly: We welcome contributions with a straightforward contribution process and no unusual policies.

# Getting Started

## Add Dependency

You can include radosgw-admin4j in your project by adding the following Maven Central dependency:

```xml
<dependency>
    <groupId>io.github.twonote</groupId>
    <artifactId>radosgw-admin4j</artifactId>
    <version>2.0.10</version> <!-- Replace with the latest version -->
</dependency>
```

## Configuration

```java
RgwAdmin rgwAdmin = new RgwAdminBuilder()
    .accessKey("administrator access key")
    .secretKey("administrator secret key")
    .endpoint("radosgw admin endpoint, e.g., http://127.0.0.1:8080/admin")
    .build();
```

## Usage Example

We offer a comprehensive set of operations, including User, Subuser, Key, Bucket, Capability, Quota, and Usage. Please refer to the [![Javadocs](https://www.javadoc.io/badge/io.github.twonote/radosgw-admin4j.svg)](https://www.javadoc.io/doc/io.github.twonote/radosgw-admin4j/latest/org/twonote/rgwadmin4j/RgwAdmin.html) for all available operations.

### User Management

```java
// List users in the system
List<User> users = rgwAdmin.listUserInfo();

// Create a user
rgwAdmin.createUser(userId);

// Get user information and display keys
User user = rgwAdmin.getUserInfo(userId).get();
user.getS3Credentials().forEach(System.out::println);

// Get user information without keys (enhanced security)
// This is useful when you only need user metadata without exposing sensitive credentials
User userWithoutKeys = rgwAdmin.getUserInfo(userId, false).get();

// Get user information by access key
String accessKey = user.getS3Credentials().get(0).getAccessKey();
User userByKey = rgwAdmin.getUserInfoByAccessKey(accessKey).get();

// Get user information by access key without credentials
User userByKeyWithoutKeys = rgwAdmin.getUserInfoByAccessKey(accessKey, false).get();

// Create a subuser
SubUser subUser = rgwAdmin.createSubUser(userId, "subUserId", SubUser.Permission.FULL, CredentialType.SWIFT);

// Suspend a user
rgwAdmin.suspendUser(userId, true);

// Remove a user
rgwAdmin.removeUser(userId);
```

**Note on Enhanced Security:** The new `getUserInfo` overloads support the `user-info-without-keys` capability. When `fetchKeys` is set to `false`, or when the caller only has the `user-info-without-keys=read` capability (without `users=read`), S3 and Swift keys will be excluded from the response unless the caller is a system user or admin user.

### Quota Management

```java
// Allow the user to own more buckets
rgwAdmin.modifyUser(userId, ImmutableMap.of("max-buckets", String.valueOf(Integer.MAX_VALUE)));

// Set a quota that limits the user to a maximum of one thousand objects and a maximum usage of 1 GiB
rgwAdmin.setUserQuota(userId, 1000, 1048576);
```

### Bucket Management

```java
// Transfer the bucket ownership from the user to the administrator
BucketInfo bucketInfo = rgwAdmin.getBucketInfo(bucketName).get();
rgwAdmin.linkBucket(bucketName, bucketInfo.getId(), adminUserId);

// Remove a bucket
rgwAdmin.removeBucket(bucketName);
```

### Usage Report

```java
// Retrieve and display the usage report for a given user
UsageInfo userUsage = rgwAdmin.getUserUsage(userId).get();
userUsage.getSummary().forEach(System.out::println);
```

## One More Thing: Radosgw Setup

To get started, you need a ready-to-use radosgw instance and an admin account with the necessary capabilities. Here's how you can set up a radosgw instance:

### If You Don't Have a Radosgw Setup

1. You can refer to the [Ceph official manual](http://docs.ceph.com/docs/master/start/) for a quick Ceph cluster setup. If you're not familiar with Ceph, this may be a bit challenging. An easier approach is available if you have **Docker** in your environment. To set up a standalone instance with an admin account using the [Ceph daemon Docker image](https://hub.docker.com/r/ceph/daemon/), follow these instructions:

   ```bash
   $ docker run -d -p 80:8080 -v /etc/ceph/:/etc/ceph/ -e CEPH_DEMO_UID=qqq -e CEPH_DEMO_ACCESS_KEY=qqq -e CEPH_DEMO_SECRET_KEY=qqq -e NETWORK_AUTO_DETECT=4 --name rgw ceph/daemon:v6.0.3-stable-6.0-pacific-centos-8-x86_64 demo
   ```

   Note that port 80 should be available.

2. It takes about two minutes to initialize the Ceph cluster. Check if the setup succeeded with the following command:

   ```bash
   $ timeout 120 bash -c "until docker logs rgw &> rgw.log && grep SUCCESS rgw.log; do sleep 1; done"
   ```

3. Once the setup is complete, you can run radosgw-admin4j tests without any additional configuration on the client side since the [default config](https://github.com/twonote/radosgw-admin4j/blob/master/src/test/resources/rgwadmin.properties) should suffice. Run tests with the following commands:

   ```bash
   $ git clone https://github.com/twonote/radosgw-admin4j.git
   $ cd radosgw-admin4j
   $ mvn test
   ```

### If You Already Have a Radosgw Instance

1. First, ensure you have an admin account. If not, create the account with the following commands:

   ```bash
   $ radosgw-admin user create --uid=qqq --display-name="qqq" --access-key=qqq --secret-key=qqq
   $ radosgw-admin --id admin caps add --caps="buckets=*;users=*;usage=*;metadata=*" --uid=qqq
   ```

2. Enter the key pair (qqq,qqq) and add your radosgw endpoint to the [config file](https://github.com/twonote/radosgw-admin4j/blob/master/src/test/resources/rgwadmin.properties).

3. Note that radosgw does not enable [usage log](http://docs.ceph.com/docs/master/radosgw/admin/#usage) by default. If you need this feature or plan to run test cases, make sure you enable the usage log in your Ceph config file. For example, in your ceph.conf:

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

That's it!

# Contributing
We welcome all contributions to the project. Our code style follows the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html), and we use [google-java-format](https://github.com/google/google-java-format) for code formatting. There are no unusual policies, and we encourage you to get involved.

# Legal
Copyright 2017-2026 [twonote](http://twonote.github.io/) & The "radosgw-admin4j" contributors.

Licensed under the Apache License, Version 2.0. You may not use this file except in compliance with the License. You can obtain a copy of the License at [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0). Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, without warranties or conditions of any kind, either express or implied. See the License for the specific language governing permissions and limitations under the License.
