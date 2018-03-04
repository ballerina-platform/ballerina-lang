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

package org.ballerinalang.observe.trace;

import io.opentracing.Tracer;
import org.ballerinalang.observe.trace.config.OpenTracingConfig;
import org.ballerinalang.observe.trace.config.TracerConfig;
import org.ballerinalang.observe.trace.exception.InvalidConfigurationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that holds the list of tracers.
 */
public class TracersStore {

    private final Map<String, Map<String, Tracer>> tracerForServiceMap; // serviceName -> map(tracerName, tracer)
    private OpenTracingConfig openTracingConfig;

    public TracersStore(OpenTracingConfig openTracingConfig) {
        this.tracerForServiceMap = new HashMap<>();
        this.openTracingConfig = openTracingConfig;
    }

    public Map<String, Map<String, Tracer>> getTracerForServiceMap() {
        return tracerForServiceMap;
    }

    /**
     * Return trace implementations for a specific service
     *
     * @param serviceName name of service of whose trace implementations are needed
     * @return trace implementations i.e: zipkin, jaeger
     */
    public Map<String, Tracer> getTracers(String serviceName) {
        Map<String, Tracer> stringTracerMap;
        stringTracerMap = tracerForServiceMap.get(serviceName);
        if (stringTracerMap == null) {
            stringTracerMap = new HashMap<>();
            for (TracerConfig tracerConfig : openTracingConfig.getTracers()) {
                if (tracerConfig.isEnabled()) {
                    try {
                        Tracer tracer = OpenTracerFactory.getInstance().getTracer(tracerConfig, serviceName);
                        stringTracerMap.put(tracerConfig.getName(), tracer);
                    } catch (IllegalAccessException | InstantiationException | ClassNotFoundException |
                            InvalidConfigurationException ex) {
                        // TODO: 2/13/18 Maybe throw RuntimeException
                    }
                } else {
                }
            }
            tracerForServiceMap.put(serviceName, stringTracerMap);
        }
        return stringTracerMap;
    }
}
