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

import java.util.HashMap;
import java.util.Map;

/**
 * Class that holds the list of tracers.
 */
class TracersStore {

    private OpenTracingConfig openTracingConfig;

    TracersStore(OpenTracingConfig openTracingConfig) {
        this.openTracingConfig = openTracingConfig;
    }

    /**
     * Return trace implementations for a specific service
     *
     * @param serviceName name of service of whose trace implementations are needed
     * @return trace implementations i.e: zipkin, jaeger
     */
    Map<String, Tracer> getTracers(String serviceName) {
        Map<String, Tracer> tracerMap = new HashMap<>();
        for (TracerConfig tracerConfig : openTracingConfig.getTracers()) {
            if (tracerConfig.isEnabled()) {
                try {
                    Tracer tracer = OpenTracerFactory.getInstance()
                            .getTracer(tracerConfig, serviceName);
                    tracerMap.put(tracerConfig.getName(), tracer);
                } catch (Throwable ignored) {
                    //Tracers will get added only of there's no errors.
                    //If tracers contains errors, empty map will return.
                }
            }
        }
        return tracerMap;
    }
}
