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

package org.ballerinalang.nativeimpl.utils;


import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Native function ballerina.utils:base64ToBase16Encode.
 *
 * @since 0.8.0
 */

@BallerinaFunction(
        packageName = "ballerina.utils",
        functionName = "base64ToBase16Encode",
        args = { @Argument(name = "baseString", type = TypeEnum.STRING)},
        returnType = { @ReturnType(type = TypeEnum.STRING) },
        isPublic = true)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Encodes a Base64 encoded string into a Base16 encoded string.") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "baseString",
        value = "The input string to be encoded") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The Base16 encoded string") })
/**
 * This function converts a Base 64 encoded string to a Base16 encoded string.
 */
public class Base64ToBase16Encode extends AbstractNativeFunction {


    @Override
    public BValue[] execute(Context context) {
        String stringValue = getArgument(context, 0).stringValue();

        String result;
        byte[] keyBytes = Base64.getDecoder().decode(stringValue.getBytes(Charset.defaultCharset()));

        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[keyBytes.length * 2];

        for (int j = 0; j < keyBytes.length; j++) {
            final int byteVal = keyBytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[byteVal >>> 4];
            hexChars[j * 2 + 1] = hexArray[byteVal & 0x0F];
        }

        result = new String(hexChars);

        return getBValues(new BString(result));
    }
}
