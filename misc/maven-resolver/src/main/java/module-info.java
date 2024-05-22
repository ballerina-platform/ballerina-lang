module io.ballerina.maven.resolver {
	requires progressbar;
	requires maven.model;
    requires org.apache.maven.resolver;
    requires maven.resolver.provider;
    requires plexus.utils;
    requires org.apache.maven.resolver.connector.basic;
    requires org.apache.maven.resolver.impl;
    requires org.apache.maven.resolver.spi;
    requires org.apache.maven.resolver.transport.http;
    requires org.apache.maven.resolver.util;
    requires org.apache.maven.resolver.transport.file;

    exports org.ballerinalang.maven;
    exports org.ballerinalang.maven.exceptions;
    exports org.ballerinalang.maven.bala.client;
}