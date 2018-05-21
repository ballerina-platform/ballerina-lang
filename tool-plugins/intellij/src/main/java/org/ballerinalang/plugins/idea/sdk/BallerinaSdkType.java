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

package org.ballerinalang.plugins.idea.sdk;

import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VirtualFile;
import org.ballerinalang.plugins.idea.BallerinaIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import javax.swing.Icon;

/**
 * Represent a Ballerina SDK.
 */
public class BallerinaSdkType extends SdkType {

    public BallerinaSdkType() {
        super("Ballerina SDK");
    }

    @NotNull
    public static BallerinaSdkType getInstance() {
        return SdkType.findInstance(BallerinaSdkType.class);
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return BallerinaIcons.ICON;
    }

    @NotNull
    @Override
    public Icon getIconForAddAction() {
        return getIcon();
    }

    @Nullable
    @Override
    public String suggestHomePath() {
        VirtualFile suggestSdkDirectory = BallerinaSdkUtil.suggestSdkDirectory();
        return suggestSdkDirectory != null ? suggestSdkDirectory.getPath() : null;
    }

    @Override
    public boolean isValidSdkHome(@NotNull String path) {
        BallerinaSdkService.LOG.debug("Validating sdk path: " + path);
        String executablePath = BallerinaSdkService.getBallerinaExecutablePath(path);
        if (executablePath == null) {
            BallerinaSdkService.LOG.debug("Ballerina executable is not found: ");
            return false;
        }
        if (!new File(executablePath).canExecute()) {
            BallerinaSdkService.LOG.debug("Ballerina binary cannot be executed: " + path);
            return false;
        }
        if (getVersionString(path) != null) {
            BallerinaSdkService.LOG.debug("Cannot retrieve version for sdk: " + path);
            return true;
        }
        return false;
    }

    @NotNull
    @Override
    public String adjustSelectedSdkHome(@NotNull String homePath) {
        return "";
    }

    @NotNull
    @Override
    public String suggestSdkName(@Nullable String currentSdkName, @NotNull String sdkHome) {
        String version = getVersionString(sdkHome);
        if (version == null) {
            return "Unknown Ballerina version at " + sdkHome;
        }
        return "Ballerina " + version;
    }

    @Nullable
    @Override
    public String getVersionString(@NotNull String sdkHome) {
        return BallerinaSdkUtil.retrieveBallerinaVersion(sdkHome);
    }

    @Nullable
    @Override
    public String getDefaultDocumentationUrl(@NotNull Sdk sdk) {
        return null;
    }

    @Nullable
    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull
            SdkModificator sdkModificator) {
        return null;
    }

    @Override
    public void saveAdditionalData(@NotNull SdkAdditionalData additionalData, @NotNull Element additional) {
    }

    @NotNull
    @NonNls
    @Override
    public String getPresentableName() {
        return "Ballerina SDK";
    }

    @Override
    public void setupSdkPaths(@NotNull Sdk sdk) {
        String versionString = sdk.getVersionString();
        if (versionString == null) {
            throw new RuntimeException("SDK version is not defined");
        }
        SdkModificator modificator = sdk.getSdkModificator();
        String path = sdk.getHomePath();
        if (path == null) {
            return;
        }
        modificator.setHomePath(path);

        for (VirtualFile file : BallerinaSdkUtil.getSdkDirectoriesToAttach(path, versionString)) {
            modificator.addRoot(file, OrderRootType.CLASSES);
            modificator.addRoot(file, OrderRootType.SOURCES);
        }
        modificator.commitChanges();
    }
}
