module io.ballerina.maven.resolver {
    exports org.ballerinalang.maven;
    exports org.ballerinalang.maven.exceptions;
    requires maven.resolver.provider;
    requires org.apache.maven.resolver;
    requires org.apache.maven.resolver.connector.basic;
    requires org.apache.maven.resolver.impl;
    requires org.apache.maven.resolver.spi;
    requires org.apache.maven.resolver.transport.file;
    requires org.apache.maven.resolver.transport.http;
    requires org.apache.maven.resolver.util;
    requires progressbar;
}