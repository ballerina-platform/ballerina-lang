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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.observability.ObserveUtils;
import io.ballerina.runtime.observability.ObserverContext;
import io.ballerina.runtime.observability.metrics.Tag;
import org.ballerinalang.config.ConfigRegistry;

import java.util.HashMap;

import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;

/**
 * This function add tags to System Metrics.
 * Custom tag is not included in the 'in progress-requests'
 */
public class AddTagToMetrics {

    public static Object addTagToMetrics(Environment env, BString tagKey, BString tagValue) {

        boolean enabled = ConfigRegistry.getInstance().getAsBoolean(CONFIG_TRACING_ENABLED);
        if (!enabled) {
            return null;
        }

        ObserverContext observerContext = ObserveUtils.getObserverContextOfCurrentFrame(env);
        if (observerContext != null) {

            if (observerContext.customMetricTags == null) {
                observerContext.customMetricTags = new HashMap<>();
            }
            observerContext.customMetricTags.put(tagKey.getValue(), Tag.of(tagKey.getValue(), tagValue.getValue()));
            return null;
        }
        return ErrorCreator.createError(
                StringUtils.fromString("Can not add tag {" + tagKey + ":" + tagValue + "}"));
    }
}
