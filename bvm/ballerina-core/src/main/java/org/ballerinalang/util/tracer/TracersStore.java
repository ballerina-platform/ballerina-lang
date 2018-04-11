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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TABLE_TRACING;
import static org.ballerinalang.util.tracer.TraceConstants.ENABLED_CONFIG;
import static org.ballerinalang.util.tracer.TraceConstants.JAEGER;
import static org.ballerinalang.util.tracer.TraceConstants.TRACER_NAME_CONFIG;

/**
 * Class that creates the list of tracers for a given service.
 */
class TracersStore {

    private List<TracerGenerator> tracers;
    private Map<String, Map<String, Tracer>> tracerStore;

    private static TracersStore instance = new TracersStore();

    public static TracersStore getInstance() {
        return instance;
    }

    private TracersStore() {
        Map<String, String> tracingConfigs = ConfigRegistry.getInstance().getConfigTable(CONFIG_TABLE_TRACING);
        if (Boolean.parseBoolean(tracingConfigs.get(ENABLED_CONFIG))) {

            this.tracers = new ArrayList<>();
            this.tracerStore = new HashMap<>();

            ServiceLoader<OpenTracer> openTracers = ServiceLoader.load(OpenTracer.class);
            HashMap<String, OpenTracer> tracerMap = new HashMap<>();
            openTracers.forEach(t -> tracerMap.put(t.getName(), t));

            String tracerName = tracingConfigs.getOrDefault(TRACER_NAME_CONFIG, JAEGER);

            OpenTracer tracer = tracerMap.get(tracerName);
            if (tracer != null) {
                tracer.init(tracingConfigs);
                this.tracers.add(new TracerGenerator(tracer.getName(), tracer, tracingConfigs));
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
                } catch (Throwable ignored) {
                    //Tracers will get added only of there's no errors.
                    //If tracers contains errors, tracer will be ignored.
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
        Map<String, String> properties;

        TracerGenerator(String name, OpenTracer tracer, Map<String, String> properties) {
            this.name = name;
            this.tracer = tracer;
            this.properties = properties;
        }

        Tracer generate(String serviceName) throws InvalidConfigurationException {
            return tracer.getTracer(name, serviceName);
        }
    }
}
