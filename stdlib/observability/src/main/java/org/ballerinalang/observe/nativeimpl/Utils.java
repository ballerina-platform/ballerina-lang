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
 *
 */

package org.ballerinalang.observe.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.metrics.PercentileValue;
import org.ballerinalang.util.metrics.Snapshot;
import org.ballerinalang.util.metrics.StatisticConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * This provides the util functions to observe related functions.
 */
public class Utils {

    public static Map<String, String> toStringMap(BMap map) {
        Map<String, String> returnMap = new HashMap<>();
        if (map != null) {
            for (Object aKey : map.keys()) {
                returnMap.put(aKey.toString(), map.get(aKey).stringValue());
            }
        }
        return returnMap;
    }

    public static BError createError(Context context, String message) {
        return BLangVMErrors.createError(context, message);
    }

    public static BValueArray createBSnapshots(Snapshot[] snapshots, Context context) {
        if (snapshots != null && snapshots.length > 0) {
            PackageInfo observePackage = context.getProgramFile().
                    getPackageInfo(ObserveNativeImplConstants.OBSERVE_PACKAGE_PATH);
            StructureTypeInfo snapshotStructInfo = observePackage.
                    getStructInfo(ObserveNativeImplConstants.SNAPSHOT);
            StructureTypeInfo percentileStructInfo = observePackage.
                    getStructInfo(ObserveNativeImplConstants.PERCENTILE_VALUE);

            BValueArray bSnapshots = new BValueArray(new BArrayType(observePackage.
                    getTypeInfo(ObserveNativeImplConstants.SNAPSHOT).getType()));
            int index = 0;
            for (Snapshot snapshot : snapshots) {
                BValueArray bPercentiles = new BValueArray(new BArrayType(observePackage.
                        getTypeInfo(ObserveNativeImplConstants.PERCENTILE_VALUE).getType()));
                int percentileIndex = 0;
                for (PercentileValue percentileValue : snapshot.getPercentileValues()) {
                    BMap<String, BValue> bPercentileValue = BLangVMStructs.createBStruct(percentileStructInfo,
                            percentileValue.getPercentile(),
                            percentileValue.getValue());
                    bPercentiles.add(percentileIndex, bPercentileValue);
                    percentileIndex++;
                }
                BMap<String, BValue> aSnapshot = BLangVMStructs.createBStruct(snapshotStructInfo,
                        snapshot.getTimeWindow().toMillis(), snapshot.getMean(), snapshot.getMax(),
                        snapshot.getMin(), snapshot.getStdDev(), bPercentiles);
                bSnapshots.add(index, aSnapshot);
                index++;
            }
            return bSnapshots;
        } else {
            return null;
        }
    }

    public static BValueArray createBStatisticConfig(StatisticConfig[] configs, Context context) {
        PackageInfo observePackage = context.getProgramFile().
                getPackageInfo(ObserveNativeImplConstants.OBSERVE_PACKAGE_PATH);
        StructureTypeInfo statisticConfigInfo = observePackage.
                getStructInfo(ObserveNativeImplConstants.STATISTIC_CONFIG);
        if (configs != null) {
            BValueArray bStatsConfig = new BValueArray(new BArrayType(statisticConfigInfo.getType()));
            int index = 0;
            for (StatisticConfig config : configs) {
                BValueArray bPercentiles = new BValueArray(new BArrayType(BTypes.typeFloat));
                int percentileIndex = 0;
                for (Double percentile : config.getPercentiles()) {
                    bPercentiles.add(percentileIndex, new BFloat(percentile));
                    percentileIndex++;
                }
                BMap<String, BValue> aSnapshot = BLangVMStructs.createBStruct(statisticConfigInfo,
                        bPercentiles, config.getTimeWindow(), config.getBuckets());
                bStatsConfig.add(index, aSnapshot);
                index++;
            }
            return bStatsConfig;
        } else {
            return new BValueArray(new BArrayType(statisticConfigInfo.getType()));
        }
    }
}
