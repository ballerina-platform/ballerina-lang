/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.task;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleModuleContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType;
import org.wso2.ballerinalang.compiler.SourceDirectoryManager;

import java.io.IOException;
import java.nio.file.Path;

import static org.ballerinalang.packerina.utils.FileUtils.deleteDirectory;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Cleans up the target directory.
 */
public class CleanTargetDirTask implements Task {
    @Override
    public void execute(BuildContext buildContext) {
        Path targetDir = buildContext.get(BuildContextField.TARGET_DIR);
        try {
            // Deletes only the respective module's target resources if a single module is build.
            if (buildContext.getSourceType() == SourceType.SINGLE_MODULE) {
                SourceDirectoryManager sourceDirectoryManager = SourceDirectoryManager
                        .getInstance(buildContext.get(BuildContextField.COMPILER_CONTEXT));
                SingleModuleContext moduleContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                PackageID packageID = sourceDirectoryManager.getPackageID(moduleContext.getModuleName());
                deleteResource(buildContext.getExecutablePathFromTarget(packageID));
                deleteResource(buildContext.getBaloFromTarget(packageID));
                deleteResource(buildContext.getBirPathFromTargetCache(packageID));
                deleteResource(buildContext.getTestBirPathFromTargetCache(packageID));
                deleteResource(buildContext.getJarPathFromTargetCache(packageID));
                deleteResource(buildContext.getTestJarPathFromTargetCache(packageID));
            } else {
                deleteResource(targetDir);
            }
        } catch (IOException e) {
            throw createLauncherException("Unable to clean target : " + targetDir.toString() + "\n", e);
        }
    }

    private void deleteResource(Path path) throws IOException {
        if (path != null && path.toFile().exists()) {
            deleteDirectory(path);
        }
    }
}
