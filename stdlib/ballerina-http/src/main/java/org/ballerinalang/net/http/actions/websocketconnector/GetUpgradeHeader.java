/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.actions.websocketconnector;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.WebSocketConstants;

import java.util.Locale;
import java.util.Map;

/**
 * {@code Get} is the GET action implementation of the HTTP Connector.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "getUpgradeHeader",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = WebSocketConstants.WEBSOCKET_CONNECTOR,
                structPackage = "ballerina.http"),
        args = {
                @Argument(name = "wsConnector", type = TypeKind.STRUCT),
                @Argument(name = "key", type = TypeKind.STRING)
        },
        returnType = {
                @ReturnType(type = TypeKind.STRING)
        }
)
public class GetUpgradeHeader extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BStruct wsConnection = (BStruct) context.getRefArgument(0);
        String key = context.getStringArgument(0).toLowerCase(Locale.ENGLISH);
        Map<String, String> upgradeHeaders =
                (Map<String, String>) wsConnection.getNativeData(WebSocketConstants.NATIVE_DATA_UPGRADE_HEADERS);
        context.setReturnValues(new BString(upgradeHeaders.get(key)));
    }
}
