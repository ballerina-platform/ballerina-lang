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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Native function ballerina.model.arrays:sort(string[]).
 */
@BallerinaFunction(
        packageName = "ballerina.lang.arrays",
        functionName = "sort",
        args = {@Argument(name = "arr", type = TypeEnum.ARRAY, elementType = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.ARRAY, elementType = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Sorts the specified string array ") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "arr",
        value = "The string array to be sorted") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string[])",
        value = "The sorted array") })
public class StringArraySort extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BArray array = (BArray) getArgument(context, 0);

        List<BString> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            list.add(i, (BString) array.get(i));
        }
        Collections.sort(list, (bString1, bString2) -> bString1.stringValue().compareTo(bString2.stringValue()));
        BArray<BString> sortedArray = new BArray<>(BString.class);
        int i = 0;
        while (i < list.size()) {
            sortedArray.add(i, list.get(i));
            i++;
        }
        return getBValues(sortedArray);
    }
}
