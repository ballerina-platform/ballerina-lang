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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.observability.metrics.PercentileValue;
import io.ballerina.runtime.observability.metrics.Snapshot;
import io.ballerina.runtime.observability.metrics.StatisticConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This provides the util functions to observe related functions.
 */
public class Utils {

    private static final Type STATISTIC_CONFIG_TYPE =
            ValueCreator.createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_ID,
                                           ObserveNativeImplConstants.STATISTIC_CONFIG).getType();

    private static final Type PERCENTILE_VALUE_TYPE =
            ValueCreator.createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_ID,
                                           ObserveNativeImplConstants.PERCENTILE_VALUE).getType();

    private static final Type SNAPSHOT_TYPE =
            ValueCreator.createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_ID,
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

            BArray bSnapshots = ValueCreator.createArrayValue(TypeCreator.createArrayType(SNAPSHOT_TYPE));
            int index = 0;
            for (Snapshot snapshot : snapshots) {
                BArray bPercentiles = ValueCreator.createArrayValue(TypeCreator.createArrayType(PERCENTILE_VALUE_TYPE));
                int percentileIndex = 0;
                for (PercentileValue percentileValue : snapshot.getPercentileValues()) {
                    BMap<BString, Object> bPercentileValue =
                            ValueCreator.createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_ID,
                                                           ObserveNativeImplConstants.PERCENTILE_VALUE);
                    bPercentileValue.put(StringUtils.fromString("percentile"), percentileValue.getPercentile());
                    bPercentileValue.put(StringUtils.fromString("value"), percentileValue.getValue());
                    bPercentiles.add(percentileIndex, bPercentileValue);
                    percentileIndex++;
                }

                BMap<BString, Object> aSnapshot = ValueCreator.createRecordValue(
                        ObserveNativeImplConstants.OBSERVE_PACKAGE_ID, ObserveNativeImplConstants.SNAPSHOT);
                aSnapshot.put(StringUtils.fromString("timeWindow"), snapshot.getTimeWindow().toMillis());
                aSnapshot.put(StringUtils.fromString("mean"), snapshot.getMean());
                aSnapshot.put(StringUtils.fromString("max"), snapshot.getMax());
                aSnapshot.put(StringUtils.fromString("min"), snapshot.getMin());
                aSnapshot.put(StringUtils.fromString("stdDev"), snapshot.getStdDev());
                aSnapshot.put(StringUtils.fromString("percentileValues"), bPercentiles);
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
            BArray bStatsConfig = ValueCreator.createArrayValue(TypeCreator.createArrayType(STATISTIC_CONFIG_TYPE));
            int index = 0;
            for (StatisticConfig config : configs) {
                BArray bPercentiles = ValueCreator.createArrayValue(TypeCreator.createArrayType(
                        PredefinedTypes.TYPE_FLOAT));
                int percentileIndex = 0;
                for (Double percentile : config.getPercentiles()) {
                    bPercentiles.add(percentileIndex, percentile);
                    percentileIndex++;
                }
                BMap<BString, Object> aSnapshot = ValueCreator.createRecordValue(
                        ObserveNativeImplConstants.OBSERVE_PACKAGE_ID, ObserveNativeImplConstants.STATISTIC_CONFIG);
                aSnapshot.put(StringUtils.fromString("percentiles"), bPercentiles);
                aSnapshot.put(StringUtils.fromString("timeWindow"), config.getTimeWindow());
                aSnapshot.put(StringUtils.fromString("buckets"), config.getBuckets());
                bStatsConfig.add(index, aSnapshot);
                index++;
            }
            return bStatsConfig;
        } else {
            return ValueCreator.createArrayValue(TypeCreator.createArrayType(STATISTIC_CONFIG_TYPE));
        }
    }
}
