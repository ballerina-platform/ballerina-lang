/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.actions.httpserverconnector;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpConstants;

/**
 * {@code Forward} is the forward action implementation of the HTTP Server Connector.
 *
 * @since 0.96
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "forward",
        connectorName = HttpConstants.SERVER_CONNECTOR,
        args = {@Argument(name = "c", type = TypeKind.CONNECTOR),
                @Argument(name = "res", type = TypeKind.STRUCT, structType = "Response",
                        structPackage = "ballerina.net.http")},
        returnType = @ReturnType(type = TypeKind.STRUCT, structType = "HttpConnectorError",
                structPackage = "ballerina.net.http")
)
public class Forward extends AbstractConnectorAction {

    @Override
    public void execute(Context context) {
        super.execute(context);
    }
}
