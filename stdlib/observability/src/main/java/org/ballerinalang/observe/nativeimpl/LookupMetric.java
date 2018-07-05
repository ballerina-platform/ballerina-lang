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
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.metrics.Counter;
import org.ballerinalang.util.metrics.DefaultMetricRegistry;
import org.ballerinalang.util.metrics.Gauge;
import org.ballerinalang.util.metrics.Metric;
import org.ballerinalang.util.metrics.MetricId;
import org.ballerinalang.util.metrics.PolledGauge;
import org.ballerinalang.util.metrics.Tag;
import org.ballerinalang.util.metrics.Tags;

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
        String metricName = context.getStringArgument(0);
        BMap tags = (BMap) context.getNullableRefArgument(0);
        Map<String, String> tagMap = Utils.toStringMap(tags);
        Set<Tag> tagSet = new HashSet<>();
        Tags.tags(tagSet, tagMap);
        Metric metric = DefaultMetricRegistry.getInstance().lookup(new MetricId(metricName, "", tagSet));

        if (metric != null) {
            PackageInfo observePackage = context.getProgramFile().getPackageInfo(OBSERVE_PACKAGE_PATH);
            MetricId metricId = metric.getId();
            if (metric instanceof Counter) {
                StructureTypeInfo counterStructInfo = observePackage.getStructInfo(COUNTER);
                BMap counter = BLangVMStructs.createBStruct(counterStructInfo,
                        metricId.getName(), metricId.getDescription(), getTags(metricId));
                counter.addNativeData(METRIC_NATIVE_INSTANCE_KEY, metric);
                context.setReturnValues(counter);
            } else if (metric instanceof Gauge) {
                Gauge gauge = (Gauge) metric;
                StructureTypeInfo gaugeStructInfo = observePackage.getStructInfo(GAUGE);
                BMap bGauge = BLangVMStructs.createBStruct(gaugeStructInfo,
                        metricId.getName(), metricId.getDescription(), getTags(metricId),
                        Utils.createBStatisticConfig(gauge.getStatisticsConfig(), context));
                bGauge.addNativeData(METRIC_NATIVE_INSTANCE_KEY, metric);
                context.setReturnValues(bGauge);
            } else if (metric instanceof PolledGauge) {
                StructureTypeInfo gaugeStructInfo = observePackage.getStructInfo(GAUGE);
                BMap bGauge = BLangVMStructs.createBStruct(gaugeStructInfo,
                        metricId.getName(), metricId.getDescription(), getTags(metricId),
                        Utils.createBStatisticConfig(null, context));
                bGauge.addNativeData(METRIC_NATIVE_INSTANCE_KEY, metric);
            }
        } else {
            context.setReturnValues();
        }
    }

    private BMap<String, BString> getTags(MetricId metricId) {
        BMap<String, BString> bTags = new BMap<>(new BMapType(BTypes.typeString));
        Set<Tag> tags = metricId.getTags();
        for (Tag tag : tags) {
            bTags.put(tag.getKey(), new BString(tag.getValue()));
        }
        return bTags;
    }
}
