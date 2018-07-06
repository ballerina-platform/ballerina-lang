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
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.util.metrics.Counter;

/**
 * This is the native initialize function that's getting called when instantiating the Counter object.
 *
 * @since 0.980.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe",
        functionName = "initialize",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = ObserveNativeImplConstants.COUNTER,
                structPackage = ObserveNativeImplConstants.OBSERVE_PACKAGE_PATH)
)
public class CounterInitialize extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BMap<String, BValue> bStruct = (BMap<String, BValue>) context.getRefArgument(0);
        Counter counter = Counter.builder(bStruct.get(ObserveNativeImplConstants.NAME_FIELD).stringValue())
                .description(bStruct.get(ObserveNativeImplConstants.DESCRIPTION_FIELD).stringValue())
                .tags(Utils.toStringMap((BMap) bStruct.get(ObserveNativeImplConstants.TAGS_FIELD)))
                .build();
        bStruct.addNativeData(ObserveNativeImplConstants.METRIC_NATIVE_INSTANCE_KEY, counter);
    }
}
