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
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.observability.metrics.Counter;
import org.ballerinalang.jvm.observability.metrics.DefaultMetricRegistry;
import org.ballerinalang.jvm.observability.metrics.Gauge;
import org.ballerinalang.jvm.observability.metrics.Metric;
import org.ballerinalang.jvm.observability.metrics.MetricId;
import org.ballerinalang.jvm.observability.metrics.PolledGauge;
import org.ballerinalang.jvm.observability.metrics.Tag;
import org.ballerinalang.jvm.observability.metrics.Tags;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.observe.nativeimpl.ObserveNativeImplConstants.COUNTER;
import static org.ballerinalang.observe.nativeimpl.ObserveNativeImplConstants.GAUGE;
import static org.ballerinalang.observe.nativeimpl.ObserveNativeImplConstants.METRIC_NATIVE_INSTANCE_KEY;
import static org.ballerinalang.observe.nativeimpl.ObserveNativeImplConstants.OBSERVE_PACKAGE_PATH;

/**
 * This is the lookupMetric function native implementation for the registered metrics.
 *
 * @since 0.980.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe",
        functionName = "lookupMetric",
        returnType = @ReturnType(type = TypeKind.ARRAY),
        isPublic = true
)
public class LookupMetric extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
//        String metricName = context.getStringArgument(0);
//        BMap tags = (BMap) context.getNullableRefArgument(0);
//        Map<String, String> tagMap = Utils.toStringMap(tags);
//        Set<Tag> tagSet = new HashSet<>();
//        Tags.tags(tagSet, tagMap);
//        Metric metric = DefaultMetricRegistry.getInstance().lookup(new MetricId(metricName, "", tagSet));
//
//        if (metric != null) {
//            PackageInfo observePackage = context.getProgramFile().getPackageInfo(OBSERVE_PACKAGE_PATH);
//            MetricId metricId = metric.getId();
//            if (metric instanceof Counter) {
//                StructureTypeInfo counterStructInfo = observePackage.getStructInfo(COUNTER);
//                BMap counter = BLangVMStructs.createBStruct(counterStructInfo,
//                        metricId.getName(), metricId.getDescription(), getTags(metricId));
//                counter.addNativeData(METRIC_NATIVE_INSTANCE_KEY, metric);
//                context.setReturnValues(counter);
//            } else if (metric instanceof Gauge) {
//                Gauge gauge = (Gauge) metric;
//                StructureTypeInfo gaugeStructInfo = observePackage.getStructInfo(GAUGE);
//                BMap bGauge = BLangVMStructs.createBStruct(gaugeStructInfo,
//                        metricId.getName(), metricId.getDescription(), getTags(metricId),
//                        Utils.createBStatisticConfig(gauge.getStatisticsConfig(), context));
//                bGauge.addNativeData(METRIC_NATIVE_INSTANCE_KEY, metric);
//                context.setReturnValues(bGauge);
//            } else if (metric instanceof PolledGauge) {
//                StructureTypeInfo gaugeStructInfo = observePackage.getStructInfo(GAUGE);
//                BMap bGauge = BLangVMStructs.createBStruct(gaugeStructInfo,
//                        metricId.getName(), metricId.getDescription(), getTags(metricId),
//                        Utils.createBStatisticConfig(null, context));
//                bGauge.addNativeData(METRIC_NATIVE_INSTANCE_KEY, metric);
//            }
//        } else {
//            context.setReturnValues();
//        }
    }

    public static Object lookupMetric(Strand strand, String metricName, Object tags) {

        Map<String, String> tagMap = Utils.toStringMap((MapValue<?, ?>) tags);
        Set<Tag> tagSet = new HashSet<>();
        Tags.tags(tagSet, tagMap);
        Metric metric = DefaultMetricRegistry.getInstance().lookup(new MetricId(metricName, "", tagSet));

        if (metric != null) {
            MetricId metricId = metric.getId();
            if (metric instanceof Counter) {
                ObjectValue counter = BallerinaValues.createObjectValue(OBSERVE_PACKAGE_PATH, COUNTER,
                        metricId.getName(), metricId.getDescription(), getTags(metricId));
                counter.addNativeData(METRIC_NATIVE_INSTANCE_KEY, metric);
                return counter;
            } else if (metric instanceof Gauge) {
                Gauge gauge = (Gauge) metric;
                ArrayValue statisticConfigs = Utils.createBStatisticConfig(gauge.getStatisticsConfig());
                ObjectValue bGauge = BallerinaValues.createObjectValue(OBSERVE_PACKAGE_PATH, GAUGE, metricId.getName(),
                        metricId.getDescription(), getTags(metricId), statisticConfigs);
                bGauge.addNativeData(METRIC_NATIVE_INSTANCE_KEY, metric);
                return bGauge;
            } else if (metric instanceof PolledGauge) {
                ArrayValue statisticConfigs = Utils.createBStatisticConfig(null);
                ObjectValue bGauge = BallerinaValues.createObjectValue(OBSERVE_PACKAGE_PATH, GAUGE, metricId.getName(),
                        metricId.getDescription(), getTags(metricId), statisticConfigs);
                bGauge.addNativeData(METRIC_NATIVE_INSTANCE_KEY, metric);
                return bGauge;
            }
        }

        return null;
    }

    private static MapValue<String, Object> getTags(MetricId metricId) {
        MapValue<String, Object> bTags = new MapValueImpl<>(new BMapType(BTypes.typeString));
        Set<Tag> tags = metricId.getTags();
        for (Tag tag : tags) {
            bTags.put(tag.getKey(), new BString(tag.getValue()));
        }
        return bTags;
    }
}
