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

package org.ballerinalang.nativeimpl.connectors.http;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.Connector;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BException;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.natives.connectors.BalConnectorCallback;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;

/**
 * {@code Head} is the HEAD action implementation of the HTTP Connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "head",
        connectorName = ClientConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "c", type = TypeEnum.CONNECTOR),
                @Argument(name = "path", type = TypeEnum.STRING),
                @Argument(name = "m", type = TypeEnum.MESSAGE)
        },
        returnType = {@ReturnType(type = TypeEnum.MESSAGE)})
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "The HEAD action implementation of the HTTP Connector.") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "c",
        value = "A connector object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "path",
        value = "Resource path ") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m",
        value = "A message object") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "message",
        value = "The response message object") })
@Component(
        name = "action.net.http.head",
        immediate = true,
        service = AbstractNativeAction.class)
public class Head extends AbstractHTTPAction {

    private static final Logger logger = LoggerFactory.getLogger(Head.class);

    @Override
    public BValue execute(Context context) {

        logger.debug("Executing Native Action : head");

        try {
            // Execute the operation
            return executeAction(context, createCarbonMsg(context));
        } catch (Throwable t) {
            throw new BallerinaException("Failed to invoke 'head' action in " + ClientConnector.CONNECTOR_NAME
                    + ". " + t.getMessage(), context);
        }
    }

    @Override
    public void execute(Context context, BalConnectorCallback callback) {

        if (logger.isDebugEnabled()) {
            logger.debug("Executing Native Action (non-blocking): {}", this.getName());
        }
        try {
            // Execute the operation
            executeNonBlockingAction(context, createCarbonMsg(context), callback);
        } catch (ClientConnectorException | RuntimeException e) {
            String msg = "Failed to invoke 'head' action in " + ClientConnector.CONNECTOR_NAME
                    + ". " + e.getMessage();
            BException exception = new BException(msg, Constants.HTTP_CLIENT_EXCEPTION_CATEGORY);
            context.getExecutor().handleBException(exception);
        } catch (Throwable t) {
            throw new BallerinaException("Failed to invoke 'head' action in " + ClientConnector.CONNECTOR_NAME
                    + ". " + t.getMessage(), context);
        }
    }

    private CarbonMessage createCarbonMsg(Context context) {
        // Extract Argument values
        BConnector bConnector = (BConnector) getArgument(context, 0);
        String path = getArgument(context, 1).stringValue();
        BMessage bMessage = (BMessage) getArgument(context, 2);

        Connector connector = bConnector.value();
        if (!(connector instanceof ClientConnector)) {
            throw new BallerinaException("Need to use a HTTPConnector as the first argument", context);
        }

        // Prepare the message
        CarbonMessage cMsg = bMessage.value();
        prepareRequest(connector, path, cMsg);
        cMsg.setProperty(Constants.HTTP_METHOD, Constants.HTTP_METHOD_HEAD);
        return cMsg;
    }
}
