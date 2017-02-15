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

package org.wso2.ballerina.nativeimpl.lang.array;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Native function ballerina.lang.array:sort(double[]).
 */
@BallerinaFunction(
        packageName = "ballerina.lang.array",
        functionName = "sort",
        args = {@Argument(name = "doubleArray", type = TypeEnum.ARRAY, elementType = TypeEnum.DOUBLE)},
        returnType = {@ReturnType(type = TypeEnum.ARRAY, elementType = TypeEnum.DOUBLE)},
        isPublic = true
)
public class DoubleArraySort extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BArray array = (BArray) getArgument(context, 0);

        List<BDouble> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            list.add(i, (BDouble) array.get(i));
        }
        Collections.sort(list, (bDouble1, bDouble2) -> Double.compare(bDouble1.doubleValue(), bDouble2.doubleValue()));
        BArray<BDouble> sortedArray = new BArray<>(BDouble.class);
        int i = 0;
        while (i < list.size()) {
            sortedArray.add(i, list.get(i));
            i++;
        }
        return getBValues(sortedArray);
    }
}
