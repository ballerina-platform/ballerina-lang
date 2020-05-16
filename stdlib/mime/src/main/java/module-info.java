module ballerina.mime {
    exports org.ballerinalang.mime.util;
    exports org.ballerinalang.mime.nativeimpl;
    requires ballerina.runtime;
    requires slf4j.api;
    requires org.wso2.transport.http.netty;
    requires io.netty.codec.http;
    requires ballerina.io;
    requires ballerina.lang;
    requires org.jvnet.mimepull;
    requires jakarta.activation;
}