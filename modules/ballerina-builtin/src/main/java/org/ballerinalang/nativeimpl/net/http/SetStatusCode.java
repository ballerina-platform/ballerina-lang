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
import org.ballerinalang.nativeimpl.actions.http.Constants;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;

/**
 * Set HTTP StatusCode to the message.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "setStatusCode",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE),
                @Argument(name = "statusCode", type = TypeEnum.INT)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Sets the HTTP StatusCode on the message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m",
        value = "A message object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "statusCode",
        value = "HTTP status code") })
public class SetStatusCode extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        try {
            BMessage bMsg = (BMessage) getRefArgument(context, 0);
            long statusCode = getIntArgument(context, 0);
            if (statusCode != (int) statusCode) {
                throw BLangExceptionHelper
                        .getRuntimeException(RuntimeErrors.INDEX_NUMBER_TOO_LARGE, statusCode);
            }
            bMsg.value().setProperty(Constants.HTTP_STATUS_CODE, (int) statusCode);
        } catch (ClassCastException e) {
            throw new BallerinaException("Invalid message or Status Code");
        }
        return VOID_RETURN;
    }
}
