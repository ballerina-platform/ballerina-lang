/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.lang.strings;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.UnsupportedEncodingException;

/**
 * Convert String to Blob.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.strings",
        functionName = "toBlob",
        args = {@Argument(name = "string", type = TypeEnum.STRING),
                @Argument(name = "encoding", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.BLOB)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Converts String to a Blob") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "string",
        value = "String value to be converted") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "encoding",
        value = "Encoding to used in conversion") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "blob",
        value = "Blob representation of the given string") })
public class ToBlob extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        try {
            String string = getStringArgument(ctx, 0);
            String encoding = getStringArgument(ctx, 1);
            byte[] arr = string.getBytes(encoding);
            return getBValues(new BBlob(arr));
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException("Unsupported Encoding", e);
        }
    }
}
