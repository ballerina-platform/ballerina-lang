/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.array;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BStreamType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.BStreamIteratorImpl;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native implementation of lang.array:toStream((any|error)[]).
 *
 * @since 1.2
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", functionName = "toStream",
        args = {@Argument(name = "arr", type = TypeKind.ARRAY)},
        returnType = {@ReturnType(type = TypeKind.INT)},
        isPublic = true
)
public class ToStream {

    public static StreamValue toStream(Strand strand, ArrayValue arr) {
        return new StreamValue(new BStreamType(arr.getElementType()), new BStreamIteratorImpl(arr.getIterator()),
                null, null);
    }
}
