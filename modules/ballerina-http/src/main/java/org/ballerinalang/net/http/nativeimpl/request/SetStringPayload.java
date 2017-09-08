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

package org.ballerinalang.net.http.nativeimpl.request;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.lang.utils.Constants;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.runtime.message.StringDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Native function to get payload as String..
 * ballerina.net.http:setStringPayload
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "setStringPayload",
        args = {@Argument(name = "request", type = TypeEnum.STRUCT, structType = "Request",
                          structPackage = "ballerina.net.http"),
                @Argument(name = "payload", type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Sets the message payload using a string object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "request",
        value = "The current request object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "payload",
        value = "The string payload object") })
public class SetStringPayload extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(SetStringPayload.class);

    @Override
    public BValue[] execute(Context context) {
        BStruct requestStruct = (BStruct) getRefArgument(context, 0);
        String payload = getStringArgument(context, 0);

        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                .getNativeData(org.ballerinalang.net.http.Constants.TRANSPORT_MESSAGE);
        StringDataSource stringDataSource = new StringDataSource(payload);
        httpCarbonMessage.setMessageDataSource(stringDataSource);
        httpCarbonMessage.setHeader(Constants.CONTENT_TYPE, Constants.TEXT_PLAIN);
        if (log.isDebugEnabled()) {
            log.debug("Setting new payload: " + payload);
        }
        return VOID_RETURN;
    }
}
