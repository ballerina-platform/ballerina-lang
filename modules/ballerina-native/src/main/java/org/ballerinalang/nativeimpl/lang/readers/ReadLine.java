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
package org.ballerinalang.nativeimpl.lang.readers;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BReader;
import org.ballerinalang.model.values.BString;
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
 * Get the next line in a reader.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.readers",
        functionName = "readLine",
        args = {@Argument(name = "reader", type = TypeEnum.READER)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Gets the next line from reader") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "reader",
        value = "The reader to read the next line from") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "line",
        value = "The next line read from the reader") })
public class ReadLine extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BString result;
        BReader reader = (BReader) getArgument(context, 0);
        String line;
        try {
            line = reader.value().readLine();
        } catch (IOException e) {
            throw new BallerinaException("Exception occurred when reading line", e);
        }
        if (line != null) {
            result = new BString(line);
        } else {
            result = new BString("");
        }
        return getBValues(result);
    }
}
