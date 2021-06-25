/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.telemetry;

import org.ballerinalang.langserver.common.utils.CommonUtil;

/**
 * Represents a telemetry event sent to gather feature usage statistics.
 *
 * @since 2.0.0
 */
public class LSFeatureUsageTelemetryEvent extends LSTelemetryEvent {

    private final String featureName;
    private final String featureClass;
    private final String featureMessage;

    protected LSFeatureUsageTelemetryEvent(String component, String version, String featureName, String featureClass,
                                           String featureMessage) {
        super(LSTelemetryEvent.TYPE_FEATURE_USAGE_EVENT, component, version);
        this.featureName = featureName;
        this.featureClass = featureClass;
        this.featureMessage = featureMessage;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getFeatureClass() {
        return featureClass;
    }

    /**
     * Get the title (which was visible to the user) of the feature.
     *
     * @return Title of the feature
     */
    public String getFeatureMessage() {
        return featureMessage;
    }

    public static LSFeatureUsageTelemetryEvent from(String featureName, String featureClass, String featureMessage) {
        return new LSFeatureUsageTelemetryEvent(LS_TELEMETRY_COMPONENT_NAME, CommonUtil.SDK_VERSION, featureName,
                featureClass, featureMessage);
    }
}
