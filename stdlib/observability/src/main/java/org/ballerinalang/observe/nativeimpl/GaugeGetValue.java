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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.observe.Constants;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.metrics.Gauge;

/**
 * This is the native getValue function implementation of the Gauge object.
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe",
        functionName = "getValue",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = Constants.GAUGE,
                structPackage = Constants.OBSERVE_PACKAGE_PATH),
        args = {
                @Argument(name = "amount", type = TypeKind.FLOAT)
        },
        returnType = @ReturnType(type = TypeKind.FLOAT),
        isPublic = true
)
public class GaugeGetValue extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BStruct bStruct = (BStruct) context.getRefArgument(0);
        Gauge gauge = (Gauge) bStruct.getNativeData(Constants.METRIC_NATIVE_INSTANCE_KEY);
        context.setReturnValues(new BFloat(gauge.getValue()));
    }
}
