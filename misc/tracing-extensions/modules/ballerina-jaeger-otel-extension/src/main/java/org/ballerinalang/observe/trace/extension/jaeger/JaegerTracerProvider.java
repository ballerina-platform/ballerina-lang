/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.observe.trace.extension.jaeger;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelProvider;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jvm.observability.InvalidConfigurationException;
import org.ballerinalang.jvm.observability.tracer.spi.TracerProvider;
import org.ballerinalang.observe.trace.extension.jaeger.sampler.RateLimitingSampler;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import static io.opentelemetry.semconv.ResourceAttributes.SERVICE_NAME;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.DEFAULT_REPORTER_FLUSH_INTERVAL;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.DEFAULT_REPORTER_HOSTNAME;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.DEFAULT_REPORTER_MAX_BUFFER_SPANS;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.DEFAULT_REPORTER_PORT;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.DEFAULT_SAMPLER_PARAM;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.DEFAULT_SAMPLER_TYPE;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.REPORTER_FLUSH_INTERVAL_MS_CONFIG;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.REPORTER_HOST_NAME_CONFIG;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.REPORTER_MAX_BUFFER_SPANS_CONFIG;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.REPORTER_PORT_CONFIG;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.SAMPLER_PARAM_CONFIG;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.SAMPLER_TYPE_CONFIG;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.TRACER_NAME;

/**
 * This is the Jaeger tracing extension class for {@link TracerProvider}.
 */
public class JaegerTracerProvider implements TracerProvider {
    private ConfigRegistry configRegistry;
    private String hostname;
    private int port;
    private String samplerType;
    private Number samplerParam;
    private int reporterFlushInterval;
    private int reporterBufferSize;

    private static final PrintStream console = System.out;

    private SdkTracerProviderBuilder tracerProviderBuilder;

    @Override
    public String getName() {
        return TRACER_NAME;
    }

    @Override
    public void init() throws InvalidConfigurationException {
        configRegistry = ConfigRegistry.getInstance();
        try {
            port = Integer.parseInt(
                    configRegistry.getConfigOrDefault(REPORTER_PORT_CONFIG, String.valueOf(DEFAULT_REPORTER_PORT)));
            hostname = configRegistry.getConfigOrDefault(REPORTER_HOST_NAME_CONFIG, DEFAULT_REPORTER_HOSTNAME);

            samplerType = configRegistry.getConfigOrDefault(SAMPLER_TYPE_CONFIG, DEFAULT_SAMPLER_TYPE);
            samplerParam = Float.valueOf(
                    configRegistry.getConfigOrDefault(SAMPLER_PARAM_CONFIG, String.valueOf(DEFAULT_SAMPLER_PARAM)));
            reporterFlushInterval = Integer.parseInt(configRegistry.getConfigOrDefault(
                    REPORTER_FLUSH_INTERVAL_MS_CONFIG, String.valueOf(DEFAULT_REPORTER_FLUSH_INTERVAL)));
            reporterBufferSize = Integer.parseInt(configRegistry.getConfigOrDefault
                    (REPORTER_MAX_BUFFER_SPANS_CONFIG, String.valueOf(DEFAULT_REPORTER_MAX_BUFFER_SPANS)));

        } catch (IllegalArgumentException | ArithmeticException e) {
            throw new InvalidConfigurationException(e.getMessage());
        }

        String reporterEndpoint = hostname + ":" + port;

        ManagedChannel jaegerChannel = new NettyChannelProvider()
                .builderForTarget(reporterEndpoint)
                .usePlaintext()
                .build();

        OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder()
                .setChannel(jaegerChannel)
                .build();

        tracerProviderBuilder = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor
                        .builder(exporter)
                        .setMaxExportBatchSize(reporterBufferSize)
                        .setExporterTimeout(reporterFlushInterval, TimeUnit.MILLISECONDS)
                        .build());

        tracerProviderBuilder.setSampler(selectSampler(samplerType, samplerParam));

        console.println("ballerina: started publishing tracers to Jaeger on " + reporterEndpoint);
    }

    private static Sampler selectSampler(String samplerType, Number samplerParam) {
        switch (samplerType) {
            default:
            case "const":
                if (samplerParam.intValue() == 0) {
                    return Sampler.alwaysOff();
                } else {
                    return Sampler.alwaysOn();
                }
            case "probabilistic":
                return Sampler.traceIdRatioBased(samplerParam.doubleValue());
            case RateLimitingSampler.TYPE:
                return new RateLimitingSampler(samplerParam.intValue());
        }
    }

    @Override
    public Tracer getTracer(String serviceName) {
        return tracerProviderBuilder.setResource(
                Resource.create(Attributes.of(SERVICE_NAME, serviceName)))
                .build().get("jaeger");
    }

    @Override
    public ContextPropagators getPropagators() {

        return ContextPropagators.create(W3CTraceContextPropagator.getInstance());
    }
}
