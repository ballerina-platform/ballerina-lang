/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina;

import org.ballerinalang.langserver.commons.registration.BallerinaClientCapability;

/**
 * Client capabilities for the performance analyzer service.
 *
 * @since 2.0.0
 */
public class PerformanceAnalyzerClientCapabilities extends BallerinaClientCapability {

    private boolean getEndpoints;
    private boolean getGraphData;
    private boolean getRealtimeData;

    public boolean isGetEndpoints() {

        return getEndpoints;
    }

    public void setGetEndpoints(boolean getEndpoints) {

        this.getEndpoints = getEndpoints;
    }

    public boolean isGetGraphData() {

        return getGraphData;
    }

    public void setGetGraphData(boolean getGraphData) {

        this.getGraphData = getGraphData;
    }

    public boolean isGetRealtimeData() {

        return getRealtimeData;
    }

    public void setGetRealtimeData(boolean getRealtimeData) {

        this.getRealtimeData = getRealtimeData;
    }

    public PerformanceAnalyzerClientCapabilities() {

        super(Constants.CAPABILITY_NAME);
    }
}
