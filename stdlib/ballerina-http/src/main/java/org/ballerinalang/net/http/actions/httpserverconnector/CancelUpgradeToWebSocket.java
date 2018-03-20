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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.WebSocketConstants;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;

/**
 * {@code Get} is the GET action implementation of the HTTP Connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "cancelUpgradeToWebSocket",
        connectorName = HttpConstants.SERVICE_ENDPOINT,
        args = {
                @Argument(name = "c", type = TypeKind.CONNECTOR),
                @Argument(name = "status", type = TypeKind.INT),
                @Argument(name = "reason", type = TypeKind.STRING)
        },
        connectorArgs = {
                @Argument(name = "attributes", type = TypeKind.MAP)
        }
)
public class CancelUpgradeToWebSocket extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BConnector serverConnector = (BConnector) context.getRefArgument(0);
        int statusCode = (int) context.getIntArgument(0);
        String reason = context.getStringArgument(0);
        WebSocketInitMessage initMessage =
                (WebSocketInitMessage) serverConnector.getNativeData(WebSocketConstants.WEBSOCKET_MESSAGE);
        initMessage.cancelHandShake(statusCode, reason);
        context.setReturnValues();
    }
}
