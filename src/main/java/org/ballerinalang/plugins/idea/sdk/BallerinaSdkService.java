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

package org.ballerinalang.plugins.idea.sdk;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.containers.ContainerUtil;
import org.ballerinalang.plugins.idea.BallerinaConstants;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Set;

public class BallerinaSdkService {

    public static final Logger LOG = Logger.getInstance(BallerinaSdkService.class);
    private static final Set<String> FEDORA_SUBDIRECTORIES = ContainerUtil.newHashSet("linux_amd64", "linux_386",
            "linux_arm");

    public static BallerinaSdkService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, BallerinaSdkService.class);
    }

    public static String getBallerinaExecutablePath(@Nullable String sdkHomePath) {
        if (sdkHomePath != null) {
            File binDirectory = new File(sdkHomePath, "bin");
            if (!binDirectory.exists() && SystemInfo.isLinux) {
                LOG.debug(sdkHomePath + "/bin doesn't exist, checking linux-specific paths");
                File ballerinaFromPath = PathEnvironmentVariableUtil.findInPath(
                        BallerinaConstants.BALLERINA_EXECUTABLE_NAME);
                if (ballerinaFromPath != null && ballerinaFromPath.exists()) {
                    LOG.debug("Ballerina executable found at " + ballerinaFromPath.getAbsolutePath());
                    return ballerinaFromPath.getAbsolutePath();
                }
            }

            String executableName = BallerinaEnvironmentUtil.getBinaryFileNameForPath(
                    BallerinaConstants.BALLERINA_EXECUTABLE_NAME);
            String executable = FileUtil.join(sdkHomePath, "bin", executableName);
            if (!new File(executable).exists() && SystemInfo.isLinux) {
                LOG.debug(executable + " doesn't exists. Looking for binaries in fedora-specific directories");
                // fedora
                for (String directory : FEDORA_SUBDIRECTORIES) {
                    File file = new File(binDirectory, directory);
                    if (file.exists() && file.isDirectory()) {
                        LOG.debug("Ballerina executable found at " + file.getAbsolutePath());
                        return FileUtil.join(file.getAbsolutePath(), executableName);
                    }
                }
            }
            LOG.debug("Ballerina executable found at " + executable);
            return executable;
        }
        return null;
    }

    @Contract("null -> false")
    public boolean isGoModule(@Nullable Module module) {
        return module != null && !module.isDisposed();
    }

}
