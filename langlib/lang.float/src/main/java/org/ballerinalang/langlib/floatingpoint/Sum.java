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

package org.ballerinalang.langlib.floatingpoint;

/**
 * Native implementation of lang.float:sum(float...).
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.float", functionName = "sum",
//        args = {@Argument(name = "ns", type = TypeKind.ARRAY)},
//        returnType = {@ReturnType(type = TypeKind.FLOAT)},
//        isPublic = true
//)
public class Sum {

    public static double sum(double[] ns) {
        double sum = 0;
        for (double n : ns) {
            sum += n;
        }
        return sum;
    }
}
