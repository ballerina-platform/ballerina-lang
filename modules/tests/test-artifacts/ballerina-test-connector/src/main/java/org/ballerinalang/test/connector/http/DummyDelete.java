/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.connector.http;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ballerinalang.core.interpreter.Context;
import org.ballerinalang.core.model.Connector;
import org.ballerinalang.core.model.types.TypeEnum;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.wso2.carbon.messaging.CarbonMessage;

import static org.ballerinalang.natives.connectors.http.Constants.HTTP_METHOD;
import static org.ballerinalang.natives.connectors.http.Constants.HTTP_METHOD_DELETE;

/**
 * {@code DummyDelete} is the DELETE action implementation of the HTTP Connector
 *
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "delete",
        connectorName = DummyHTTPConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "connector", type = TypeEnum.CONNECTOR),
                @Argument(name = "path", type = TypeEnum.STRING),
                @Argument(name = "message", type = TypeEnum.MESSAGE)
        },
        returnType = {TypeEnum.MESSAGE})
@Component(
        name = "action.net.http.dummy_delete",
        immediate = true,
        service = AbstractNativeAction.class)
public class DummyDelete extends AbstractHTTPAction {

    private static final Logger logger = LoggerFactory.getLogger(DummyDelete.class);

    @Override
    public BValue execute(Context context) {

        logger.debug("Executing Native Action : DummyDelete");

        // Extract Argument values
        BConnector connectorValue = (BConnector) getArgument(context, 0);
        String path = getArgument(context, 1).stringValue();
        BMessage messageValue = (BMessage) getArgument(context, 2);

        Connector connector = connectorValue.value();
        if (!(connector instanceof DummyHTTPConnector)) {
            logger.error("Need to use a DummyHTTPConnector as the first argument");
            return null;
        }
        // Prepare the message
        CarbonMessage cMsg = messageValue.value();
        prepareRequest(connector, path, cMsg);
        cMsg.setProperty(HTTP_METHOD, HTTP_METHOD_DELETE);

        // DummyExecute the operation
        messageValue.setBuiltPayload(new BString("DummyDelete method invoked."));
        messageValue.setAlreadyRead(true);

        return messageValue;
    }
}
