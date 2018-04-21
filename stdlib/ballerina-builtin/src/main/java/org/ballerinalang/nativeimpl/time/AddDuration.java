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
package org.ballerinalang.nativeimpl.time;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.nativeimpl.Utils.STRUCT_TYPE_TIME;

/**
 * Add given durations to the time.
 *
 * @since 0.89
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "time",
        functionName = "addDuration",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = STRUCT_TYPE_TIME, structPackage = "ballerina.time"),
        args = {@Argument(name = "years", type = TypeKind.INT),
                @Argument(name = "months", type = TypeKind.INT),
                @Argument(name = "days", type = TypeKind.INT),
                @Argument(name = "hours", type = TypeKind.INT),
                @Argument(name = "minutes", type = TypeKind.INT),
                @Argument(name = "seconds", type = TypeKind.INT),
                @Argument(name = "milliseconds", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "Time",
                                  structPackage = "ballerina.time")},
        isPublic = true
)
public class AddDuration extends AbstractTimeFunction {

    @Override
    public void execute(Context context) {
        BStruct timeStruct = ((BStruct) context.getRefArgument(0));
        long years = context.getIntArgument(0);
        long months = context.getIntArgument(1);
        long dates = context.getIntArgument(2);
        long hours = context.getIntArgument(3);
        long minutes = context.getIntArgument(4);
        long seconds = context.getIntArgument(5);
        long milliSeconds = context.getIntArgument(6);
        context.setReturnValues(
                addDuration(context, timeStruct, years, months, dates, hours, minutes, seconds, milliSeconds));
    }
}
