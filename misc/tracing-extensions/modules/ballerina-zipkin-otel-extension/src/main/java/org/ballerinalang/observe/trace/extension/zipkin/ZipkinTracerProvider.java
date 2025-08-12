/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.observe.trace.extension.zipkin;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jvm.observability.InvalidConfigurationException;
import org.ballerinalang.jvm.observability.tracer.spi.TracerProvider;
import org.ballerinalang.observe.trace.extension.zipkin.sampler.RateLimitingSampler;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import static io.opentelemetry.semconv.ResourceAttributes.SERVICE_NAME;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_TABLE_TRACING;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.APPLICATION_LAYER_PROTOCOL;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.DEFAULT_REPORTER_API_CONTEXT;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.DEFAULT_REPORTER_COMPRESSION_ENABLED;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.DEFAULT_REPORTER_FLUSH_INTERVAL;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.DEFAULT_REPORTER_HOSTNAME;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.DEFAULT_REPORTER_MAX_BUFFER_SPANS;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.DEFAULT_REPORTER_PORT;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.DEFAULT_SAMPLER_PARAM;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.DEFAULT_SAMPLER_TYPE;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.REPORTER_API_CONTEXT_CONFIG;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.REPORTER_COMPRESSION_ENABLED_CONFIG;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.REPORTER_FLUSH_INTERVAL_MS_CONFIG;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.REPORTER_HOST_NAME_CONFIG;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.REPORTER_MAX_BUFFER_SPANS_CONFIG;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.REPORTER_PORT_CONFIG;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.SAMPLER_PARAM_CONFIG;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.SAMPLER_TYPE_CONFIG;
import static org.ballerinalang.observe.trace.extension.zipkin.Constants.TRACER_NAME;

/**
 * This is the Zipkin tracing extension class for {@link TracerProvider}.
 */
public class ZipkinTracerProvider implements TracerProvider {
    private ConfigRegistry configRegistry;
    private String hostname;
    private int port;
    private String samplerType;
    private Number samplerParam;
    private int reporterFlushInterval;
    private int reporterBufferSize;
    private String apiContext;
    private String compressionMethod = "gzip";

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
            apiContext = configRegistry.getConfigOrDefault(getFullQualifiedConfig(REPORTER_API_CONTEXT_CONFIG),
                    DEFAULT_REPORTER_API_CONTEXT);
            boolean compressionEnabled = Boolean.parseBoolean(configRegistry.getConfigOrDefault(
                    getFullQualifiedConfig(REPORTER_COMPRESSION_ENABLED_CONFIG),
                    String.valueOf(DEFAULT_REPORTER_COMPRESSION_ENABLED)));
            if (compressionEnabled) {
                compressionMethod = "gzip";
            } else {
                compressionMethod = "none";
            }
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

        String reporterEndpoint = APPLICATION_LAYER_PROTOCOL + "://" + hostname + ":" + port + apiContext;

        ZipkinSpanExporter exporter = ZipkinSpanExporter.builder()
                .setEndpoint(reporterEndpoint)
                .setCompression(compressionMethod)
                .build();

        tracerProviderBuilder = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor
                        .builder(exporter)
                        .setMaxExportBatchSize(reporterBufferSize)
                        .setExporterTimeout(reporterFlushInterval, TimeUnit.MILLISECONDS)
                        .build());

        tracerProviderBuilder.setSampler(selectSampler(samplerType, samplerParam));

        console.println("ballerina: started publishing traces to Zipkin on " + reporterEndpoint);
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

    private String getFullQualifiedConfig(String configName) {
        return CONFIG_TABLE_TRACING + "." + TRACER_NAME + "." + configName;
    }

    @Override
    public Tracer getTracer(String serviceName) {
        return tracerProviderBuilder.setResource(
                Resource.create(Attributes.of(SERVICE_NAME, serviceName)))
                .build().get("zipkin");
    }

    @Override
    public ContextPropagators getPropagators() {
        return ContextPropagators.create(B3Propagator.injectingSingleHeader());
    }
}
