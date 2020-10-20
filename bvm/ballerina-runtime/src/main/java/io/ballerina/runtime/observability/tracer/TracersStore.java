/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.observability.tracer;

import io.opentracing.Tracer;
import org.ballerinalang.config.ConfigRegistry;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_OBSERVABILITY_PROVIDER;
import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static io.ballerina.runtime.observability.tracer.TraceConstants.JAEGER;
import static io.ballerina.runtime.observability.tracer.TraceConstants.TRACER_NAME_CONFIG;

/**
 * Class that creates the tracer for a given service.
 */
public class TracersStore {

    private OpenTracer tracerGenerator;
    private Map<String, Tracer> store;
    private static final PrintStream consoleError = System.err;
    private static final TracersStore instance = new TracersStore();

    public static TracersStore getInstance() {
        return instance;
    }

    private TracersStore() {
    }

    public void loadTracers() {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        if (configRegistry.getAsBoolean(CONFIG_TRACING_ENABLED)) {
            String overallProviderName = configRegistry.getConfigOrDefault(CONFIG_OBSERVABILITY_PROVIDER, JAEGER);
            String tracerName = configRegistry.getConfigOrDefault(TRACER_NAME_CONFIG, overallProviderName);

            ServiceLoader<OpenTracer> openTracers = ServiceLoader.load(OpenTracer.class);
            for (OpenTracer openTracer : openTracers) {
                if (tracerName.equalsIgnoreCase(openTracer.getName())) {
                    tracerGenerator = openTracer;
                    break;
                }
            }

            if (tracerGenerator != null) {
                try {
                    tracerGenerator.init();
                } catch (InvalidConfigurationException e) {
                    consoleError.println("error: error in observability tracing configurations: " + e.getMessage());
                }
            } else {
                consoleError.println(
                        "error: observability enabled but no tracing extension found for name " + tracerName);
            }
        }
        store = new HashMap<>();
    }

    /**
     * Return trace implementations for a specific service.
     *
     * @param serviceName name of service of whose trace implementations are needed
     * @return trace implementations i.e: zipkin, jaeger
     */
    public Tracer getTracer(String serviceName) {
        Tracer openTracer = null;
        if (store.containsKey(serviceName)) {
            openTracer = store.get(serviceName);
        } else {
            if (tracerGenerator != null) {
                try {
                    openTracer = tracerGenerator.getTracer(serviceName);
                    store.put(serviceName, openTracer);
                } catch (Throwable e) {
                    consoleError.println("error: error getting tracer for " + serviceName + " service from "
                            + tracerGenerator.getName() + ". " + e.getMessage());
                }
            }
        }
        return openTracer;
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
