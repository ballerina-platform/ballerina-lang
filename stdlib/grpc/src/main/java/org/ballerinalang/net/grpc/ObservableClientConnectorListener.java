/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.net.grpc;

import org.ballerinalang.bre.Context;
import org.ballerinalang.util.observability.ObservabilityConstants;
import org.ballerinalang.util.observability.ObserveUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.wso2.transport.http.netty.contract.ClientConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.Optional;

import static org.ballerinalang.net.http.HttpConstants.HTTP_STATUS_CODE;

/**
 * Observable Client Connector Listener.
 *
 * @since 0.995.0
 */
public class ObservableClientConnectorListener extends ClientConnectorListener {
    private final Context context;

    ObservableClientConnectorListener(ClientCall.ClientStreamListener streamListener, Context context) {
        super(streamListener);
        this.context = context;
    }

    @Override
    public void onMessage(HttpCarbonMessage httpCarbonMessage) {
        super.onMessage(httpCarbonMessage);
        Integer statusCode = (Integer) httpCarbonMessage.getProperty(HTTP_STATUS_CODE);
        addHttpStatusCode(statusCode != null ? statusCode : 0);
    }

    @Override
    public void onError(Throwable throwable) {
        super.onError(throwable);
        if (throwable instanceof ClientConnectorException) {
            ClientConnectorException clientConnectorException = (ClientConnectorException) throwable;
            addHttpStatusCode(clientConnectorException.getHttpStatusCode());
            Optional<ObserverContext> observerContext = ObserveUtils.getObserverContextOfCurrentFrame(context);
            observerContext.ifPresent(ctx -> {
                ctx.addProperty(ObservabilityConstants.PROPERTY_ERROR, Boolean.TRUE);
                ctx.addProperty(ObservabilityConstants.PROPERTY_ERROR_MESSAGE, throwable.getMessage());
            });

        }
    }

    private void addHttpStatusCode(int statusCode) {
        Optional<ObserverContext> observerContext = ObserveUtils.getObserverContextOfCurrentFrame(context);
        observerContext.ifPresent(ctx -> ctx.addTag(ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE,
                String.valueOf(statusCode)));
    }
}
