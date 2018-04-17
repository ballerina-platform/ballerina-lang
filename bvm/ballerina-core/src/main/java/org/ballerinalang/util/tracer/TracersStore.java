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

package org.ballerinalang.util.tracer;

import io.opentracing.Tracer;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.util.tracer.exception.InvalidConfigurationException;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;
import static org.ballerinalang.util.tracer.TraceConstants.JAEGER;
import static org.ballerinalang.util.tracer.TraceConstants.TRACER_NAME_CONFIG;

/**
 * Class that creates the list of tracers for a given service.
 */
class TracersStore {

    private List<TracerGenerator> tracers;
    private Map<String, Map<String, Tracer>> tracerStore;
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

            this.tracers = new ArrayList<>();
            this.tracerStore = new HashMap<>();

            ServiceLoader<OpenTracer> openTracers = ServiceLoader.load(OpenTracer.class);
            HashMap<String, OpenTracer> tracerMap = new HashMap<>();
            openTracers.forEach(t -> tracerMap.put(t.getName().toLowerCase(), t));

            String tracerName = configRegistry.getConfigOrDefault(TRACER_NAME_CONFIG, JAEGER);

            OpenTracer tracer = tracerMap.get(tracerName.toLowerCase());
            if (tracer != null) {
                try {
                    tracer.init();
                    this.tracers.add(new TracerGenerator(tracer.getName(), tracer));
                } catch (InvalidConfigurationException e) {
                    consoleError.println("ballerina: error in observability tracing configurations: " + e.getMessage());
                }
            } else {
                consoleError.println(
                        "ballerina: observability enabled but no tracing extension found for name " + tracerName);
            }
        } else {
            this.tracers = Collections.emptyList();
            this.tracerStore = new HashMap<>();
        }
    }

    /**
     * Return trace implementations for a specific service
     *
     * @param serviceName name of service of whose trace implementations are needed
     * @return trace implementations i.e: zipkin, jaeger
     */
    Map<String, Tracer> getTracers(String serviceName) {
        if (tracerStore.containsKey(serviceName)) {
            return tracerStore.get(serviceName);
        } else {
            Map<String, Tracer> tracerMap = new HashMap<>();
            for (TracerGenerator tracerGenerator : tracers) {
                try {
                    Tracer tracer = tracerGenerator.generate(serviceName);
                    tracerMap.put(tracerGenerator.name, tracer);
                } catch (Throwable e) {
                    consoleError.println("ballerina: error getting tracer for "
                            + tracerGenerator.name + ". " + e.getMessage());
                }
            }
            tracerStore.put(serviceName, tracerMap);
            return tracerMap;
        }
    }

    /**
     * Holds the tracerExt and properties, and generates a tracer upon request.
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
}
