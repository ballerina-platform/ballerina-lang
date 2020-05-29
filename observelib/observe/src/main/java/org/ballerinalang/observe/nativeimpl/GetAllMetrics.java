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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.StringUtils;
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
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
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
        packageName = "observe", version = "0.8.0",
        functionName = "getAllMetrics",
        returnType = @ReturnType(type = TypeKind.ARRAY),
        isPublic = true
)
public class GetAllMetrics {

    private static final BType METRIC_TYPE = BallerinaValues
            .createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_ID, ObserveNativeImplConstants.METRIC)
            .getType();

    public static ArrayValue getAllMetrics(Strand strand) {
        Metric[] metrics = DefaultMetricRegistry.getInstance().getAllMetrics();

        ArrayValue bMetrics = new ArrayValueImpl(new BArrayType(METRIC_TYPE));
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
                MapValue<BString, Object> metricStruct = BallerinaValues.createRecordValue(
                        ObserveNativeImplConstants.OBSERVE_PACKAGE_ID, ObserveNativeImplConstants.METRIC);
                metricStruct.put(StringUtils.fromString("name"), StringUtils.fromString(metricId.getName()));
                metricStruct.put(StringUtils.fromString("desc"), StringUtils.fromString(metricId.getDescription()));
                metricStruct.put(StringUtils.fromString("tags"), getTags(metricId));
                metricStruct.put(StringUtils.fromString("metricType"), StringUtils.fromString(metricType));
                metricStruct.put(StringUtils.fromString("value"), metricValue);
                metricStruct.put(StringUtils.fromString("summary"), summary);
                bMetrics.add(metricIndex, metricStruct);
                metricIndex++;
            }
        }

        return bMetrics;
    }

    private static MapValue<BString, Object> getTags(MetricId metricId) {
        MapValue<BString, Object> bTags = new MapValueImpl<>(new BMapType(BTypes.typeString));
        Set<Tag> tags = metricId.getTags();
        for (Tag tag : tags) {
            bTags.put(StringUtils.fromString(tag.getKey()), StringUtils.fromString(tag.getValue()));
        }
        return bTags;
    }

}
