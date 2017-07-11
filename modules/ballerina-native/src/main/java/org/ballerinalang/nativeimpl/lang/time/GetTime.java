/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.nativeimpl.lang.time;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Get the hour, minute, second and millisecond value for the given time.
 *
 * @since 0.89
 */
@BallerinaFunction(
        packageName = "ballerina.lang.time",
        functionName = "getTime",
        args = {@Argument(name = "time", type = TypeEnum.STRUCT, structType = "Time",
                          structPackage = "ballerina.lang.time")},
        returnType = {@ReturnType(type = TypeEnum.INT),
                      @ReturnType(type = TypeEnum.INT),
                      @ReturnType(type = TypeEnum.INT),
                      @ReturnType(type = TypeEnum.INT)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "Get the hour, minute, second and millisecond values of the given the Time.")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "int) ",
        value = "Hour of the given time value")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "int) ",
        value = "Minute of the given time value")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "int) ",
        value = "Second of the given time value")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "int) ",
        value = "MilliSecond of the given time value")})
public class GetTime extends AbstractTimeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStruct timeStruct = ((BStruct) getRefArgument(context, 0));
        return getBValues(new BInteger(getHour(timeStruct)), new BInteger(getMinute(timeStruct)),
                new BInteger(getSecond(timeStruct)), new BInteger(getMilliSecond(timeStruct)));
    }
}
