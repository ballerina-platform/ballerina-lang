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
package org.ballerinalang.nativeimpl.connectors.data.sql.client;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.Connector;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.connectors.data.sql.SQLConnector;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.osgi.service.component.annotations.Component;

/**
 * {@code UpdateWithGeneratedKeyColumns} is the updateWithGeneratedKeys action implementation of the SQL Connector.
 *
 * @since 0.8.0
 */
@BallerinaAction(
        packageName = "ballerina.data.sql",
        actionName = "updateWithGeneratedKeys",
        connectorName = SQLConnector.CONNECTOR_NAME,
        args = {@Argument(name = "c", type = TypeEnum.CONNECTOR),
                @Argument(name = "query", type = TypeEnum.STRING),
                @Argument(name = "keyColumns", type = TypeEnum.ARRAY, elementType = TypeEnum.STRING),
                @Argument(name = "parameters", type = TypeEnum.ARRAY, elementType = TypeEnum.STRUCT,
                          structType = "Parameter")},
        returnType = { @ReturnType(type = TypeEnum.INT), @ReturnType(type = TypeEnum.STRING) })
@Component(
        name = "action.data.sql.UpdateWithGeneratedKeyColumns",
        immediate = true,
        service = AbstractNativeAction.class)
public class UpdateWithGeneratedKeyColumns extends AbstractSQLAction {

    @Override
    public BValue execute(Context context) {
        BConnector bConnector = (BConnector) getArgument(context, 0);
        String query = getArgument(context, 1).stringValue();
        BArray<BString> keyColumns = ((BArray<BString>) getArgument(context, 2));
        BArray parameters = (BArray) getArgument(context, 3);
        Connector connector = bConnector.value();
        executeUpdateWithKeys(context, (SQLConnector) connector, query, keyColumns, parameters);
        return null;
    }
}
