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

import io.opentracing.Tracer;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.util.tracer.OpenTracer;
import org.ballerinalang.util.tracer.exception.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

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

    private static final Logger logger = LoggerFactory.getLogger(OpenTracingExtension.class);
    private static final String NAME = "jaeger";

    @Override
    public Tracer getTracer(String tracerName, Properties configProperties, String serviceName)
            throws InvalidConfigurationException {
        if (!tracerName.equalsIgnoreCase(TRACER_NAME)) {
            throw new InvalidConfigurationException("Unexpected tracer name! " +
                    "The tracer name supported by this extension is : " + TRACER_NAME + " but found : "
                    + tracerName);
        }
        validateConfiguration(configProperties);
        return new com.uber.jaeger.Configuration(serviceName,
                new com.uber.jaeger.Configuration.SamplerConfiguration(
                        (String) configProperties.get(SAMPLER_TYPE_CONFIG)
                        , (Integer) configProperties.get(SAMPLER_PARAM_CONFIG)),
                new com.uber.jaeger.Configuration.ReporterConfiguration(
                        (Boolean) configProperties.get(REPORTER_LOG_SPANS_CONFIG),
                        (String) configProperties.get(REPORTER_HOST_NAME_CONFIG),
                        (Integer) configProperties.get(REPORTER_PORT_CONFIG),
                        (Integer) configProperties.get(REPORTER_FLUSH_INTERVAL_MS_CONFIG),
                        (Integer) configProperties.get(REPORTER_MAX_BUFFER_SPANS_CONFIG))
        ).getTracerBuilder().withScopeManager(NoOpScopeManager.INSTANCE).build();
    }

    @Override
    public String getName() {
        return NAME;
    }

    private void validateConfiguration(Properties configuration) {
        setValidatedStringConfig(configuration, SAMPLER_TYPE_CONFIG, DEFAULT_SAMPLER_TYPE);
        setValidatedIntegerConfig(configuration, SAMPLER_PARAM_CONFIG, DEFAULT_SAMPLER_PARAM.intValue());
        setValidatedBooleanConfig(configuration, REPORTER_LOG_SPANS_CONFIG, DEFAULT_REPORTER_LOG_SPANS);
        setValidatedStringConfig(configuration, REPORTER_HOST_NAME_CONFIG, DEFAULT_REPORTER_HOSTNAME);
        setValidatedIntegerConfig(configuration, REPORTER_PORT_CONFIG, DEFAULT_REPORTER_PORT);
        setValidatedIntegerConfig(configuration, REPORTER_FLUSH_INTERVAL_MS_CONFIG, DEFAULT_REPORTER_FLUSH_INTERVAL);
        setValidatedIntegerConfig(configuration, REPORTER_MAX_BUFFER_SPANS_CONFIG, DEFAULT_REPORTER_MAX_BUFFER_SPANS);
    }

    private void setValidatedStringConfig(Properties configuration, String configName, String defaultValue) {
        Object configValue = configuration.get(configName);
        if (configValue == null || configValue.toString().trim().isEmpty()) {
            configuration.put(configName, defaultValue);
        } else {
            configuration.put(configName, configValue.toString().trim());
        }
    }

    private void setValidatedIntegerConfig(Properties configuration, String configName, int defaultValue) {
        Object configValue = configuration.get(configName);
        if (configValue == null) {
            configuration.put(configName, defaultValue);
        } else {
            try {
                configuration.put(configName, Integer.parseInt(configValue.toString()));
            } catch (NumberFormatException ex) {
                logger.warn(String.format("Open tracing configuration for tracer name - %s expects " +
                                "configuration element : %swith integer type but found non integer : %s ! Therefore " +
                                "assigning default value : %d for %s configuration.", TRACER_NAME, configName,
                        configValue.toString(), defaultValue, configName));
                configuration.put(configName, defaultValue);
            }
        }
    }

    private void setValidatedBooleanConfig(Properties configuration, String configName, boolean defaultValue) {
        Object configValue = configuration.get(configName);
        if (configValue == null) {
            configuration.put(configName, defaultValue);
        } else {
            String configStringValue = configValue.toString().trim();
            if (configStringValue.equalsIgnoreCase(Boolean.TRUE.toString())
                    || configStringValue.equalsIgnoreCase(Boolean.FALSE.toString())) {
                configuration.put(configName, Boolean.parseBoolean(configStringValue));
            } else {
                logger.warn(String.format("Open tracing configuration for tracer name - %s expects " +
                                "configuration element : %swith boolean type (true/false) but found non boolean :" +
                                " %s ! Therefore assigning default value : %s for %s configuration.",
                        TRACER_NAME, configName, configStringValue, defaultValue, configName));
                configuration.put(configName, defaultValue);
            }
        }
    }
}
