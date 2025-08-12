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

import org.ballerinalang.jvm.observability.BallerinaObserver;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.observability.OtelTracingUtils;
import org.ballerinalang.jvm.observability.TracingUtils;

import static org.ballerinalang.jvm.observability.tracer.TraceConstants.OTEL_TRACING_PROTOCOL;

/**
 * Observe the runtime and start/stop tracing.
 */
public class BallerinaTracingObserver implements BallerinaObserver {

    @Override
    public void startServerObservation(ObserverContext observerContext) {
        if (ObserveUtils.getTracingProtocol().equals(OTEL_TRACING_PROTOCOL)) {
            OtelTracingUtils.startObservation(observerContext, false);

        } else {
            TracingUtils.startObservation(observerContext, false);
        }
    }

    @Override
    public void startClientObservation(ObserverContext observerContext) {
        if (ObserveUtils.getTracingProtocol().equals(OTEL_TRACING_PROTOCOL)) {
            OtelTracingUtils.startObservation(observerContext, true);

        } else {
            TracingUtils.startObservation(observerContext, true);
        }
    }

    @Override
    public void stopServerObservation(ObserverContext observerContext) {
        if (ObserveUtils.getTracingProtocol().equals(OTEL_TRACING_PROTOCOL)) {
            OtelTracingUtils.stopObservation(observerContext);
        } else {
            TracingUtils.stopObservation(observerContext);
        }
    }

    @Override
    public void stopClientObservation(ObserverContext observerContext) {
        if (ObserveUtils.getTracingProtocol().equals(OTEL_TRACING_PROTOCOL)) {
            OtelTracingUtils.stopObservation(observerContext);
        } else {
            TracingUtils.stopObservation(observerContext);
        }
    }
}
