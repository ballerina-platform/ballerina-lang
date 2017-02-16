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
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

/**
 * Native function ballerina.lang.array:copyOf(string[]).
 */
@BallerinaFunction(
        packageName = "ballerina.lang.array",
        functionName = "copyOf",
        args = {@Argument(name = "stringArray", type = TypeEnum.ARRAY, elementType = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.ARRAY, elementType = TypeEnum.STRING)},
        isPublic = true
)
public class StringArrayCopyOf extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BArray array = (BArray) getArgument(context, 0);
        BArray<BString> newArray = new BArray<>(BString.class);
        for (int i = 0; i < array.size(); i++) {
            newArray.add(i, array.get(i));
        }
        return getBValues(newArray);
    }
}
