/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.core.runtime.net.http.sender;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.model.values.MessageValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.runtime.internal.ServiceContextHolder;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.MessageProcessorException;

/**
 * HTTP Get function
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "get",
        args = {@Argument(name = "host", type = TypeEnum.STRING),
                @Argument(name = "port", type = TypeEnum.INT),
                @Argument(name = "path", type = TypeEnum.STRING)},
        returnType = {TypeEnum.MESSAGE},
        isPublic = true
)
@Component(
        name = "func.ballerina.net.http.get",
        immediate = true,
        service = AbstractNativeFunction.class
)
public class HTTPGetFunction extends AbstractNativeFunction {

    private static final String INVOCATION_COMPLETE = "INVOCATION_COMPLETE";

    private static final Logger log = LoggerFactory.getLogger(HTTPGetFunction.class);

    @Override
    public BValue[] execute(Context context) {

        CarbonMessage cMsg = context.getCarbonMessage();

        String host = ((StringValue) getArgument(context, 0).getBValue()).getValue();
        int port = ((IntValue) getArgument(context, 1).getBValue()).getValue();
        String urlPath = ((StringValue) getArgument(context, 2).getBValue()).getValue();

        cMsg.setProperty(Constants.HOST, host);
        cMsg.setProperty(Constants.PORT, port);
        cMsg.setProperty(Constants.TO, urlPath);

        cMsg.setProperty(Constants.PROTOCOL, org.wso2.ballerina.core.runtime.net.http.source.Constants.PROTOCOL_HTTP);

        // Set Host header
        if (port != 80) {
            cMsg.getHeaders().set(Constants.HOST, host + ":" + port);
        } else {
            cMsg.getHeaders().set(Constants.HOST, host);
        }

        try {
            ServiceContextHolder.getInstance().getSender().send(cMsg, message -> {
                context.setCarbonMessage(message);
                context.setProperty(INVOCATION_COMPLETE, true);
                cMsg.notify();
            });
        } catch (MessageProcessorException e) {
            log.error("Error while sending the message to the endpoint - " + host + ":" + port + "/" + urlPath);
            //TODO: Handle error
        }

        try {
            // Block the thread from here, TODO: Change this after introducing non-blocking in the chain
            while (context.getProperty(INVOCATION_COMPLETE) == null) {
                cMsg.wait();
            }
            context.removeProperty(INVOCATION_COMPLETE);
        } catch (InterruptedException ignore) {
        }

        return getBValues(new MessageValue(context.getCarbonMessage()));
    }
}
