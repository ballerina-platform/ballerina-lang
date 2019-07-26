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
import org.ballerinalang.jvm.observability.metrics.Counter;
import org.ballerinalang.jvm.observability.metrics.DefaultMetricRegistry;
import org.ballerinalang.jvm.observability.metrics.Gauge;
import org.ballerinalang.jvm.observability.metrics.Metric;
import org.ballerinalang.jvm.observability.metrics.MetricConstants;
import org.ballerinalang.jvm.observability.metrics.MetricId;
import org.ballerinalang.jvm.observability.metrics.PolledGauge;
import org.ballerinalang.jvm.observability.metrics.Tag;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Set;

/**
 * This is the getAllMetrics function native implementation for the registered metrics.
 * This can be used by the metric reporters to report the metrics.
 *
 * @since 0.980.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "observe",
        functionName = "getAllMetrics",
        returnType = @ReturnType(type = TypeKind.ARRAY),
        isPublic = true
)
public class GetAllMetrics extends BlockingNativeCallableUnit {

    private static final BType METRIC_TYPE = BallerinaValues
            .createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_PATH, ObserveNativeImplConstants.METRIC)
            .getType();
    
    @Override
    public void execute(Context context) {
//        Metric[] metrics = DefaultMetricRegistry.getInstance().getAllMetrics();
//        PackageInfo observePackage = context.getProgramFile().getPackageInfo(OBSERVE_PACKAGE_PATH);
//        StructureTypeInfo metricStructInfo = observePackage.getStructInfo(METRIC);
//
//        BValueArray bMetrics = new BValueArray(observePackage.getTypeInfo(METRIC).getType());
//        int metricIndex = 0;
//        for (Metric metric : metrics) {
//            MetricId metricId = metric.getId();
//            BValue metricValue = null;
//            String metricType = null;
//            BValueArray summary = null;
//            if (metric instanceof Counter) {
//                metricValue = new BInteger(((Counter) metric).getValue());
//                metricType = MetricConstants.COUNTER;
//            } else if (metric instanceof Gauge) {
//                Gauge gauge = (Gauge) metric;
//                metricValue = new BFloat(gauge.getValue());
//                metricType = MetricConstants.GAUGE;
//                summary = Utils.createBSnapshots(gauge.getSnapshots(), context);
//            } else if (metric instanceof PolledGauge) {
//                PolledGauge gauge = (PolledGauge) metric;
//                metricValue = new BFloat(gauge.getValue());
//                metricType = MetricConstants.GAUGE;
//            }
//            if (metricValue != null) {
//                BMap metricStruct = BLangVMStructs.createBStruct(metricStructInfo,
//                        metricId.getName(), metricId.getDescription(), getTags(metricId), metricType,
//                        metricValue, summary);
//                bMetrics.add(metricIndex, metricStruct);
//                metricIndex++;
//            }
//        }
//        context.setReturnValues(bMetrics);
    }

    public static ArrayValue getAllMetrics(Strand strand) {
        Metric[] metrics = DefaultMetricRegistry.getInstance().getAllMetrics();

        ArrayValue bMetrics = new ArrayValue(new BArrayType(METRIC_TYPE));
        int metricIndex = 0;
        for (Metric metric : metrics) {
            MetricId metricId = metric.getId();
            Object metricValue = null;
            String metricType = null;
            ArrayValue summary = null;
            if (metric instanceof Counter) {
                metricValue = ((Counter) metric).getValue();
                metricType = MetricConstants.COUNTER;
            } else if (metric instanceof Gauge) {
                Gauge gauge = (Gauge) metric;
                metricValue = gauge.getValue();
                metricType = MetricConstants.GAUGE;
                summary = Utils.createBSnapshots(gauge.getSnapshots(), strand);
            } else if (metric instanceof PolledGauge) {
                PolledGauge gauge = (PolledGauge) metric;
                metricValue = gauge.getValue();
                metricType = MetricConstants.GAUGE;
            }
            if (metricValue != null) {
                MapValue<String, Object> metricStruct = BallerinaValues.createRecordValue(
                        ObserveNativeImplConstants.OBSERVE_PACKAGE_PATH, ObserveNativeImplConstants.METRIC);
                metricStruct.put("name", metricId.getName());
                metricStruct.put("desc", metricId.getDescription());
                metricStruct.put("tags", getTags(metricId));
                metricStruct.put("metricType", metricType);
                metricStruct.put("value", metricValue);
                metricStruct.put("summary", summary);
                bMetrics.add(metricIndex, metricStruct);
                metricIndex++;
            }
        }

        return bMetrics;
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
