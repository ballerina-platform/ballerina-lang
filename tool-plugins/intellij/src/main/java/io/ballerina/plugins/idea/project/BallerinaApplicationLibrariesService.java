/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import io.ballerina.plugins.idea.BallerinaConstants;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Provides Ballerina application library services.
 */
@State(
        name = BallerinaConstants.BALLERINA_LIBRARIES_SERVICE_NAME,
        storages = @Storage( value = BallerinaConstants.BALLERINA_LIBRARIES_CONFIG_FILE)
)
public class BallerinaApplicationLibrariesService extends
        BallerinaLibrariesService<BallerinaApplicationLibrariesService.BallerinaApplicationLibrariesState> {

    @NotNull
    @Override
    protected BallerinaApplicationLibrariesState createState() {
        return new BallerinaApplicationLibrariesState();
    }

    public static BallerinaApplicationLibrariesService getInstance() {
        return ServiceManager.getService(BallerinaApplicationLibrariesService.class);
    }

    public boolean isUseBallerinaPathFromSystemEnvironment() {
        return myState.isUseBallerinaPathFromSystemEnvironment();
    }

    public void setUseBallerinaPathFromSystemEnvironment(boolean useBallerinaPathFromSystemEnvironment) {
        if (myState.isUseBallerinaPathFromSystemEnvironment() != useBallerinaPathFromSystemEnvironment) {
            myState.setUseBallerinaPathFromSystemEnvironment(useBallerinaPathFromSystemEnvironment);
            if (!BallerinaSdkUtil.getBallerinaPathsRootsFromEnvironment().isEmpty()) {
                incModificationCount();
                ApplicationManager.getApplication().getMessageBus().syncPublisher(LIBRARIES_TOPIC)
                        .librariesChanged(getLibraryRootUrls());
            }
        }
    }

    /**
     * Represents Ballerina application library state.
     */
    public static class BallerinaApplicationLibrariesState extends BallerinaLibraryState {

        private boolean myUseBallerinaPathFromSystemEnvironment = true;

        public boolean isUseBallerinaPathFromSystemEnvironment() {
            return myUseBallerinaPathFromSystemEnvironment;
        }

        public void setUseBallerinaPathFromSystemEnvironment(boolean useBallerinaPathFromSystemEnvironment) {
            myUseBallerinaPathFromSystemEnvironment = useBallerinaPathFromSystemEnvironment;
        }
    }
}
