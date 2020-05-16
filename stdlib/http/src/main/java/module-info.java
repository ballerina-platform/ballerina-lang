module io.ballerina.http {
    exports org.ballerinalang.net.http;
    requires org.wso2.transport.http.netty;
    requires slf4j.api;
    requires ballerina.runtime;
    requires ballerina.logging;
    requires io.netty.codec.http;
    requires ballerina.mime;
    requires io.netty.buffer;
    requires org.apache.commons.lang3;
    requires ballerina.config;
    requires ballerina.io;
    requires io.netty.codec;
    requires io.netty.transport;
    requires io.netty.common;
    requires ballerina.lang;
    requires io.ballerina.lang.typedesc;

}