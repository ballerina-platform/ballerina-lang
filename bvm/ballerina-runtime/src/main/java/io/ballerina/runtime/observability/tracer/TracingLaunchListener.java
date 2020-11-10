/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package io.ballerina.runtime.observability.tracer;

import io.ballerina.runtime.launch.LaunchListener;
import io.ballerina.runtime.observability.ObserveUtils;
import org.ballerinalang.config.ConfigRegistry;

import static io.ballerina.runtime.observability.ObservabilityConstants.CONFIG_TRACING_ENABLED;

/**
 * Listen to Launcher events and initialize Tracing.
 */
public class TracingLaunchListener implements LaunchListener {

    @Override
    public void beforeRunProgram(boolean service) {
        if (!TracersStore.getInstance().isInitialized()) {
            ConfigRegistry configRegistry = ConfigRegistry.getInstance();
            if (configRegistry.getAsBoolean(CONFIG_TRACING_ENABLED)) {
                ObserveUtils.addObserver(new BallerinaTracingObserver());
                TracersStore.getInstance().loadTracers();
            }
        }
    }

    @Override
    public void afterRunProgram(boolean service) {
    }
}
