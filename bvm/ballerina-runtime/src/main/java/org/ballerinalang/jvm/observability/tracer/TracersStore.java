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

package org.ballerinalang.jvm.observability.tracer;

import io.opentracing.Tracer;
import org.ballerinalang.config.ConfigRegistry;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_OBSERVABILITY_PROVIDER;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.JAEGER;
import static org.ballerinalang.jvm.observability.tracer.TraceConstants.TRACER_NAME_CONFIG;

/**
 * Class that creates the tracer for a given service.
 */
public class TracersStore {

    private TracerGenerator tracer;
    private Map<String, Tracer> tracerStore = null;
    private static final PrintStream consoleError = System.err;
    private static TracersStore instance = new TracersStore();

    public static TracersStore getInstance() {
        return instance;
    }

    private TracersStore() {
    }

    public void loadTracers() {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        if (configRegistry.getAsBoolean(CONFIG_TRACING_ENABLED)) {

            this.tracerStore = new HashMap<>();

            ServiceLoader<OpenTracer> openTracers = ServiceLoader.load(OpenTracer.class);
            HashMap<String, OpenTracer> tracerMap = new HashMap<>();
            openTracers.forEach(t -> tracerMap.put(t.getName().toLowerCase(), t));

            String defaultReporterName = configRegistry.getConfigOrDefault(CONFIG_OBSERVABILITY_PROVIDER, JAEGER);
            String tracerName = configRegistry.getConfigOrDefault(TRACER_NAME_CONFIG, defaultReporterName);

            OpenTracer openTracer = tracerMap.get(tracerName.toLowerCase());
            if (openTracer != null) {
                try {
                    openTracer.init();
                    tracer = new TracerGenerator(openTracer.getName(), openTracer);
                } catch (InvalidConfigurationException e) {
                    consoleError.println("error: error in observability tracing configurations: " + e.getMessage());
                }
            } else {
                consoleError.println(
                        "error: observability enabled but no tracing extension found for name " + tracerName);
            }
        } else {
            this.tracerStore = new HashMap<>();
        }
    }

    /**
     * Return trace implementations for a specific service.
     *
     * @param serviceName name of service of whose trace implementations are needed
     * @return trace implementations i.e: zipkin, jaeger
     */
    public Tracer getTracer(String serviceName) {
        if (tracerStore.containsKey(serviceName)) {
            return tracerStore.get(serviceName);
        } else {
            Tracer openTracer = null;
            if (tracer != null) {
                try {
                    openTracer = tracer.generate(serviceName);
                } catch (Throwable e) {
                    consoleError.println("error: error getting tracer for " + tracer.name + ". " + e.getMessage());
                }
            }
            tracerStore.put(serviceName, openTracer);
            return openTracer;
        }
    }

    /**
     * Holds the tracerExt and generates a tracer upon request.
     */
    private static class TracerGenerator {
        String name;
        OpenTracer tracer;

        TracerGenerator(String name, OpenTracer tracer) {
            this.name = name;
            this.tracer = tracer;
        }

        Tracer generate(String serviceName) {
            return tracer.getTracer(name, serviceName);
        }
    }

    /**
     * Checks whether the tracer store is initialized.
     *
     * @return boolean value whether it's initialized.
     */
    public boolean isInitialized() {
        return this.tracerStore != null;
    }
}
