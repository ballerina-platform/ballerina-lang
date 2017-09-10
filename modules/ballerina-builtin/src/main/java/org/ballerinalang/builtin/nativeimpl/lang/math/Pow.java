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
package org.ballerinalang.builtin.nativeimpl.lang.math;

import org.ballerinalang.annotation.natives.Argument;
import org.ballerinalang.annotation.natives.BallerinaFunction;
import org.ballerinalang.annotation.natives.ReturnType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.NativeFunction;

/**
 * Native function ballerina.lang.math:pow.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.lang.math",
        functionName = "pow",
        args = {@Argument(name = "a", type = TypeKind.FLOAT),
                @Argument(name = "b", type = TypeKind.FLOAT)},
        returnType = {@ReturnType(type = TypeKind.FLOAT)},
        isPublic = true
)
public class Pow implements NativeFunction {

    public Object[] execute(Object[] args) {
        double a = (Double) args[0];
        double b = (Double) args[1];
        return new Object[] { new Double(Math.pow(a, b)) };
    }
    
}
