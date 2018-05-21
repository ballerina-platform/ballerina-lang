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
 *
 */

package org.ballerinalang.plugins.idea.project;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import org.ballerinalang.plugins.idea.BallerinaConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Represents Ballerina project library service.
 */
@State(
        name = BallerinaConstants.BALLERINA_LIBRARIES_SERVICE_NAME,
        storages = {
                @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
                @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/" +
                        BallerinaConstants.BALLERINA_LIBRARIES_CONFIG_FILE, scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class BallerinaProjectLibrariesService extends BallerinaLibrariesService<BallerinaLibraryState> {
    public static BallerinaProjectLibrariesService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, BallerinaProjectLibrariesService.class);
    }
}
