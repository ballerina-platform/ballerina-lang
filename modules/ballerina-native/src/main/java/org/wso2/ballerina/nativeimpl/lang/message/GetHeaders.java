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

package org.wso2.ballerina.nativeimpl.lang.message;


import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

/**
 * Get the Headers of the Message.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.message",
        functionName = "getHeaders",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE),
                @Argument(name = "headerName", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.ARRAY, elementType = TypeEnum.STRING)},
        isPublic = true
)
public class GetHeaders extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
//        MessageValue msg = (MessageValue) getArgument(ctx, 0);
//        String headerName = getArgument(ctx, 1).stringValue();
//        String[] headerValue = msg.getHeaders(headerName);

        // TODO Fix this with the proper array implementation
//        ArrayValue<BString> headers = new ArrayValue<>(headerValue.length);
//        int i = 0;
//        for (String header : headerValue) {
//            headers.insert(i++, new StringValue(header));
//        }
//        return getBValues(headers);
//        return getBValues(null);

        throw new RuntimeException("Not supported yet");
    }
}
