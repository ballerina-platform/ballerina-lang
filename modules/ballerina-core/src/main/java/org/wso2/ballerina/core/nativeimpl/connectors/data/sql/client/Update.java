/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.nativeimpl.connectors.data.sql.client;

import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.ballerina.core.nativeimpl.connectors.data.sql.SQLConnector;

/**
 * {@code Update} is the Update action implementation of the SQL Connector.
 */
@BallerinaAction(
        packageName = "ballerina.data.sql",
        actionName = "update",
        connectorName = SQLConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "connector",
                          type = TypeEnum.CONNECTOR),
                @Argument(name = "query",
                          type = TypeEnum.STRING)/*, //TODO:Add Parameter struct
                @Argument(name = "optionalProperties",
                          type = TypeEnum.MAP)*/
        },
        returnType = { TypeEnum.INT })
@Component(
        name = "action.data.sql.update",
        immediate = true,
        service = AbstractNativeAction.class)
public class Update extends AbstractSQLAction {
    @Override
    public BValue execute(Context context) {
        BConnector bConnector = (BConnector) getArgument(context, 0);
        String query = getArgument(context, 1).stringValue();

        Connector connector = bConnector.value();
        if (!(connector instanceof SQLConnector)) {
            throw new BallerinaException("Need to use a SQL Connector as the first argument", context);
        }
        executeUpdate(context, (SQLConnector) connector, query);
        return null;
    }
}
