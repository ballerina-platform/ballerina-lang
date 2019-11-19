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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.observability.metrics.PercentileValue;
import org.ballerinalang.jvm.observability.metrics.Snapshot;
import org.ballerinalang.jvm.observability.metrics.StatisticConfig;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This provides the util functions to observe related functions.
 */
public class Utils {

    private static final BType STATISTIC_CONFIG_TYPE =
            BallerinaValues.createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_ID,
                    ObserveNativeImplConstants.STATISTIC_CONFIG).getType();

    private static final BType PERCENTILE_VALUE_TYPE =
            BallerinaValues.createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_ID,
                    ObserveNativeImplConstants.PERCENTILE_VALUE).getType();

    private static final BType SNAPSHOT_TYPE =
            BallerinaValues.createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_ID,
                    ObserveNativeImplConstants.SNAPSHOT).getType();

    public static Map<String, String> toStringMap(MapValue<?, ?> map) {
        Map<String, String> returnMap = new HashMap<>();
        if (map != null) {
            for (Entry<?, ?> keyVals : map.entrySet()) {
                Object value = keyVals.getValue();
                returnMap.put(keyVals.getKey().toString(), value == null ? "()" : value.toString());
            }
        }
        return returnMap;
    }

    public static ArrayValue createBSnapshots(Snapshot[] snapshots, Strand strand) {
        if (snapshots != null && snapshots.length > 0) {

            ArrayValue bSnapshots = new ArrayValueImpl(new BArrayType(SNAPSHOT_TYPE));
            int index = 0;
            for (Snapshot snapshot : snapshots) {
                ArrayValue bPercentiles = new ArrayValueImpl(new BArrayType(PERCENTILE_VALUE_TYPE));
                int percentileIndex = 0;
                for (PercentileValue percentileValue : snapshot.getPercentileValues()) {
                    MapValue<String, Object> bPercentileValue =
                            BallerinaValues.createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_ID,
                                    ObserveNativeImplConstants.PERCENTILE_VALUE);
                    bPercentileValue.put("percentile", percentileValue.getPercentile());
                    bPercentileValue.put("value", percentileValue.getValue());
                    bPercentiles.add(percentileIndex, bPercentileValue);
                    percentileIndex++;
                }

                MapValue<String, Object> aSnapshot = BallerinaValues.createRecordValue(
                        ObserveNativeImplConstants.OBSERVE_PACKAGE_ID, ObserveNativeImplConstants.SNAPSHOT);
                aSnapshot.put("timeWindow", snapshot.getTimeWindow().toMillis());
                aSnapshot.put("mean", snapshot.getMean());
                aSnapshot.put("max", snapshot.getMax());
                aSnapshot.put("min", snapshot.getMin());
                aSnapshot.put("stdDev", snapshot.getStdDev());
                aSnapshot.put("percentileValues", bPercentiles);
                bSnapshots.add(index, aSnapshot);
                index++;
            }
            return bSnapshots;
        } else {
            return null;
        }
    }

    public static ArrayValue createBStatisticConfig(StatisticConfig[] configs) {
        if (configs != null) {
            ArrayValue bStatsConfig = new ArrayValueImpl(new BArrayType(STATISTIC_CONFIG_TYPE));
            int index = 0;
            for (StatisticConfig config : configs) {
                ArrayValue bPercentiles = new ArrayValueImpl(new BArrayType(BTypes.typeFloat));
                int percentileIndex = 0;
                for (Double percentile : config.getPercentiles()) {
                    bPercentiles.add(percentileIndex, percentile);
                    percentileIndex++;
                }
                MapValue<String, Object> aSnapshot = BallerinaValues.createRecordValue(
                        ObserveNativeImplConstants.OBSERVE_PACKAGE_ID, ObserveNativeImplConstants.STATISTIC_CONFIG);
                aSnapshot.put("percentiles", bPercentiles);
                aSnapshot.put("timeWindow", config.getTimeWindow());
                aSnapshot.put("buckets", config.getBuckets());
                bStatsConfig.add(index, aSnapshot);
                index++;
            }
            return bStatsConfig;
        } else {
            return new ArrayValueImpl(new BArrayType(STATISTIC_CONFIG_TYPE));
        }
    }
}
