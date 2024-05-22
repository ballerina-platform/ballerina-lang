module io.ballerina.maven.resolver {
    requires progressbar;
    requires plexus.utils;
    requires maven.model;
    requires maven.resolver.provider;
    requires org.apache.maven.resolver;
    requires org.apache.maven.resolver.connector.basic;
    requires org.apache.maven.resolver.impl;
    requires org.apache.maven.resolver.spi;
    requires org.apache.maven.resolver.transport.file;
    requires org.apache.maven.resolver.transport.http;
    requires org.apache.maven.resolver.util;

    exports org.ballerinalang.maven;
    exports org.ballerinalang.maven.exceptions;
    exports org.ballerinalang.maven.bala.client;
}