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
package org.ballerinalang.nativeimpl.lang.math;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
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
        packageName = "ballerina.lang.math",
        functionName = "randomInRange",
        args = {@Argument(name = "start", type = TypeEnum.INT),
                @Argument(name = "end", type = TypeEnum.INT)},
        returnType = {@ReturnType(type = TypeEnum.INT)},
        isPublic = true
)
public class RandomInRange extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        long start = getIntArgument(ctx, 0);
        long end = getIntArgument(ctx, 1);
        long random = ThreadLocalRandom.current().nextLong(start, end);
        return getBValues(new BInteger(random));
    }
}
