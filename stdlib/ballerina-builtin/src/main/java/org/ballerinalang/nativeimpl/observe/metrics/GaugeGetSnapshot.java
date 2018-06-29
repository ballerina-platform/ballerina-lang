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
package org.ballerinalang.nativeimpl.observe.metrics;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.observe.Constants;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.metrics.Gauge;
import org.ballerinalang.util.metrics.PercentileValue;
import org.ballerinalang.util.metrics.Snapshot;

/**
 * This is the getSnapshot native function implementation of the Gauge object.
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe",
        functionName = "getSnapshot",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = Constants.GAUGE,
                structPackage = Constants.OBSERVE_PACKAGE_PATH),
        isPublic = true,
        returnType = @ReturnType(type = TypeKind.ARRAY)
)
public class GaugeGetSnapshot extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BStruct bStruct = (BStruct) context.getRefArgument(0);
        Gauge gauge = (Gauge) bStruct.getNativeData(Constants.METRIC_NATIVE_INSTANCE_KEY);
        Snapshot[] snapshots = gauge.getSnapshots();

        PackageInfo observePackage = context.getProgramFile().getPackageInfo(Constants.OBSERVE_PACKAGE_PATH);
        StructureTypeInfo snapshotStructInfo = observePackage.getStructInfo(Constants.SNAPSHOT);
        StructureTypeInfo percentileStructInfo = observePackage.getStructInfo(Constants.PERCENTILE_VALUE);

        BRefValueArray bSnapshots = new BRefValueArray();
        int index = 0;
        for (Snapshot snapshot : snapshots) {
            BRefValueArray bPercentiles = new BRefValueArray();
            int percentileIndex = 0;
            for (PercentileValue percentileValue : snapshot.getPercentileValues()) {
                BStruct bPercentileValue = BLangVMStructs.createBStruct(percentileStructInfo,
                        percentileValue.getPercentile(),
                        percentileValue.getValue());
                bPercentiles.add(percentileIndex, bPercentileValue);
                percentileIndex++;
            }
            BStruct aSnapshot = BLangVMStructs.createBStruct(snapshotStructInfo, snapshot.getExpiry().toMillis(),
                    snapshot.getMean(), snapshot.getMax(), snapshot.getMin(), snapshot.getStdDev(), bPercentiles);
            bSnapshots.add(index, aSnapshot);
            index++;
        }
        context.setReturnValues(bSnapshots);
    }


}
