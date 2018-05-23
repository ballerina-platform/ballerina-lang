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
import org.ballerinalang.util.metrics.Gauge;
import org.ballerinalang.util.metrics.Summary;
import org.ballerinalang.util.metrics.Tag;

import java.util.Set;

/**
 * Get a Summary instance.
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe",
        functionName = "getSummaryInstance",
        args = {
                @Argument(name = "name", type = TypeKind.STRING),
                @Argument(name = "description", type = TypeKind.STRING),
                @Argument(name = "tags", type = TypeKind.MAP),
        },
        returnType = @ReturnType(type = TypeKind.STRUCT),
        isPublic = true
)
public class GetSummaryInstance extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String name = context.getStringArgument(0);
        String description = context.getNullableStringArgument(0);
        if (description == null) {
            // Make description optional
            description = "";
        }
        BMap tags = (BMap) context.getNullableRefArgument(1);
        try {
            Summary.Builder builder = Summary.builder(name).description(description);
            Gauge.Builder gaugeBuilder = Gauge.builder(name + Constants.GAUGE_VALUE_SUFFIX)
                    .description(description);
            if (tags != null) {
                Set<Tag> metricTags = Utils.toTags(tags);
                builder.tags(metricTags);
                gaugeBuilder.tags(metricTags);
            }
            Summary summary = builder.register();
            Gauge gauge = gaugeBuilder.register();
            BStruct bSummary = Utils.createMetricsStruct(context, Constants.SUMMARY);
            bSummary.addNativeData(Constants.SUMMARY, summary);
            bSummary.addNativeData(Constants.GAUGE, gauge);
            context.setReturnValues(bSummary);
        } catch (RuntimeException e) {
            context.setReturnValues(Utils.createErrorStruct(context, e.getMessage()));
        }
    }
}
