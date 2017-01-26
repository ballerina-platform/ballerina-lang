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
package org.wso2.ballerina.core.nativeimpl.connectors.data.mongodb.client;

import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.ballerina.core.nativeimpl.connectors.data.mongodb.MongoDBConnector;

/**
 * The FindOne with query action for MongoDB.
 */
@BallerinaAction(
        packageName = "ballerina.data.mongodb",
        actionName = "findOne",
        connectorName = MongoDBConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "connector",
                        type = TypeEnum.CONNECTOR),
                @Argument(name = "collection", type = TypeEnum.STRING),
                @Argument(name = "query", type = TypeEnum.JSON)
        },
        returnType = {TypeEnum.JSON})
@Component(
        name = "action.data.mongodb.findOneWithQuery",
        immediate = true,
        service = AbstractNativeAction.class)
public class FindOneWithQuery extends AbstractNativeAction {

    @Override
    public BValue execute(Context context) {
        Connector connector = ((BConnector) getArgument(context, 0)).value();
        String collection = ((BString) getArgument(context, 1)).stringValue();
        BJSON query = ((BJSON) getArgument(context, 2));
        if (!(connector instanceof MongoDBConnector)) {
            throw new BallerinaException("Need to use a MongoDBConnector as the first argument", context);
        }
        MongoDBConnector mdb = (MongoDBConnector) connector;
        BValue result = mdb.findOne(collection, query);
        context.getControlStack().setReturnValue(0, result);
        return null;
    }

}
