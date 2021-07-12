module io.ballerina.observability.test.utils {
    requires com.google.gson;
    requires io.ballerina.runtime;
    requires io.netty.transport;
    requires io.netty.buffer;
    requires io.netty.codec.http;
    requires io.ballerina.lang;
    requires io.opentelemetry.api;
    requires io.opentelemetry.sdk.trace;
    requires io.opentelemetry.sdk.testing;
    requires io.opentelemetry.context;

    provides io.ballerina.runtime.observability.tracer.spi.TracerProvider
            with org.ballerina.testobserve.tracing.extension.BMockTracerProvider;
}
