/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.nativeimpl.request;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.net.http.util.RequestResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set the payload of the Message as a XML.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "setXmlPayload",
        args = {@Argument(name = "request", type = TypeEnum.STRUCT, structType = "Request",
                          structPackage = "ballerina.net.http"),
                @Argument(name = "payload", type = TypeEnum.XML)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Sets the message payload using an XML object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "request",
                                                                        value = "The current request object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "payload",
        value = "The XML payload object") })
public class SetXMLPayload extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(SetXMLPayload.class);

    @Override
    public BValue[] execute(Context context) {
        return RequestResponseUtil.setXMLPayload(context, this, log);
    }
}
