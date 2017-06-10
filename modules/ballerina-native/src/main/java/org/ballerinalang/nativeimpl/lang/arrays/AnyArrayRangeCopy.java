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
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;

/**
 * Native function ballerina.model.arrays:copyOfRange(any[], any[], int, int).
 */
@BallerinaFunction(
        packageName = "ballerina.lang.arrays",
        functionName = "copyOfRange",
        args = {@Argument(name = "anyArrayFrom", type = TypeEnum.ARRAY, elementType = TypeEnum.ANY),
                @Argument(name = "anyArrayTo", type = TypeEnum.ARRAY, elementType = TypeEnum.ANY),
                @Argument(name = "from", type = TypeEnum.INT),
                @Argument(name = "to", type = TypeEnum.INT)},
        returnType = {@ReturnType(type = TypeEnum.INT)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Copies the specified range of the specified float array ") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "anyArrayFrom",
        value = "The any array from which the range will be copied") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "anyArrayTo",
        value = "The any array to which the range will be copied") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "from",
        value = "The initial index of the range") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "to",
        value = "The final index of the range") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "int",
        value = "Number of elements copied") })
public class AnyArrayRangeCopy extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BNewArray arrayFrom = (BNewArray) getRefArgument(context, 0);
        BNewArray arrayTo = (BNewArray) getRefArgument(context, 1);

        long fromLong = getIntArgument(context, 0);
        long toLong = getIntArgument(context, 1);

        if (toLong != (int) toLong) {
            throw BLangExceptionHelper
                    .getRuntimeException(RuntimeErrors.INDEX_NUMBER_TOO_LARGE, toLong);
        }
        if (fromLong != (int) fromLong) {
            throw BLangExceptionHelper
                    .getRuntimeException(RuntimeErrors.INDEX_NUMBER_TOO_LARGE, fromLong);
        }

        int from = (int) fromLong;
        int to = (int) toLong;
        if (from < 0 || to > arrayFrom.size()) {
            throw new BallerinaException(
                    "Array index out of range. Actual:" + arrayFrom.size() + " requested: " + from + " to " + to);
        }


        if (arrayFrom instanceof BIntArray && arrayTo instanceof BIntArray) {
            BIntArray intArrayFrom = (BIntArray) arrayFrom;
            BIntArray intArrayTo = (BIntArray) arrayTo;
            int index = 0;
            for (int i = from; i < to; i++) {
                intArrayTo.add(index++, intArrayFrom.get(i));
            }
        } else if (arrayFrom instanceof BFloatArray && arrayTo instanceof BFloatArray) {
            BFloatArray floatArrayFrom = (BFloatArray) arrayFrom;
            BFloatArray floatArrayTo = (BFloatArray) arrayTo;
            int index = 0;
            for (int i = from; i < to; i++) {
                floatArrayTo.add(index++, floatArrayFrom.get(i));
            }
        } else if (arrayFrom instanceof BStringArray && arrayTo instanceof BStringArray) {
            BStringArray stringArrayFrom = (BStringArray) arrayFrom;
            BStringArray stringArrayTo = (BStringArray) arrayTo;
            int index = 0;
            for (int i = from; i < to; i++) {
                stringArrayTo.add(index++, stringArrayFrom.get(i));
            }
        } else if (arrayFrom instanceof BBooleanArray && arrayTo instanceof BBooleanArray) {
            BBooleanArray booleanArrayFrom = (BBooleanArray) arrayFrom;
            BBooleanArray booleanArrayTo = (BBooleanArray) arrayTo;
            int index = 0;
            for (int i = from; i < to; i++) {
                booleanArrayTo.add(index++, booleanArrayFrom.get(i));
            }
        } else if (arrayFrom instanceof BRefValueArray && arrayTo instanceof BRefValueArray) {
            BRefValueArray refValueArrayFrom = (BRefValueArray) arrayFrom;
            BRefValueArray refValueArrayTo = (BRefValueArray) arrayTo;
            int index = 0;
            for (int i = from; i < to; i++) {
                refValueArrayTo.add(index++, refValueArrayFrom.get(i));
            }
        } else {
            throw BLangExceptionHelper
                    .getRuntimeException(RuntimeErrors.ARRAY_TYPE_MISMATCH, arrayFrom.getType(), arrayTo.getType());
        }
        return getBValues(new BInteger(to - from));
    }
}
