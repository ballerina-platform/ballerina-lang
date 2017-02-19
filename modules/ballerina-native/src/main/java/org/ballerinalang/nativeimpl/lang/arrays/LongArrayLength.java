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
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;

/**
 * Native function ballerina.model.arrays:length(long[]).
 */
//@BallerinaFunction(
//        packageName = "ballerina.lang.arrays",
//        functionName = "length",
//        args = {@Argument(name = "longArray", type = TypeEnum.ARRAY, elementType = TypeEnum.LONG)},
//        returnType = {@ReturnType(type = TypeEnum.INT)},
//        isPublic = true
//)
public class LongArrayLength extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BArray array = (BArray) getArgument(context, 0);
        return getBValues(new BInteger(array.size()));
    }
}
