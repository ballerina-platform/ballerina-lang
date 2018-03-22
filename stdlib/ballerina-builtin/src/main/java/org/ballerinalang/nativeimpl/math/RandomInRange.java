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
package org.ballerinalang.nativeimpl.math;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Native function wso2.ballerina.math:random.
 *
 * @since 0.90
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "math",
        functionName = "randomInRange",
        args = {@Argument(name = "start", type = TypeKind.INT),
                @Argument(name = "end", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.INT)},
        isPublic = true
)
public class RandomInRange extends BlockingNativeCallableUnit {

    public void execute(Context ctx) {
        long start = ctx.getIntArgument(0);
        long end = ctx.getIntArgument(1);
        long random = ThreadLocalRandom.current().nextLong(start, end);
        ctx.setReturnValues(new BInteger(random));
    }
}
