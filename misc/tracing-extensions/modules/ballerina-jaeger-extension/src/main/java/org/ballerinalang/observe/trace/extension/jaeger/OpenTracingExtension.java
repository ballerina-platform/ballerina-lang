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

import io.ballerina.runtime.observability.tracer.InvalidConfigurationException;
import io.ballerina.runtime.observability.tracer.OpenTracer;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.internal.samplers.ProbabilisticSampler;
import io.jaegertracing.internal.samplers.RateLimitingSampler;
import io.opentracing.Tracer;
import org.ballerinalang.config.ConfigRegistry;

import java.io.PrintStream;
import java.util.Objects;

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
 * This is the open tracing extension class for {@link OpenTracer}.
 */
public class OpenTracingExtension implements OpenTracer {

    private ConfigRegistry configRegistry;
    private String hostname;
    private int port;
    private String samplerType;
    private Number samplerParam;
    private int reporterFlushInterval;
    private int reporterBufferSize;

    private static final PrintStream console = System.out;
    private static final PrintStream consoleError = System.err;

    @Override
    public void init() throws InvalidConfigurationException {
        configRegistry = ConfigRegistry.getInstance();

        try {
            port = Integer.parseInt(
                    configRegistry.getConfigOrDefault(REPORTER_PORT_CONFIG, String.valueOf(DEFAULT_REPORTER_PORT)));
            hostname = configRegistry.getConfigOrDefault(REPORTER_HOST_NAME_CONFIG, DEFAULT_REPORTER_HOSTNAME);

            samplerType = configRegistry.getConfigOrDefault(SAMPLER_TYPE_CONFIG, DEFAULT_SAMPLER_TYPE);
            if (!(samplerType.equals(ConstSampler.TYPE) || samplerType.equals(RateLimitingSampler.TYPE)
                    || samplerType.equals(ProbabilisticSampler.TYPE))) {
                samplerType = DEFAULT_SAMPLER_TYPE;
                consoleError.println(
                        "error: Jaeger configuration: \"sampler type\" invalid. Defaulted to const sampling");
            }

            samplerParam = Float.valueOf(
                    configRegistry.getConfigOrDefault(SAMPLER_PARAM_CONFIG, String.valueOf(DEFAULT_SAMPLER_PARAM)));
            reporterFlushInterval = Integer.parseInt(configRegistry.getConfigOrDefault(
                    REPORTER_FLUSH_INTERVAL_MS_CONFIG, String.valueOf(DEFAULT_REPORTER_FLUSH_INTERVAL)));
            reporterBufferSize = Integer.parseInt(configRegistry.getConfigOrDefault
                    (REPORTER_MAX_BUFFER_SPANS_CONFIG, String.valueOf(DEFAULT_REPORTER_MAX_BUFFER_SPANS)));

        } catch (IllegalArgumentException | ArithmeticException e) {
            throw new InvalidConfigurationException(e.getMessage());
        }
        console.println("ballerina: started publishing tracers to Jaeger on " + hostname + ":" + port);
    }

    @Override
    public Tracer getTracer(String serviceName) {
        if (Objects.isNull(configRegistry)) {
            throw new IllegalStateException("Tracer not initialized with configurations");
        }

        return new Configuration(serviceName)
                .withSampler(new Configuration.SamplerConfiguration()
                        .withType(samplerType)
                        .withParam(samplerParam))
                .withReporter(new Configuration.ReporterConfiguration()
                        .withLogSpans(Boolean.FALSE)
                        .withSender(new Configuration.SenderConfiguration()
                                .withAgentHost(hostname)
                                .withAgentPort(port))
                        .withFlushInterval(reporterFlushInterval)
                        .withMaxQueueSize(reporterBufferSize))
                .getTracerBuilder()
                .withScopeManager(NoOpScopeManager.INSTANCE)
                .build();
    }

    @Override
    public String getName() {
        return TRACER_NAME;
    }

}
