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
import org.osgi.service.component.annotations.Component;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Native function ballerina.utils:base64encode.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.utils",
        functionName = "base64encode",
        args = {@Argument(name = "s", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Encodes the specified string into a string using the Base64 scheme") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "s",
        value = "The input string to be encoded") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The encoded string") })
@Component(
        name = "func.util_base64encode",
        immediate = true,
        service = AbstractNativeFunction.class
)
public class Base64Encode extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String str = getArgument(context, 0).stringValue();
        byte[] encodedValue = Base64.getEncoder().encode(str.getBytes(Charset.defaultCharset()));

        return getBValues(new BString(new String(encodedValue, Charset.defaultCharset())));
    }
}
