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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Native function ballerina.model.arrays:sort(string[]).
 */
@BallerinaFunction(
        packageName = "ballerina.util.arrays",
        functionName = "sort",
        args = {@Argument(name = "arr", type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        isPublic = true
)
public class StringArraySort extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStringArray array = (BStringArray) getRefArgument(context, 0);

        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            list.add(i, array.get(i));
        }
        Collections.sort(list, Comparator.naturalOrder());
        BStringArray sortedArray = new BStringArray();
        int i = 0;
        while (i < list.size()) {
            sortedArray.add(i, list.get(i));
            i++;
        }
        return getBValues(sortedArray);
    }
}
