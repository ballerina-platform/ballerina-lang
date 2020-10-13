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

import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.BValueCreator;
import io.ballerina.runtime.api.Types;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.observability.metrics.PercentileValue;
import io.ballerina.runtime.observability.metrics.Snapshot;
import io.ballerina.runtime.observability.metrics.StatisticConfig;
import io.ballerina.runtime.types.BArrayType;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This provides the util functions to observe related functions.
 */
public class Utils {

    private static final Type STATISTIC_CONFIG_TYPE =
            BValueCreator.createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_ID,
                                                ObserveNativeImplConstants.STATISTIC_CONFIG).getType();

    private static final Type PERCENTILE_VALUE_TYPE =
            BValueCreator.createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_ID,
                                                ObserveNativeImplConstants.PERCENTILE_VALUE).getType();

    private static final Type SNAPSHOT_TYPE =
            BValueCreator.createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_ID,
                                                ObserveNativeImplConstants.SNAPSHOT).getType();

    public static Map<String, String> toStringMap(BMap<BString, ?> map) {
        Map<String, String> returnMap = new HashMap<>();
        if (map != null) {
            for (Entry<BString, ?> keyVals : map.entrySet()) {
                Object value = keyVals.getValue();
                returnMap.put(keyVals.getKey().toString(), value == null ? "()" : value.toString());
            }
        }
        return returnMap;
    }

    public static BArray createBSnapshots(Snapshot[] snapshots) {
        if (snapshots != null && snapshots.length > 0) {

            BArray bSnapshots = BValueCreator.createArrayValue(new BArrayType(SNAPSHOT_TYPE));
            int index = 0;
            for (Snapshot snapshot : snapshots) {
                BArray bPercentiles = BValueCreator.createArrayValue(new BArrayType(PERCENTILE_VALUE_TYPE));
                int percentileIndex = 0;
                for (PercentileValue percentileValue : snapshot.getPercentileValues()) {
                    BMap<BString, Object> bPercentileValue =
                            BValueCreator.createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_ID,
                                                                ObserveNativeImplConstants.PERCENTILE_VALUE);
                    bPercentileValue.put(BStringUtils.fromString("percentile"), percentileValue.getPercentile());
                    bPercentileValue.put(BStringUtils.fromString("value"), percentileValue.getValue());
                    bPercentiles.add(percentileIndex, bPercentileValue);
                    percentileIndex++;
                }

                BMap<BString, Object> aSnapshot = BValueCreator.createRecordValue(
                        ObserveNativeImplConstants.OBSERVE_PACKAGE_ID, ObserveNativeImplConstants.SNAPSHOT);
                aSnapshot.put(BStringUtils.fromString("timeWindow"), snapshot.getTimeWindow().toMillis());
                aSnapshot.put(BStringUtils.fromString("mean"), snapshot.getMean());
                aSnapshot.put(BStringUtils.fromString("max"), snapshot.getMax());
                aSnapshot.put(BStringUtils.fromString("min"), snapshot.getMin());
                aSnapshot.put(BStringUtils.fromString("stdDev"), snapshot.getStdDev());
                aSnapshot.put(BStringUtils.fromString("percentileValues"), bPercentiles);
                bSnapshots.add(index, aSnapshot);
                index++;
            }
            return bSnapshots;
        } else {
            return null;
        }
    }

    public static BArray createBStatisticConfig(StatisticConfig[] configs) {
        if (configs != null) {
            BArray bStatsConfig = BValueCreator.createArrayValue(new BArrayType(STATISTIC_CONFIG_TYPE));
            int index = 0;
            for (StatisticConfig config : configs) {
                BArray bPercentiles = BValueCreator.createArrayValue(new BArrayType(Types.TYPE_FLOAT));
                int percentileIndex = 0;
                for (Double percentile : config.getPercentiles()) {
                    bPercentiles.add(percentileIndex, percentile);
                    percentileIndex++;
                }
                BMap<BString, Object> aSnapshot = BValueCreator.createRecordValue(
                        ObserveNativeImplConstants.OBSERVE_PACKAGE_ID, ObserveNativeImplConstants.STATISTIC_CONFIG);
                aSnapshot.put(BStringUtils.fromString("percentiles"), bPercentiles);
                aSnapshot.put(BStringUtils.fromString("timeWindow"), config.getTimeWindow());
                aSnapshot.put(BStringUtils.fromString("buckets"), config.getBuckets());
                bStatsConfig.add(index, aSnapshot);
                index++;
            }
            return bStatsConfig;
        } else {
            return BValueCreator.createArrayValue(new BArrayType(STATISTIC_CONFIG_TYPE));
        }
    }
}
