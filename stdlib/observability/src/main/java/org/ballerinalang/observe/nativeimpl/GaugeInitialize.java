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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.util.metrics.Gauge;
import org.ballerinalang.util.metrics.StatisticConfig;

import java.time.Duration;

/**
 * This is the native initialize function that's getting called when instantiating the Gauge object.
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe",
        functionName = "initialize",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = Constants.GAUGE,
                structPackage = Constants.OBSERVE_PACKAGE_PATH)
)
public class GaugeInitialize extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BMap<String, BValue> bStruct = (BMap<String, BValue>) context.getRefArgument(0);
        BRefValueArray summaryConfigs = (BRefValueArray) bStruct.get(Constants.STATISTICS_CONFIG_FIELD);
        Gauge.Builder gaugeBuilder = Gauge.builder(bStruct.get(Constants.NAME_FIELD).stringValue())
                .description(bStruct.get(Constants.DESCRIPTION_FIELD).stringValue())
                .tags(Utils.toStringMap((BMap) bStruct.get(Constants.TAGS_FIELD)));
        if (summaryConfigs != null && summaryConfigs.size() > 0) {
            for (int i = 0; i < summaryConfigs.size(); i++) {
                BMap summaryConfigStruct = (BMap) summaryConfigs.getBValue(i);
                StatisticConfig.Builder statisticBuilder = StatisticConfig.builder()
                        .expiry(Duration.ofMillis(((BInteger) summaryConfigStruct
                                .get(Constants.EXPIRY_FIELD)).intValue()))
                        .buckets(((BInteger) summaryConfigStruct
                                .get(Constants.BUCKETS_FIELD)).intValue());
                BFloatArray bFloatArray = (BFloatArray) summaryConfigStruct.get(Constants.PERCENTILES_FIELD);
                double[] percentiles = new double[(int) bFloatArray.size()];
                for (int j = 0; j < bFloatArray.size(); j++) {
                    percentiles[j] = bFloatArray.get(j);
                }
                statisticBuilder.percentiles(percentiles);
                StatisticConfig config = statisticBuilder.build();
                gaugeBuilder.summarize(config);
            }
        }
        Gauge gauge = gaugeBuilder.build();
        bStruct.addNativeData(Constants.METRIC_NATIVE_INSTANCE_KEY, gauge);
    }
}
