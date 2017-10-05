/**
 * Created by hrchu on 2017-10-05.
 */
module org.twonote.radosgwadmin4j {
    // TODO: Change requires to named modules
    requires gson;
    requires guava;
    requires okhttp;
    exports org.twonote.rgwadmin4j;
    exports org.twonote.rgwadmin4j.model;
    exports org.twonote.rgwadmin4j.model.usage;
    opens org.twonote.rgwadmin4j.model to gson;
    opens org.twonote.rgwadmin4j.model.usage to gson;

}