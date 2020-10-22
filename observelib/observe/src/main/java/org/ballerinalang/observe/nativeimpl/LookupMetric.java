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
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.observability.metrics.Counter;
import io.ballerina.runtime.observability.metrics.DefaultMetricRegistry;
import io.ballerina.runtime.observability.metrics.Gauge;
import io.ballerina.runtime.observability.metrics.Metric;
import io.ballerina.runtime.observability.metrics.MetricId;
import io.ballerina.runtime.observability.metrics.PolledGauge;
import io.ballerina.runtime.observability.metrics.Tag;
import io.ballerina.runtime.observability.metrics.Tags;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.observe.nativeimpl.ObserveNativeImplConstants.COUNTER;
import static org.ballerinalang.observe.nativeimpl.ObserveNativeImplConstants.GAUGE;
import static org.ballerinalang.observe.nativeimpl.ObserveNativeImplConstants.METRIC_NATIVE_INSTANCE_KEY;
import static org.ballerinalang.observe.nativeimpl.ObserveNativeImplConstants.OBSERVE_PACKAGE_ID;

/**
 * This is the lookupMetric function native implementation for the registered metrics.
 *
 * @since 0.980.0
 */

public class LookupMetric {

    public static Object lookupMetric(BString metricName, Object tags) {

        Map<String, String> tagMap = Utils.toStringMap((BMap<BString, ?>) tags);
        Set<Tag> tagSet = new HashSet<>();
        Tags.tags(tagSet, tagMap);
        Metric metric = DefaultMetricRegistry.getInstance().lookup(new MetricId(metricName.getValue(), "", tagSet));

        if (metric != null) {
            MetricId metricId = metric.getId();
            if (metric instanceof Counter) {
                BObject counter = ValueCreator.createObjectValue(
                        OBSERVE_PACKAGE_ID, COUNTER, StringUtils.fromString(metricId.getName()),
                        StringUtils.fromString(metricId.getDescription()), getTags(metricId));
                counter.addNativeData(METRIC_NATIVE_INSTANCE_KEY, metric);
                return counter;
            } else if (metric instanceof Gauge) {
                Gauge gauge = (Gauge) metric;
                BArray statisticConfigs = Utils.createBStatisticConfig(gauge.getStatisticsConfig());
                BObject bGauge = ValueCreator.createObjectValue(
                        OBSERVE_PACKAGE_ID, GAUGE, StringUtils.fromString(metricId.getName()),
                        StringUtils.fromString(metricId.getDescription()), getTags(metricId), statisticConfigs);
                bGauge.addNativeData(METRIC_NATIVE_INSTANCE_KEY, metric);
                return bGauge;
            } else if (metric instanceof PolledGauge) {
                BArray statisticConfigs = Utils.createBStatisticConfig(null);
                BObject bGauge = ValueCreator.createObjectValue(
                        OBSERVE_PACKAGE_ID, GAUGE, StringUtils.fromString(metricId.getName()),
                        StringUtils.fromString(metricId.getDescription()), getTags(metricId), statisticConfigs);
                bGauge.addNativeData(METRIC_NATIVE_INSTANCE_KEY, metric);
                return bGauge;
            }
        }

        return null;
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
