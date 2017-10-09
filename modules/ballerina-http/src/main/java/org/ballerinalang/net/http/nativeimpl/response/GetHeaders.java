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

package org.ballerinalang.net.http.nativeimpl.response;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Get the Headers of the Message.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http.response",
        functionName = "getHeaders",
        args = {@Argument(name = "res", type = TypeKind.STRUCT, structType = "Response",
                structPackage = "ballerina.net.http"),
                @Argument(name = "headerName", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        isPublic = true
)
public class GetHeaders extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
//        MessageValue msg = (MessageValue) getArgument(ctx, 0);
//        String headerName = getArgument(ctx, 1).stringValue();
//        String[] headerValue = msg.getHeaders(headerName);
//
//         TODO Fix this with the proper arrays implementation
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
