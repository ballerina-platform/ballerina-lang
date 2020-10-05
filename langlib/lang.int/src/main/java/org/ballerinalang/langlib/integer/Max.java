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

package org.ballerinalang.langlib.integer;

/**
 * Native implementation of lang.int:max(int, int...).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.int", functionName = "max",
//        args = {@Argument(name = "n", type = TypeKind.INT), @Argument(name = "ns", type = TypeKind.ARRAY)},
//        returnType = {@ReturnType(type = TypeKind.INT)},
//        isPublic = true
//)
public class Max {

    public static long max(long n, long[] ns) {
        long max = n;
        int size = ns.length;
        for (int i = 0; i < size; i++) {
            long current = ns[i];
            max = current >= max ? current : max;
        }
        return max;
    }
}
