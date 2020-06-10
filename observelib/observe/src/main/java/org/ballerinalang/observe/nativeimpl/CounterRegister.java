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

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.observability.metrics.Counter;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

/**
 * This is the native register function implementation of the Counter object.
 *
 * @since 0.980.0
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe", version = "0.8.0",
        functionName = "register",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = ObserveNativeImplConstants.COUNTER,
                structPackage = ObserveNativeImplConstants.OBSERVE_PACKAGE_PATH),
        isPublic = true
)
public class CounterRegister {

    public static Object register(Strand strand, ObjectValue counterObj) {
        try {
            Counter counter = (Counter) counterObj.getNativeData(ObserveNativeImplConstants.METRIC_NATIVE_INSTANCE_KEY);
            Counter registeredCounter = counter.register();
            counterObj.addNativeData(ObserveNativeImplConstants.METRIC_NATIVE_INSTANCE_KEY, registeredCounter);
        } catch (Exception e) {
            return BallerinaErrors.createError(e.getMessage());
        }

        return null;
    }
}
