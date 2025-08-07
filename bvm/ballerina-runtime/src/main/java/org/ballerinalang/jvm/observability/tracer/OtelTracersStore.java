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

package org.ballerinalang.jvm.observability.tracer;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.observability.tracer.InvalidConfigurationException;
import org.ballerinalang.jvm.observability.tracer.spi.TracerProvider;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.JAEGER;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.TRACER_NAME_CONFIG;

/**
 * Class that creates the tracer for a given service.
 */
public class OtelTracersStore {

    private TracerProvider tracerProvider;
    private Map<String, Tracer> store;
    private static final PrintStream consoleError = System.err;
    private static final OtelTracersStore instance = new OtelTracersStore();
    private ContextPropagators propagators;

    public static OtelTracersStore getInstance() {
        return instance;
    }

    private OtelTracersStore() {
    }

    public void setTracerGenerator(TracerProvider tracerProvider) {
        this.tracerProvider = tracerProvider;
        if (tracerProvider != null) {
            propagators = tracerProvider.getPropagators();
        }
        store = new HashMap<>();
    }

    public void loadTracers() {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        if (configRegistry.getAsBoolean(CONFIG_TRACING_ENABLED)) {
            this.store = new HashMap<>();

            String tracerName = configRegistry.getConfigOrDefault(TRACER_NAME_CONFIG, JAEGER);
            for (TracerProvider providerFactory : ServiceLoader.load(TracerProvider.class)) {
                if (tracerName.equalsIgnoreCase(providerFactory.getName())) {
                    tracerProvider = providerFactory;
                    try {
                        tracerProvider.init();
                    } catch (InvalidConfigurationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else {
            this.store = new HashMap<>();
        }
    }

    /**
     * Return trace implementations for a specific service.
     *
     * @param serviceName name of service of whose trace implementations are needed
     * @return trace implementations i.e: zipkin, jaeger
     */
    public Tracer getTracer(String serviceName) {
        if (!isInitialized()) {
            throw BallerinaErrors.createError(StringUtils.fromString("error: the tracer store is not initialized " +
                    "because observability has not been enabled."));
        }
        Tracer tracer;
        if (store.containsKey(serviceName)) {
            tracer = store.get(serviceName);
        } else {
            if (tracerProvider != null) {
                try {
                    tracer = tracerProvider.getTracer(serviceName);
                } catch (Throwable e) {
                    tracer = io.opentelemetry.api.trace.TracerProvider.noop().get("");
                    consoleError.println("error: tracing disabled as getting tracer for " + serviceName + " service. "
                            + e.getMessage());
                }
                store.put(serviceName, tracer);
            } else {
                tracer = io.opentelemetry.api.trace.TracerProvider.noop().get("");
                consoleError.println("error: tracing disabled as the tracer provider had not been initialized.");
            }
        }
        return tracer;
    }

    public ContextPropagators getPropagators() {
        if (propagators != null) {
            return propagators;
        }
        return ContextPropagators.noop();
    }

    /**
     * Checks whether the tracer store is initialized.
     *
     * @return boolean value whether it's initialized.
     */
    public boolean isInitialized() {
        return this.store != null;
    }
}
