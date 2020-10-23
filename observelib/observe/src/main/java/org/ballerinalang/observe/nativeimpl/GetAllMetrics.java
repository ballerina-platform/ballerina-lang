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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.observability.metrics.Counter;
import io.ballerina.runtime.observability.metrics.DefaultMetricRegistry;
import io.ballerina.runtime.observability.metrics.Gauge;
import io.ballerina.runtime.observability.metrics.Metric;
import io.ballerina.runtime.observability.metrics.MetricConstants;
import io.ballerina.runtime.observability.metrics.MetricId;
import io.ballerina.runtime.observability.metrics.PolledGauge;
import io.ballerina.runtime.observability.metrics.Tag;

import java.util.Set;

/**
 * This is the getAllMetrics function native implementation for the registered metrics.
 * This can be used by the metric reporters to report the metrics.
 *
 * @since 0.980.0
 */

public class GetAllMetrics {

    private static final Type METRIC_TYPE = ValueCreator
            .createRecordValue(ObserveNativeImplConstants.OBSERVE_PACKAGE_ID, ObserveNativeImplConstants.METRIC)
            .getType();

    public static BArray getAllMetrics() {
        Metric[] metrics = DefaultMetricRegistry.getInstance().getAllMetrics();

        BArray bMetrics = ValueCreator.createArrayValue(TypeCreator.createArrayType(METRIC_TYPE));
        int metricIndex = 0;
        for (Metric metric : metrics) {
            MetricId metricId = metric.getId();
            Object metricValue = null;
            String metricType = null;
            BArray summary = null;
            if (metric instanceof Counter) {
                metricValue = ((Counter) metric).getValue();
                metricType = MetricConstants.COUNTER;
            } else if (metric instanceof Gauge) {
                Gauge gauge = (Gauge) metric;
                metricValue = gauge.getValue();
                metricType = MetricConstants.GAUGE;
                summary = Utils.createBSnapshots(gauge.getSnapshots());
            } else if (metric instanceof PolledGauge) {
                PolledGauge gauge = (PolledGauge) metric;
                metricValue = gauge.getValue();
                metricType = MetricConstants.GAUGE;
            }
            if (metricValue != null) {
                BMap<BString, Object> metricStruct = ValueCreator.createRecordValue(
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

    private static BMap<BString, Object> getTags(MetricId metricId) {
        BMap<BString, Object> bTags = ValueCreator.createMapValue(TypeCreator.createMapType(
                PredefinedTypes.TYPE_STRING));
        Set<Tag> tags = metricId.getTags();
        for (Tag tag : tags) {
            bTags.put(StringUtils.fromString(tag.getKey()), StringUtils.fromString(tag.getValue()));
        }
        return bTags;
    }

}
