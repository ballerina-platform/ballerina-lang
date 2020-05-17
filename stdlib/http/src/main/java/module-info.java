module io.ballerina.http {
    exports org.ballerinalang.net.http;
    requires org.wso2.transport.http.netty;
    requires slf4j.api;
    requires io.ballerina.jvm;
    requires io.ballerina.logging;
    requires io.netty.codec.http;
    requires io.ballerina.mime;
    requires io.netty.buffer;
    requires org.apache.commons.lang3;
    requires io.ballerina.config;
    requires io.ballerina.io;
    requires io.netty.codec;
    requires io.netty.transport;
    requires io.netty.common;
    requires io.ballerina.lang;
    requires io.ballerina.lang.typedesc;

}