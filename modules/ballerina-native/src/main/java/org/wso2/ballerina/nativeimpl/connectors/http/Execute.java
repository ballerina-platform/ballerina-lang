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

package org.wso2.ballerina.nativeimpl.connectors.http;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Locale;

/**
 * {@code Execute} action can be used to invoke execute a http call with any httpVerb.
 *
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "execute",
        connectorName = ClientConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "con",
                        type = TypeEnum.CONNECTOR),
                @Argument(name = "httpVerb", type = TypeEnum.STRING),
                @Argument(name = "path", type = TypeEnum.STRING),
                @Argument(name = "message", type = TypeEnum.MESSAGE)
        },
        returnType = {@ReturnType(type = TypeEnum.MESSAGE)})
@Component(
        name = "action.net.http.execute",
        immediate = true,
        service = AbstractNativeAction.class)
public class Execute extends AbstractHTTPAction {

    private static final Logger logger = LoggerFactory.getLogger(Execute.class);

    @Override
    public BValue execute(Context context) {

        logger.debug("Executing Native Action : Execute");
        try {
            // Extract Argument values
            BConnector bConnector = (BConnector) getArgument(context, 0);
            String httpVerb = getArgument(context, 1).stringValue();
            String path = getArgument(context, 2).stringValue();
            BMessage bMessage = (BMessage) getArgument(context, 3);

            Connector connector = bConnector.value();
            if (!(connector instanceof ClientConnector)) {
                throw new BallerinaException("Need to use a ClientConnector as the first argument", context);
            }

            // Prepare the message
            CarbonMessage cMsg = bMessage.value();
            prepareRequest(connector, path, cMsg);

            // If the verb is not specified, use the verb in incoming message
            if (httpVerb == null || "".equals(httpVerb)) { 
                httpVerb = (String) cMsg.getProperty(Constants.HTTP_METHOD);
            }
            cMsg.setProperty(Constants.HTTP_METHOD,
                             httpVerb.trim().toUpperCase(Locale.getDefault()));

            // Execute the operation
            return executeAction(context, cMsg);
        } catch (Throwable t) {
            throw new BallerinaException("Failed to invoke 'execute' action in " + ClientConnector.CONNECTOR_NAME
                                         + ". " + t.getMessage(), context);
        }
    }
}
