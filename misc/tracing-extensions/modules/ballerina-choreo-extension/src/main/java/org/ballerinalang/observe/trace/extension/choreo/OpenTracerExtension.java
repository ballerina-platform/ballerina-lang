/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.observe.trace.extension.choreo;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.observability.tracer.OpenTracer;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.samplers.RateLimitingSampler;
import io.jaegertracing.spi.Reporter;
import io.opentracing.Tracer;
import org.ballerinalang.observe.trace.extension.choreo.client.ChoreoClient;
import org.ballerinalang.observe.trace.extension.choreo.client.ChoreoClientHolder;
import org.ballerinalang.observe.trace.extension.choreo.client.error.ChoreoClientException;

import java.util.Objects;

import static org.ballerinalang.observe.trace.extension.choreo.Constants.CHOREO_EXTENSION_NAME;

/**
 * This is the open tracing extension class for {@link OpenTracer}.
 *
 * @since 2.0.0
 */
public class OpenTracerExtension implements OpenTracer {
    private static volatile Reporter reporterInstance;
    private ChoreoClient choreoClient;

    @Override
    public void init() {
        try {
            choreoClient = ChoreoClientHolder.getChoreoClient();
        } catch (ChoreoClientException e) {
            throw ErrorCreator.createError(
                    StringUtils
                            .fromString("Choreo client is not initialized. Please check Ballerina configurations."),
                    StringUtils.fromString(e.getMessage()));
        }

        if (Objects.isNull(choreoClient)) {
            throw ErrorCreator.createError(StringUtils.fromString(
                    "Choreo client is not initialized. Please check Ballerina configurations."));
        }
    }

    @Override
    public Tracer getTracer(String serviceName) {
        if (Objects.isNull(choreoClient)) {
            throw ErrorCreator.createError(
                    StringUtils
                            .fromString("Choreo client is not initialized. Please check Ballerina configurations."));
        }

        if (reporterInstance == null) { // Singleton instance is used since getTracer can get called multiple times
            synchronized (this) {
                if (reporterInstance == null) {
                    reporterInstance = new ChoreoJaegerReporter(2000);
                }
            }
        }
        return new JaegerTracer.Builder(serviceName)
                .withSampler(new RateLimitingSampler(2))
                .withReporter(reporterInstance)
                .build();
    }

    @Override
    public String getName() {
        return CHOREO_EXTENSION_NAME;
    }

}
