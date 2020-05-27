/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.ballerinalang.observe.nativeimpl;

import org.ballerinalang.jvm.observability.metrics.Counter;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

/**
 * This is the native increment function implementation of the Counter object.
 *
 * @since 0.980.0
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe", version = "0.8.0",
        functionName = "increment",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = ObserveNativeImplConstants.COUNTER,
                structPackage = ObserveNativeImplConstants.OBSERVE_PACKAGE_PATH),
        args = {
                @Argument(name = "amount", type = TypeKind.INT)
        },
        isPublic = true
)
public class CounterIncrement {

    public static void increment(Strand strand, ObjectValue counterObj, long amount) {
        Counter counter = (Counter) counterObj.getNativeData(ObserveNativeImplConstants.METRIC_NATIVE_INSTANCE_KEY);
        counter.increment(amount);
    }
}
