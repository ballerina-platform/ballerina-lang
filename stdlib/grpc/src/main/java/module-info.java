module io.ballerina.grpc {
    requires slf4j.api;
    requires org.wso2.transport.http.netty;
    requires io.ballerina.jvm;
    requires io.netty.codec.http;
    requires io.netty.buffer;
    requires protobuf.java;
    requires org.apache.commons.lang3;
    requires io.ballerina.http;
    requires io.ballerina.config;
    requires handlebars;
    requires proto.google.common.protos;
    requires io.ballerina.lang;
    requires java.logging;
}