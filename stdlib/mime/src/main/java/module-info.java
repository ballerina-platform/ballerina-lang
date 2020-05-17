module io.ballerina.mime {
    exports org.ballerinalang.mime.util;
    exports org.ballerinalang.mime.nativeimpl;
    requires io.ballerina.jvm;
    requires slf4j.api;
    requires org.wso2.transport.http.netty;
    requires io.netty.codec.http;
    requires io.ballerina.io;
    requires io.ballerina.lang;
    requires org.jvnet.mimepull;
    requires jakarta.activation;
}