/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.observability.ObserveUtils;
import io.ballerina.runtime.observability.ObserverContext;
import io.ballerina.runtime.observability.metrics.BallerinaMetricsObserver;
import io.ballerina.runtime.observability.metrics.Tag;
import io.ballerina.runtime.observability.tracer.TraceConstants;
import io.ballerina.runtime.scheduling.Scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * This function adds tags to a span.
 */
public class AddTagToMetrics {
    public static Object addTagToMetrics(BString tagKey, BString tagValue) {
        Optional<ObserverContext> observer = ObserveUtils.getObserverContextOfCurrentFrame(Scheduler.getStrand());
        ObserverContext observerContext;
        if (observer.isPresent()) {
            observerContext = observer.get();
            Map<String, Tag> customTags = observerContext.getProperty(BallerinaMetricsObserver.PROPERTY_CUSTOM_TAGS) != null ?
                    (Map<String, Tag>) observerContext.getProperty(BallerinaMetricsObserver.PROPERTY_CUSTOM_TAGS) :
                    new HashMap<String, Tag>();
            customTags.put(tagKey.getValue(), Tag.of(tagKey.getValue(), tagValue.getValue()));
            observerContext.addProperty(BallerinaMetricsObserver.PROPERTY_CUSTOM_TAGS,customTags);
            return null;
        }
        return ErrorCreator.createError(
                StringUtils.fromString(("Can not add tag {" + tagKey + ":" + tagValue + "}")));
    }
}
