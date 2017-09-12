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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Subtract given durations from the time.
 *
 * @since 0.89
 */
@BallerinaFunction(
        packageName = "ballerina.lang.time",
        functionName = "subtractDuration",
        args = {@Argument(name = "time", type = TypeEnum.STRUCT, structType = "Time",
                          structPackage = "ballerina.lang.time"),
                @Argument(name = "years", type = TypeEnum.INT),
                @Argument(name = "months", type = TypeEnum.INT),
                @Argument(name = "days", type = TypeEnum.INT),
                @Argument(name = "hours", type = TypeEnum.INT),
                @Argument(name = "minutes", type = TypeEnum.INT),
                @Argument(name = "seconds", type = TypeEnum.INT),
                @Argument(name = "milliseconds", type = TypeEnum.INT)},
        returnType = {@ReturnType(type = TypeEnum.STRUCT, structType = "Time",
                                  structPackage = "ballerina.lang.time")},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
         value = "Subtract given durations to the time.")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "struct) ",
         value = "The Time struct")})
public class SubtractDuration extends AbstractTimeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStruct timeStruct = ((BStruct) getRefArgument(context, 0));
        long years = getIntArgument(context, 0);
        long months = getIntArgument(context, 1);
        long dates = getIntArgument(context, 2);
        long hours = getIntArgument(context, 3);
        long minutes = getIntArgument(context, 4);
        long seconds = getIntArgument(context, 5);
        long milliSeconds = getIntArgument(context, 6);
        return new BValue[] {
                subtractDuration(context, timeStruct, years, months, dates, hours, minutes, seconds, milliSeconds)
        };
    }
}
