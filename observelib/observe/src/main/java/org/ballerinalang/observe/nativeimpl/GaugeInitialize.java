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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.observability.metrics.Gauge;
import io.ballerina.runtime.observability.metrics.StatisticConfig;

import java.time.Duration;

/**
 * This is the native initialize function that's getting called when instantiating the Gauge object.
 *
 * @since 0.980.0
 */

public class GaugeInitialize {

    public static void initialize(BObject guage) {
        BArray summaryConfigs = (BArray) guage.get(ObserveNativeImplConstants.STATISTICS_CONFIG_FIELD);
        Gauge.Builder gaugeBuilder = Gauge.builder(guage.get(ObserveNativeImplConstants.NAME_FIELD).toString())
                .description(guage.get(ObserveNativeImplConstants.DESCRIPTION_FIELD).toString())
                .tags(Utils.toStringMap((BMap<BString, ?>) guage.get(ObserveNativeImplConstants.TAGS_FIELD)));
        if (summaryConfigs != null && summaryConfigs.size() > 0) {
            for (int i = 0; i < summaryConfigs.size(); i++) {
                BMap<BString, Object> summaryConfigStruct = (BMap<BString, Object>) summaryConfigs.get(i);
                StatisticConfig.Builder statisticBuilder = StatisticConfig.builder().expiry(
                        Duration.ofMillis(((long) summaryConfigStruct.get(ObserveNativeImplConstants.EXPIRY_FIELD))))
                        .buckets(((long) summaryConfigStruct.get(ObserveNativeImplConstants.BUCKETS_FIELD)));
                BArray bFloatArray =
                        (BArray) summaryConfigStruct.get(ObserveNativeImplConstants.PERCENTILES_FIELD);
                double[] percentiles = new double[(int) bFloatArray.size()];
                for (int j = 0; j < bFloatArray.size(); j++) {
                    percentiles[j] = bFloatArray.getFloat(j);
                }
                statisticBuilder.percentiles(percentiles);
                StatisticConfig config = statisticBuilder.build();
                gaugeBuilder.summarize(config);
            }
        }
        Gauge gauge = gaugeBuilder.build();
        guage.addNativeData(ObserveNativeImplConstants.METRIC_NATIVE_INSTANCE_KEY, gauge);
    }
}
