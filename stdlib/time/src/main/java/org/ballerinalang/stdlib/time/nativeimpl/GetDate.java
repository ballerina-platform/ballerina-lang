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
package org.ballerinalang.stdlib.time.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.util.Arrays;

/**
 * Get the year,month and date value for the given time.
 *
 * @since 0.89
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "time",
        functionName = "getDate"
)
public class GetDate extends AbstractTimeFunction {

    private static final BTupleType getDateTupleType = new BTupleType(
            Arrays.asList(BTypes.typeInt, BTypes.typeInt, BTypes.typeInt));
    private static final org.ballerinalang.jvm.types.BTupleType getDateTupleJvmType = new org.ballerinalang.jvm.types
            .BTupleType(Arrays.asList(org.ballerinalang.jvm.types.BTypes.typeInt,
                                      org.ballerinalang.jvm.types.BTypes.typeInt,
                                      org.ballerinalang.jvm.types.BTypes.typeInt));

    @Override
    public void execute(Context context) {
        BMap<String, BValue> timeStruct = ((BMap<String, BValue>) context.getRefArgument(0));
        BValueArray date = new BValueArray(getDateTupleType);
        date.add(0, new BInteger(getYear(timeStruct)));
        date.add(1, new BInteger(getMonth(timeStruct)));
        date.add(2, new BInteger(getDay(timeStruct)));
        context.setReturnValues(date);
    }

    public static ArrayValue getDate(Strand strand, MapValue<String, Object> timeRecord) {
        ArrayValue date = new ArrayValue(getDateTupleJvmType);
        date.add(0, getYear(timeRecord));
        date.add(1, getMonth(timeRecord));
        date.add(2, getDay(timeRecord));
        return date;
    }
}
