/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.observe.metrics;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.metrics.Counter;

/**
 * Get a Counter instance.
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe",
        functionName = "getCounterInstance",
        args = {
                @Argument(name = "name", type = TypeKind.STRING),
                @Argument(name = "description", type = TypeKind.STRING),
                @Argument(name = "tags", type = TypeKind.MAP),
        },
        returnType = @ReturnType(type = TypeKind.STRUCT),
        isPublic = true
)
public class GetCounterInstance extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String name = context.getStringArgument(0);
        String description = context.getStringArgument(1);
        if (description == null) {
            // Make description optional
            description = "";
        }
        BMap tags = (BMap) context.getNullableRefArgument(0);
        try {
            Counter.Builder builder = Counter.builder(name).description(description);
            if (tags != null) {
                builder.tags(Utils.toTags(tags));
            }
            Counter counter = builder.register();
            BStruct bCounter = Utils.createMetricsStruct(context, Constants.COUNTER, name, description, tags);
            bCounter.addNativeData(Constants.COUNTER, counter);
            context.setReturnValues(bCounter);
        } catch (RuntimeException e) {
            context.setReturnValues(Utils.createErrorStruct(context, e.getMessage()));
        }
    }
}
