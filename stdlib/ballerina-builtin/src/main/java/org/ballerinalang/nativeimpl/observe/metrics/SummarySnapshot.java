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
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.metrics.Gauge;
import org.ballerinalang.util.metrics.PercentileValue;
import org.ballerinalang.util.metrics.Snapshot;
import org.ballerinalang.util.metrics.Summary;

/**
 * Get a snapshot of all distribution statistics.
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe",
        functionName = "getSnapshot",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = Constants.SUMMARY,
                structPackage = Constants.OBSERVE_PACKAGE_PATH),
        returnType = @ReturnType(type = TypeKind.STRUCT),
        isPublic = true
)
public class SummarySnapshot extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BStruct bSummary = (BStruct) context.getRefArgument(0);
        Gauge gauge = (Gauge) bSummary.getNativeData(Constants.GAUGE);
        Summary summary = (Summary) bSummary.getNativeData(Constants.SUMMARY);
        Snapshot snapshot = summary.getSnapshot();
        double value = gauge.getValue();
        PercentileValue[] percentileValues = snapshot.getPercentileValues();
        BArray<BStruct> bPercentileValues = new BArray<>(BStruct.class);
        for (int i = 0; i < percentileValues.length; i++) {
            PercentileValue percentileValue = percentileValues[i];
            bPercentileValues.add(i, Utils.createMetricsStruct(context, Constants.PERCENTILE_VALUE,
                    percentileValue.getPercentile(), percentileValue.getValue()));
        }
        BStruct bSnapshot = Utils.createMetricsStruct(context, Constants.SNAPSHOT, value, snapshot.getMean(),
                snapshot.getMax(), bPercentileValues);
        context.setReturnValues(bSnapshot);
    }
}
