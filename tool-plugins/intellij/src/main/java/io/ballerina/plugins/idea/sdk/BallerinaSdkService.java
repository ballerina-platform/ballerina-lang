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

package io.ballerina.plugins.idea.sdk;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import io.ballerina.plugins.idea.BallerinaConstants;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.util.Set;

/**
 * Provides Ballerina SDK service.
 */
public abstract class BallerinaSdkService extends SimpleModificationTracker {

    public static final Logger LOG = Logger.getInstance(BallerinaSdkService.class);
    private static final Set<String> FEDORA_SUBDIRECTORIES = ContainerUtil.newHashSet("linux_amd64", "linux_386",
            "linux_arm");
    private static String ourTestSdkVersion;
    @NotNull
    protected final Project myProject;

    protected BallerinaSdkService(@NotNull Project project) {
        myProject = project;
    }

    public static BallerinaSdkService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, BallerinaSdkService.class);
    }

    @Nullable
    public abstract String getSdkHomePath(@Nullable Module module);

    @NotNull
    public static String libraryRootToSdkPath(@NotNull VirtualFile root) {
        return VfsUtilCore.urlToPath(StringUtil.trimEnd(StringUtil.trimEnd(StringUtil.trimEnd(root.getUrl(),
                "src/pkg"), "src"), "/"));
    }

    @Nullable
    public String getSdkVersion(@Nullable Module module) {
        return ourTestSdkVersion;
    }

    public abstract void chooseAndSetSdk(@Nullable Module module);

    /**
     * Use this method in order to check whether the method is appropriate for providing Ballerina-specific code
     * insight.
     */
    @Contract("null -> false")
    public boolean isBallerinaModule(@Nullable Module module) {
        return module != null && !module.isDisposed();
    }

    @Nullable
    public Configurable createSdkConfigurable() {
        return null;
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

    @TestOnly
    public static void setTestingSdkVersion(@Nullable String version, @NotNull Disposable disposable) {
        ourTestSdkVersion = version;
        Disposer.register(disposable, () -> {
            //noinspection AssignmentToStaticFieldFromInstanceMethod
            ourTestSdkVersion = null;
        });
    }
}
