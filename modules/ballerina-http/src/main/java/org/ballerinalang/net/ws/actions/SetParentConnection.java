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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.net.ws.Constants;

/**
 * Set a parent connection for the client connector.
 *
 * @since 0.94
 */
@BallerinaAction(
        packageName = "ballerina.net.ws",
        actionName = "setParentConnection",
        connectorName = Constants.CONNECTOR_NAME,
        args = {
                @Argument(name = "c", type = TypeEnum.CONNECTOR),
                @Argument(name = "conn", type = TypeEnum.STRUCT),
        }
)
public class SetParentConnection extends AbstractNativeAction {
    @Override
    public BValue execute(Context context) {
        BConnector bconnector = (BConnector) getRefArgument(context, 0);
        BStruct parentConnection = (BStruct) getRefArgument(context, 1);
        bconnector.setNativeData(Constants.NATIVE_DATA_PARENT_CONNECTION, parentConnection);
        return null;
    }
}
