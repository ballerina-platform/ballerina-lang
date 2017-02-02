/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.nativeimpl.connectors.data.cassandra.client;

import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.ballerina.core.nativeimpl.connectors.data.cassandra.CassandraConnector;

/**
 * The Find with query action for MongoDB.
 */
@BallerinaAction(
        packageName = "ballerina.data.cassandra",
        actionName = "execute",
        connectorName = CassandraConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "connector",
                        type = TypeEnum.CONNECTOR),
                @Argument(name = "query", type = TypeEnum.STRING)
        },
        returnType = {TypeEnum.DATATABLE })
@Component(
        name = "action.data.cassandra.execute",
        immediate = true,
        service = AbstractNativeAction.class)
public class Execute extends AbstractNativeAction {

    @Override
    public BValue execute(Context context) {
        Connector connector = ((BConnector) getArgument(context, 0)).value();
        String query = ((BString) getArgument(context, 1)).stringValue();
        if (!(connector instanceof CassandraConnector)) {
            throw new BallerinaException("Need to use a CassandraConnector as the first argument", context);
        }
        CassandraConnector cc = (CassandraConnector) connector;
        BValue result = cc.execute(query, null);
        context.getControlStack().setReturnValue(0, result);
        return null;
    }

}
