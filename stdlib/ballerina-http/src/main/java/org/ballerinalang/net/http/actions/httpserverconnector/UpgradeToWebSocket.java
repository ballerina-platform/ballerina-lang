/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.ballerinalang.net.http.actions.httpserverconnector;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketService;
import org.ballerinalang.net.http.WebSocketUtil;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;

import java.util.Set;

/**
 * {@code Get} is the GET action implementation of the HTTP Connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "upgradeToWebSocket",
        connectorName = HttpConstants.SERVICE_ENDPOINT,
        args = {
                @Argument(name = "c", type = TypeKind.CONNECTOR),
                @Argument(name = "headers", type = TypeKind.MAP)
        },
        connectorArgs = {
                @Argument(name = "attributes", type = TypeKind.MAP)
        }
)
public class UpgradeToWebSocket extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BConnector serverConnector = (BConnector) context.getRefArgument(0);
        WebSocketService webSocketService = (WebSocketService) serverConnector.getNativeData(
                WebSocketConstants.WEBSOCKET_SERVICE);
        BMap<String, BString> queryParams =
                (BMap<String, BString>) serverConnector.getNativeData(WebSocketConstants.NATIVE_DATA_QUERY_PARAMS);
        BMap<String, BString> headers = (BMap<String, BString>) context.getRefArgument(1);
        WebSocketInitMessage initMessage =
                (WebSocketInitMessage) serverConnector.getNativeData(WebSocketConstants.WEBSOCKET_MESSAGE);
        DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            httpHeaders.add(key, headers.get(key));
        }

        WebSocketUtil.handleHandshake(initMessage, webSocketService, queryParams, httpHeaders);

        context.setReturnValues();
    }
}
