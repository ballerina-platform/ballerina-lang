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

import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.observability.metrics.Tag;
import org.ballerinalang.jvm.observability.metrics.Tags;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extension of ObserverContext for WebSockets.
 *
 * @since 1.1.0
 */

public class WebSocketObserverContext extends ObserverContext {

    private String connectionId = WebSocketObservabilityConstants.UNKNOWN;
    private String servicePathOrClientUrl = WebSocketObservabilityConstants.UNKNOWN;

    WebSocketObserverContext() {
        setConnectorName(ObservabilityConstants.SERVER_CONNECTOR_WEBSOCKET);
    }

    public WebSocketObserverContext(WebSocketConnectionInfo connectionInfo) {
        this();
        this.connectionId = WebSocketObservabilityUtil.getConnectionId(connectionInfo);
        this.servicePathOrClientUrl = WebSocketObservabilityUtil.getServicePathOrClientUrl(connectionInfo);
        setTags(connectionInfo);
    }

    /**
     * Sets the basic tags for the current WebSocket Observer context.
     * (Connection ID, whether in client or server context, and the service path/client URL).
     *
     * @param connectionInfo  information regarding connection.
     */
    public void setTags(WebSocketConnectionInfo connectionInfo) {
        addTag(WebSocketObservabilityConstants.TAG_CONTEXT,
                    WebSocketObservabilityUtil.getClientOrServerContext(connectionInfo));
        addTag(WebSocketObservabilityConstants.TAG_SERVICE, servicePathOrClientUrl);
    }

    String getConnectionId() {
        return connectionId;
    }

    String getServicePathOrClientUrl() {
        return servicePathOrClientUrl;
    }

    Set<Tag> getAllTags() {
        Map<String, String> tags = getTags();
        Set<Tag> allTags = new HashSet<>(tags.size());
        Tags.tags(allTags, tags);
        return allTags;
    }
}
