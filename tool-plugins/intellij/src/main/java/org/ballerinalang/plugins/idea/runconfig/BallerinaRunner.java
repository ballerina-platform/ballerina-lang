/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.plugins.idea.runconfig;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import org.ballerinalang.plugins.idea.runconfig.application.BallerinaApplicationConfiguration;
import org.ballerinalang.plugins.idea.runconfig.test.BallerinaTestConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Provides Ballerina run capability.
 */
public class BallerinaRunner extends DefaultProgramRunner {

    private static final String ID = "BallerinaRunner";

    @NotNull
    @Override
    public String getRunnerId() {
        return ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        // We don't need to run Ballerina Remote configuration, we only debug using it.
        return DefaultRunExecutor.EXECUTOR_ID.equals(executorId)
                && (profile instanceof BallerinaApplicationConfiguration
                || profile instanceof BallerinaTestConfiguration);
    }
}
