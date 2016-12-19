/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.core.nativeimpl.connectors.http;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.ConnectorValue;
import org.wso2.ballerina.core.model.values.MessageValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Locale;

/**
 * {@code Execute} action can be used to invoke execute a http call with any httpVerb
 *
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "execute",
        connectorName = HTTPConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "connector",
                        type = TypeEnum.CONNECTOR),
                @Argument(name = "httpVerb", type = TypeEnum.STRING),
                @Argument(name = "path", type = TypeEnum.STRING),
                @Argument(name = "message", type = TypeEnum.MESSAGE)
        },
        returnType = {TypeEnum.MESSAGE})
@Component(
        name = "action.net.http.execute",
        immediate = true,
        service = AbstractNativeAction.class)
public class Execute extends AbstractHTTPAction {

    private static final Logger logger = LoggerFactory.getLogger(Execute.class);

    @Override
    public BValueRef execute(Context context) {

        logger.debug("Executing Native Action : Execute");

        // Extract Argument values
        ConnectorValue connectorValue = (ConnectorValue) getArgument(context, 0).getBValue();
        String httpVerb = getArgument(context, 1).getString();
        String path = getArgument(context, 2).getString();
        MessageValue messageValue = (MessageValue) getArgument(context, 3).getBValue();
        CarbonMessage cMsg = messageValue.getValue();

        if (httpVerb == null || "".equals(httpVerb)) { // If the verb is not specified, use the verb in incoming message
            httpVerb = (String) cMsg.getProperty(org.wso2.ballerina.core.runtime.net.http.Constants.HTTP_METHOD);
        }

        Connector connector = connectorValue.getValue();
        if (!(connector instanceof HTTPConnector)) {
            logger.error("Need to use a HTTPConnector as the first argument");
            return null;
        }
        // Prepare the message
        prepareRequest(connector, path, cMsg);
        cMsg.setProperty(org.wso2.ballerina.core.runtime.net.http.Constants.HTTP_METHOD,
                         httpVerb.trim().toUpperCase(Locale.getDefault()));

        // Execute the operation
        return executeAction(context, cMsg);
    }
}
