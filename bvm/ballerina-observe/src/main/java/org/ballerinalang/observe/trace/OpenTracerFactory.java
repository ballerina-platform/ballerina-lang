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
import org.ballerinalang.observe.trace.config.TracerConfig;
import org.ballerinalang.observe.trace.exception.InvalidConfigurationException;

/**
 * Factory class to generate Tracer objects.
 */
public class OpenTracerFactory {

    private static OpenTracerFactory instance = new OpenTracerFactory();

    public static OpenTracerFactory getInstance() {
        return instance;
    }

    public Tracer getTracer(TracerConfig tracerConfig, String serviceName) throws IllegalAccessException,
            InstantiationException, ClassNotFoundException, InvalidConfigurationException {

        Class<?> openTracerClass = Class.forName(tracerConfig.getClassName()).asSubclass(OpenTracer.class);
        OpenTracer openTracer = (OpenTracer) openTracerClass.newInstance();

        return openTracer.getTracer(tracerConfig.getName(), tracerConfig.getConfiguration(), serviceName);
    }
}
