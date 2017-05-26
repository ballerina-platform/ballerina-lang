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
import org.ballerinalang.model.values.BReader;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Gets the reader from inputstream.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.inputstreams",
        functionName = "getReader",
        args = {@Argument(name = "is", type = TypeEnum.INPUTSTREAM)},
        returnType = {@ReturnType(type = TypeEnum.READER)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Gets the reader from inputstream") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "is",
        value = "The inputstream to get reader from") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "reader",
        value = "The reader of inputstream") })
public class GetReader extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BReader result;
        BInputStream is = (BInputStream) getArgument(context, 0);
        result = new BReader(is.value());
        return getBValues(result);
    }
}
