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
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.observe.Utils;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.metrics.Constants;
import org.ballerinalang.util.metrics.Counter;
import org.ballerinalang.util.metrics.DefaultMetricRegistry;
import org.ballerinalang.util.metrics.Gauge;
import org.ballerinalang.util.metrics.Metric;
import org.ballerinalang.util.metrics.MetricId;
import org.ballerinalang.util.metrics.PolledGauge;
import org.ballerinalang.util.metrics.Tag;

import java.util.Set;

import static org.ballerinalang.nativeimpl.observe.Constants.METRIC;
import static org.ballerinalang.nativeimpl.observe.Constants.OBSERVE_PACKAGE_PATH;

/**
 * This is the getAllMetrics function native implementation for the registered metrics.
 * This can be used by the metric reporters to report the metrics.
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe",
        functionName = "getAllMetrics",
        returnType = @ReturnType(type = TypeKind.ARRAY),
        isPublic = true
)
public class GetAllMetrics extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        Metric[] metrics = DefaultMetricRegistry.getInstance().getAllMetrics();
        PackageInfo observePackage = context.getProgramFile().getPackageInfo(OBSERVE_PACKAGE_PATH);
        StructureTypeInfo metricStructInfo = observePackage.getStructInfo(METRIC);

        BRefValueArray bMetrics = new BRefValueArray(observePackage.getTypeInfo(METRIC).getType());
        int metricIndex = 0;
        for (Metric metric : metrics) {
            MetricId metricId = metric.getId();
            BValue metricValue = null;
            String metricType = null;
            BRefValueArray summary = null;
            if (metric instanceof Counter) {
                metricValue = new BInteger(((Counter) metric).getValue());
                metricType = Constants.COUNTER;
            } else if (metric instanceof Gauge) {
                Gauge gauge = (Gauge) metric;
                metricValue = new BFloat(gauge.getValue());
                metricType = Constants.GAUGE;
                summary = Utils.createBSnapshots(gauge.getSnapshots(), context);
            } else if (metric instanceof PolledGauge) {
                PolledGauge gauge = (PolledGauge) metric;
                metricValue = new BFloat(gauge.getValue());
                metricType = Constants.GAUGE;
            }
            if (metricValue != null) {
                BStruct metricStruct = BLangVMStructs.createBStruct(metricStructInfo,
                        metricId.getName(), metricId.getDescription(), getTags(metricId), metricType,
                        metricValue, summary);
                bMetrics.add(metricIndex, metricStruct);
                metricIndex++;
            }
        }
        context.setReturnValues(bMetrics);
    }

    private BMap<String, BString> getTags(MetricId metricId) {
        BMap<String, BString> bTags = new BMap<>();
        Set<Tag> tags = metricId.getTags();
        for (Tag tag : tags) {
            bTags.put(tag.getKey(), new BString(tag.getValue()));
        }
        return bTags;
    }
}
