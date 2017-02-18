/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.lang.arrays;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function ballerina.model.arrays:copyOfRange(json[], int, int).
 */
@BallerinaFunction(
        packageName = "ballerina.lang.arrays",
        functionName = "copyOfRange",
        args = {@Argument(name = "jsonArray", type = TypeEnum.ARRAY, elementType = TypeEnum.JSON),
                @Argument(name = "from", type = TypeEnum.INT),
                @Argument(name = "to", type = TypeEnum.INT)},
        returnType = {@ReturnType(type = TypeEnum.ARRAY, elementType = TypeEnum.JSON)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Copies the specified range of the specified JSON array ") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "jsonArray",
        value = "The JSON array from which the range will be copied") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "from",
        value = "The initial index of the range") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "to",
        value = "The final index of the range") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "json[]",
        value = "A new array with the specified range from the original array") })
public class JsonArrayRangeCopy extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BArray array = (BArray) getArgument(context, 0);
        BInteger argFrom = (BInteger) getArgument(context, 1);
        BInteger argTo = (BInteger) getArgument(context, 2);

        int from = argFrom.intValue();
        int to = argTo.intValue();

        if (from < 0 || to > array.size()) {
            throw new BallerinaException(
                    "Array index out of range. Actual:" + array.size() + " requested: " + from + " to " + to);
        }
        BArray<BJSON> newArray = new BArray<>(BJSON.class);
        int index = 0;
        for (int i = from; i < to; i++) {
            newArray.add(index++, array.get(i));
        }
        return getBValues(newArray);
    }
}
