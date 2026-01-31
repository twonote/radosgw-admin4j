/**
 * Radosgw Admin SDK for Java - A Ceph Object Storage Admin client library
 * 
 * <p>This module provides a comprehensive API for managing Ceph Object Storage (Radosgw)
 * including user management, quota control, usage reporting, and bucket operations.
 */
module org.twonote.rgwadmin4j {
    // Export public API packages
    exports org.twonote.rgwadmin4j;
    exports org.twonote.rgwadmin4j.model;
    exports org.twonote.rgwadmin4j.model.usage;
    exports org.twonote.rgwadmin4j.impl;
    
    // Dependencies
    requires com.google.gson;
    requires okhttp3;
    requires com.google.common;
}
