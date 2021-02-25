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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
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

    static Type metricType;
    static Type statisticsConfigType;
    static Type percentileValueType;
    static Type snapshotType;

    public static void initializeModule(Environment env) {
        metricType = ValueCreator.createRecordValue(env.getCurrentModule(),
                ObserveNativeImplConstants.METRIC).getType();
        statisticsConfigType = ValueCreator.createRecordValue(env.getCurrentModule(),
                ObserveNativeImplConstants.STATISTIC_CONFIG).getType();
        percentileValueType = ValueCreator.createRecordValue(env.getCurrentModule(),
                ObserveNativeImplConstants.PERCENTILE_VALUE).getType();
        snapshotType = ValueCreator.createRecordValue(env.getCurrentModule(),
                ObserveNativeImplConstants.SNAPSHOT).getType();
    }

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

    public static BArray createBSnapshots(Environment env, Snapshot[] snapshots) {
        if (snapshots != null && snapshots.length > 0) {

            BArray bSnapshots = ValueCreator.createArrayValue(TypeCreator.createArrayType(snapshotType));
            int index = 0;
            for (Snapshot snapshot : snapshots) {
                BArray bPercentiles = ValueCreator.createArrayValue(TypeCreator.createArrayType(percentileValueType));
                int percentileIndex = 0;
                for (PercentileValue percentileValue : snapshot.getPercentileValues()) {
                    BMap<BString, Object> bPercentileValue =
                            ValueCreator.createRecordValue(env.getCurrentModule(),
                                    ObserveNativeImplConstants.PERCENTILE_VALUE);
                    bPercentileValue.put(StringUtils.fromString("percentile"), percentileValue.getPercentile());
                    bPercentileValue.put(StringUtils.fromString("value"), percentileValue.getValue());
                    bPercentiles.add(percentileIndex, bPercentileValue);
                    percentileIndex++;
                }

                BMap<BString, Object> aSnapshot = ValueCreator.createRecordValue(
                        env.getCurrentModule(), ObserveNativeImplConstants.SNAPSHOT);
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

    public static BArray createBStatisticConfig(Environment env, StatisticConfig[] configs) {
        if (configs != null) {
            BArray bStatsConfig = ValueCreator.createArrayValue(TypeCreator.createArrayType(statisticsConfigType));
            int index = 0;
            for (StatisticConfig config : configs) {
                BArray bPercentiles = ValueCreator.createArrayValue(TypeCreator.createArrayType(
                        PredefinedTypes.TYPE_FLOAT));
                int percentileIndex = 0;
                for (Double percentile : config.getPercentiles()) {
                    bPercentiles.add(percentileIndex, percentile);
                    percentileIndex++;
                }
                BMap<BString, Object> aSnapshot = ValueCreator.createRecordValue(env.getCurrentModule(),
                        ObserveNativeImplConstants.STATISTIC_CONFIG);
                aSnapshot.put(StringUtils.fromString("percentiles"), bPercentiles);
                aSnapshot.put(StringUtils.fromString("timeWindow"), config.getTimeWindow());
                aSnapshot.put(StringUtils.fromString("buckets"), config.getBuckets());
                bStatsConfig.add(index, aSnapshot);
                index++;
            }
            return bStatsConfig;
        } else {
            return ValueCreator.createArrayValue(TypeCreator.createArrayType(statisticsConfigType));
        }
    }
}
