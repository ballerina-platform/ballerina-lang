/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BIterator;

/**
 * Native implementation of lang.array:iterator(Type[]).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.array", functionName = "iterator",
//        args = {@Argument(name = "m", type = TypeKind.ARRAY)},
//        returnType = {@ReturnType(type = TypeKind.OBJECT)},
//        isPublic = true
//)
public class GetIterator {

    public static BIterator iterator(BArray arr) {
        return arr.getIterator();
    }
}
