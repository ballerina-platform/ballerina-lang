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
package org.ballerinalang.net.http.actions;


import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.net.http.Constants;

/**
 * {@code Init} is the Init action implementation of the SQL Connector.
 *
 * @since 0.8.5
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "<init>",
        connectorName = Constants.CONNECTOR_NAME,
        args = {@Argument(name = "c", type = TypeKind.CONNECTOR)
        },
        connectorArgs = {
                @Argument(name = "serviceUri", type = TypeKind.STRING),
                @Argument(name = "options", type = TypeKind.STRUCT, structType = "Options",
                          structPackage = "ballerina.net.http")
        })
public class Init extends AbstractHTTPAction {

    @Override
    public ConnectorFuture execute(Context context) {
        BConnector connector = (BConnector) getRefArgument(context, 0);
        String url = connector.getStringField(0);
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
            connector.setStringField(0, url);
        }
        ClientConnectorFuture ballerinaFuture = new ClientConnectorFuture();
        ballerinaFuture.notifySuccess();
        return ballerinaFuture;
    }
}
