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
package org.ballerinalang.observe.metrics.extension.prometheus;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.HTTPServer;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.observe.metrics.extension.micrometer.spi.MeterRegistryProvider;

import java.io.IOException;
import java.io.PrintStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.format.DateTimeParseException;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TABLE_METRICS;

/**
 * Support Prometheus with {@link PrometheusMeterRegistry}.
 */
@JavaSPIService("org.ballerinalang.observe.metrics.extension.micrometer.spi.MeterRegistryProvider")
public class PrometheusMeterRegistryProvider implements MeterRegistryProvider {

    private static final String METRICS_PORT = CONFIG_TABLE_METRICS + ".port";
    private static final int DEFAULT_PORT = 9797;

    private static final PrintStream console = System.out;
    private static final PrintStream consoleError = System.err;

    @Override
    public String getName() {
        return "Prometheus";
    }

    @Override
    public MeterRegistry get() {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(new BallerinaPrometheusConfig());
        String portConfigValue = configRegistry.getConfigOrDefault(METRICS_PORT, String.valueOf(DEFAULT_PORT));
        int port;
        try {
            port = Integer.parseInt(portConfigValue);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid port used for Prometheus HTTP endpoint");
        }
        InetSocketAddress socketAddress = new InetSocketAddress(port);
        try {
            new HTTPServer(socketAddress, registry.getPrometheusRegistry(), true);
            console.println("ballerina: started Prometheus HTTP endpoint " + socketAddress);
        } catch (BindException e) {
            consoleError.println("ballerina: failed to bind Prometheus HTTP endpoint " + socketAddress + ":  "
                    + e.getMessage());
        } catch (IOException e) {
            consoleError.println("ballerina: failed to start Prometheus HTTP endpoint " + socketAddress);
            throw new IllegalStateException(e);
        }
        return registry;
    }

    private static class BallerinaPrometheusConfig implements PrometheusConfig {
        private final ConfigRegistry configRegistry;
        private final PrintStream consoleErr = System.err;

        public BallerinaPrometheusConfig() {
            configRegistry = ConfigRegistry.getInstance();
        }

        @Override
        public String prefix() {
            return CONFIG_TABLE_METRICS;
        }

        @Override
        public Duration step() {
            String v = get(prefix() + ".step");
            Duration step = null;
            if (v != null) {
                try {
                    step = Duration.parse(v);
                } catch (DateTimeParseException e) {
                    step = null;
                    consoleErr.println("ballerina: error parsing duration for Prometheus step configuration");
                }
            }
            return step != null ? step : Duration.ofMinutes(1);
        }

        @Override
        public String get(String key) {
            return configRegistry.getConfiguration(key);
        }
    }
}
