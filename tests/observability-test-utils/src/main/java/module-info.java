module io.ballerina.observability.test.utils {
    requires gson;
    requires opentracing.mock;
    requires io.ballerina.runtime;
    requires io.netty.transport;
    requires io.netty.buffer;
    requires io.netty.codec.http;
    requires io.ballerina.lang;
    requires opentracing.api;

    provides io.ballerina.runtime.observability.tracer.spi.TracerProvider
            with org.ballerina.testobserve.tracing.extension.BMockTracerProvider;
}
