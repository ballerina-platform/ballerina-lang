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
package org.ballerinalang.nativeimpl.lang.inputstreams;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BInputStream;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;
import java.io.IOException;

/**
 * Get the next byte in an inputstream.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.inputstreams",
        functionName = "readByte",
        args = {@Argument(name = "is", type = TypeEnum.INPUTSTREAM)},
        returnType = {@ReturnType(type = TypeEnum.INT)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Gets the next byte from inputstream") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "is",
        value = "The inputstream to read next byte from") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "int",
        value = "The int value of next byte") })
public class ReadByte extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BInteger result;
        BInputStream inputStream = (BInputStream) getArgument(context, 0);
        try {
            result = new BInteger(inputStream.value().read());
        } catch (IOException e) {
            throw new BallerinaException("Error occurred while reading input stream", e);
        }
        return getBValues(result);
    }
}
