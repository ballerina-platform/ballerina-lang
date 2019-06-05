/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.runtime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.AsyncTimer;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Extern function ballerina/runtime:sleep.
 *
 * @since 0.94.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "runtime",
        functionName = "sleep",
        args = {@Argument(name = "millis", type = TypeKind.INT)},
        isPublic = true
)
public class Sleep implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        long delayMillis = context.getIntArgument(0);
        AsyncTimer.schedule(callback::notifySuccess, delayMillis);
    }

    public static void sleep(Strand strand, long delayMillis) {
        AsyncTimer.schedule(new NonBlockingCallback(strand)::notifySuccess, delayMillis);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
