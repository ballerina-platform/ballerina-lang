/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.ws.actions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.net.ws.Constants;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.transport.http.netty.contract.websocket.WSClientConnectorConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Add custom headers to the client connector.
 *
 * @since 0.94
 */
@BallerinaAction(
        packageName = "ballerina.net.ws",
        actionName = "addCustomHeaders",
        connectorName = Constants.CONNECTOR_NAME,
        args = {
                @Argument(name = "c", type = TypeEnum.CONNECTOR),
                @Argument(name = "headers", type = TypeEnum.MAP),
        }
)
public class AddCustomHeaders extends AbstractNativeAction {
    @Override
    public BValue execute(Context context) {
        BConnector bconnector = (BConnector) getRefArgument(context, 0);
        BMap<BString, BString> bCustomHeaders = (BMap<BString, BString>) getRefArgument(context, 1);
        Map<String, String> customheaders = new HashMap<>();
        Set<BString> bKeySet = bCustomHeaders.keySet();

        bKeySet.forEach(
                bKey -> customheaders.put(bKey.stringValue(), bCustomHeaders.get(bKey).stringValue())
        );

        WSClientConnectorConfig senderConfiguration =
                (WSClientConnectorConfig) bconnector.getnativeData(Constants.NATIVE_DATA_SENDER_CONFIG);
        senderConfiguration.addHeaders(customheaders);
        return null;
    }
}
