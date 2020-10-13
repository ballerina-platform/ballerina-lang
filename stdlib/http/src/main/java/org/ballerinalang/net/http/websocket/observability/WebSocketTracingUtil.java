/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.websocket.observability;

import io.ballerina.runtime.observability.ObserveUtils;
import io.ballerina.runtime.observability.ObserverContext;
import io.ballerina.runtime.scheduling.Strand;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;

import java.util.Optional;

/**
 * Providing tracing functionality to WebSockets.
 *
 * @since 1.1.0
 */

public class WebSocketTracingUtil {

    /**
     * Obtains the current observer context of new resource that was invoked and sets the necessary tags to it.
     *
     * @param strand         Strand of the new resource invoked
     * @param connectionInfo information regarding connection.
     */
    static void traceResourceInvocation(Strand strand, WebSocketConnectionInfo connectionInfo) {
        if (!ObserveUtils.isTracingEnabled()) {
            return;
        }
        ObserverContext observerContext;
        Optional<ObserverContext> observerContextOptional = ObserveUtils.getObserverContextOfCurrentFrame(strand);
        if (observerContextOptional.isPresent()) {
            observerContext = observerContextOptional.get();
        } else {
            observerContext = new ObserverContext();
            ObserveUtils.setObserverContextToCurrentFrame(strand, observerContext);
        }
        setTags(observerContext, connectionInfo);
    }

    private static void setTags(ObserverContext observerContext, WebSocketConnectionInfo connectionInfo) {
        observerContext.addTag(WebSocketObservabilityConstants.TAG_CONTEXT,
               WebSocketObservabilityUtil.getClientOrServerContext(connectionInfo));
        observerContext.addTag(WebSocketObservabilityConstants.TAG_SERVICE,
                               WebSocketObservabilityUtil.getServicePathOrClientUrl(connectionInfo));
    }


    private WebSocketTracingUtil() {

    }
}
