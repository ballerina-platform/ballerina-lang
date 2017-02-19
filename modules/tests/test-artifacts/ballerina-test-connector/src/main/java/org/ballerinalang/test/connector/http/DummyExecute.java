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

import java.util.Locale;

import static org.ballerinalang.natives.connectors.http.Constants.HTTP_METHOD;

/**
 * {@code DummyExecute} action can be used to invoke execute a http call with any httpVerb
 *
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "execute",
        connectorName = DummyHTTPConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "connector",
                        type = TypeEnum.CONNECTOR),
                @Argument(name = "httpVerb", type = TypeEnum.STRING),
                @Argument(name = "path", type = TypeEnum.STRING),
                @Argument(name = "message", type = TypeEnum.MESSAGE)
        },
        returnType = {TypeEnum.MESSAGE})
@Component(
        name = "action.net.http.dummy_execute",
        immediate = true,
        service = AbstractNativeAction.class)
public class DummyExecute extends AbstractHTTPAction {

    private static final Logger logger = LoggerFactory.getLogger(DummyExecute.class);

    @Override
    public BValue execute(Context context) {

        logger.debug("Executing Native Action : DummyExecute");

        // Extract Argument values
        BConnector connectorValue = (BConnector) getArgument(context, 0);
        String httpVerb = getArgument(context, 1).stringValue();
        String path = getArgument(context, 2).stringValue();
        BMessage messageValue = (BMessage) getArgument(context, 3);

        Connector connector = connectorValue.value();
        if (!(connector instanceof DummyHTTPConnector)) {
            logger.error("Need to use a DummyHTTPConnector as the first argument");
            return null;
        }

        // Prepare the message
        CarbonMessage cMsg = messageValue.value();
        prepareRequest(connector, path, cMsg);

        if (httpVerb == null || "".equals(httpVerb)) { // If the verb is not specified, use the verb in incoming message
            httpVerb = (String) cMsg.getProperty(HTTP_METHOD);
        }
        cMsg.setProperty(HTTP_METHOD, httpVerb.trim().toUpperCase(Locale.getDefault()));

        // DummyExecute the operation
        messageValue.setBuiltPayload(new BString("Default method invoked."));
        messageValue.setAlreadyRead(true);

        return messageValue;
    }
}
