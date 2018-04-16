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

import com.uber.jaeger.Configuration;
import io.opentracing.Tracer;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.util.tracer.OpenTracer;
import org.ballerinalang.util.tracer.exception.InvalidConfigurationException;

import java.io.PrintStream;
import java.util.Map;
import java.util.Objects;

import static org.ballerinalang.observe.trace.extension.jaeger.Constants.DEFAULT_REPORTER_FLUSH_INTERVAL;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.DEFAULT_REPORTER_HOSTNAME;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.DEFAULT_REPORTER_LOG_SPANS;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.DEFAULT_REPORTER_MAX_BUFFER_SPANS;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.DEFAULT_REPORTER_PORT;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.DEFAULT_SAMPLER_PARAM;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.DEFAULT_SAMPLER_TYPE;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.REPORTER_FLUSH_INTERVAL_MS_CONFIG;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.REPORTER_HOST_NAME_CONFIG;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.REPORTER_LOG_SPANS_CONFIG;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.REPORTER_MAX_BUFFER_SPANS_CONFIG;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.REPORTER_PORT_CONFIG;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.SAMPLER_PARAM_CONFIG;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.SAMPLER_TYPE_CONFIG;
import static org.ballerinalang.observe.trace.extension.jaeger.Constants.TRACER_NAME;

/**
 * This is the open tracing extension class for {@link OpenTracer}.
 */
@JavaSPIService("org.ballerinalang.util.tracer.OpenTracer")
public class OpenTracingExtension implements OpenTracer {

    private static final PrintStream console = System.out;
    private static final PrintStream consoleError = System.err;
    private Map<String, String> configProperties;

    @Override
    public void init(Map<String, String> configProperties) {
        console.println("ballerina: started publishing tracers to Jaeger on "
                + configProperties.getOrDefault(REPORTER_HOST_NAME_CONFIG, DEFAULT_REPORTER_HOSTNAME) + ":" +
                getValidIntegerConfig(configProperties.get(REPORTER_PORT_CONFIG),
                        DEFAULT_REPORTER_PORT, REPORTER_PORT_CONFIG));
        this.configProperties = configProperties;
    }

    @Override
    public Tracer getTracer(String tracerName, String serviceName) throws InvalidConfigurationException {

        if (Objects.isNull(configProperties)) {
            throw new InvalidConfigurationException("Tracer not initialized with configurations");
        }

        return new Configuration(
                serviceName,
                new Configuration.SamplerConfiguration(
                        configProperties.getOrDefault(SAMPLER_TYPE_CONFIG, DEFAULT_SAMPLER_TYPE),
                        getValidIntegerConfig(configProperties.get(SAMPLER_PARAM_CONFIG),
                                DEFAULT_SAMPLER_PARAM, SAMPLER_PARAM_CONFIG)
                ),
                new Configuration.ReporterConfiguration(
                        Boolean.parseBoolean(String.valueOf(configProperties
                                .getOrDefault(REPORTER_LOG_SPANS_CONFIG, String.valueOf(DEFAULT_REPORTER_LOG_SPANS)))),
                        configProperties.getOrDefault(REPORTER_HOST_NAME_CONFIG, DEFAULT_REPORTER_HOSTNAME),
                        getValidIntegerConfig(configProperties.get(REPORTER_PORT_CONFIG),
                                DEFAULT_REPORTER_PORT, REPORTER_PORT_CONFIG),
                        getValidIntegerConfig(configProperties.get(REPORTER_FLUSH_INTERVAL_MS_CONFIG),
                                DEFAULT_REPORTER_FLUSH_INTERVAL, REPORTER_FLUSH_INTERVAL_MS_CONFIG),
                        getValidIntegerConfig(configProperties.get(REPORTER_MAX_BUFFER_SPANS_CONFIG),
                                DEFAULT_REPORTER_MAX_BUFFER_SPANS, REPORTER_MAX_BUFFER_SPANS_CONFIG)
                )
        ).getTracerBuilder().withScopeManager(NoOpScopeManager.INSTANCE).build();
    }

    @Override
    public String getName() {
        return TRACER_NAME;
    }

    private int getValidIntegerConfig(String config, int defaultValue, String configName) {
        if (config == null) {
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(config);
            } catch (NumberFormatException ex) {
                consoleError.println("ballerina: observability tracing configuration " + configName
                        + " is invalid. Default value of " + defaultValue + " will be used.");
                return defaultValue;
            }
        }
    }
}
