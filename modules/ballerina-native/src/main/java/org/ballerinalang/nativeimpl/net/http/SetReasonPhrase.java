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

package org.ballerinalang.nativeimpl.net.http;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.connectors.http.Constants;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Native function to set a custom Http Reason Phrase
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "setReasonPhrase",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE),
                @Argument(name = "reasonPhrase", type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Sets a custom HTTP Reason phrase") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m",
        value = "A message object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "reasonPhrase",
        value = "Reason phrase value") })
public class SetReasonPhrase extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BMessage bMsg = (BMessage) getArgument(context, 0);
        String reasonPhrase = getArgument(context, 1).stringValue();
        bMsg.value().setProperty(Constants.HTTP_REASON_PHRASE, reasonPhrase);
        return VOID_RETURN;
    }
}
