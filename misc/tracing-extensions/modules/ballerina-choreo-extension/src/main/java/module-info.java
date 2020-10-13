module io.ballerina.choreo.extension {
    requires jaeger.core;
    requires opentracing.api;
    requires io.ballerina.jvm;
    requires grpc.api;
    requires com.google.common;
    requires grpc.stub;
    requires grpc.protobuf;
    requires com.google.protobuf;
    requires io.ballerina.config;

    exports org.ballerinalang.observe.trace.extension.choreo;
}