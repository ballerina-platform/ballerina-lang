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
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Arrays;

import static org.ballerinalang.nativeimpl.Utils.STRUCT_TYPE_TIME;

/**
 * Get the hour, minute, second and millisecond value for the given time.
 *
 * @since 0.89
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "time",
        functionName = "getTime",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = STRUCT_TYPE_TIME, structPackage = "ballerina.time"),
        returnType = {@ReturnType(type = TypeKind.INT),
                      @ReturnType(type = TypeKind.INT),
                      @ReturnType(type = TypeKind.INT),
                      @ReturnType(type = TypeKind.INT)},
        isPublic = true
)
public class GetTime extends AbstractTimeFunction {

    private static final BTupleType getTimeTupleType = new BTupleType(
            Arrays.asList(BTypes.typeInt, BTypes.typeInt, BTypes.typeInt, BTypes.typeInt));

    @Override
    public void execute(Context context) {
        BStruct timeStruct = ((BStruct) context.getRefArgument(0));
        BRefValueArray time = new BRefValueArray(getTimeTupleType);
        time.add(0, new BInteger(getHour(timeStruct)));
        time.add(1, new BInteger(getMinute(timeStruct)));
        time.add(2, new BInteger(getSecond(timeStruct)));
        time.add(3, new BInteger(getMilliSecond(timeStruct)));
        context.setReturnValues(time);
    }
}
