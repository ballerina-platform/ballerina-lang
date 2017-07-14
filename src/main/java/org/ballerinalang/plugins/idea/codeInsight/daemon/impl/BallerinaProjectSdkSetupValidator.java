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

package org.ballerinalang.plugins.idea.codeInsight.daemon.impl;

import com.intellij.codeInsight.daemon.ProjectSdkSetupValidator;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.ballerinalang.plugins.idea.BallerinaFileType;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.sdk.BallerinaSdkService;
import org.ballerinalang.plugins.idea.sdk.BallerinaSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BallerinaProjectSdkSetupValidator implements ProjectSdkSetupValidator {

    public static final BallerinaProjectSdkSetupValidator INSTANCE = new BallerinaProjectSdkSetupValidator();

    @Override
    public boolean isApplicableFor(@NotNull Project project, @NotNull VirtualFile file) {
        if (file.getFileType() == BallerinaFileType.INSTANCE) {
            return true;
        }
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null) {
            return psiFile.getLanguage().isKindOf(BallerinaLanguage.INSTANCE);
        }
        return false;
    }

    @Nullable
    @Override
    public String getErrorMessage(@NotNull Project project, @NotNull VirtualFile file) {
        final Module module = ModuleUtilCore.findModuleForFile(file, project);
        if (module != null && !module.isDisposed()) {
            final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
            if (sdk == null) {
                if (ModuleRootManager.getInstance(module).isSdkInherited()) {
                    return ProjectBundle.message("project.sdk.not.defined");
                } else {
                    return ProjectBundle.message("module.sdk.not.defined");
                }
            } else {
                if (sdk.getSdkType() != BallerinaSdkType.getInstance()
                        && BallerinaSdkService.getInstance(project).isBallerinaModule(module)) {
                    return "Ballerina SDK is not defined for Ballerina Module '" + module.getName() + "'";
                }
            }
        }
        return null;
    }

    @Override
    public void doFix(@NotNull Project project, @NotNull VirtualFile file) {
        // Get the current project SDK.
        Sdk currentProjectSDK = ProjectRootManager.getInstance(project).getProjectSdk();
        if (currentProjectSDK != null) {
            // Get the current module.
            Module module = ModuleUtilCore.findModuleForFile(file, project);
            if (module != null) {
                // Get the new SDK which needs to be set to the module. Note that this will override the current
                // project SDK since there is no other way to get the SDK.
                Sdk newProjectSDK = ProjectSettingsService.getInstance(project).chooseAndSetSdk();
                // We set the module SDK.
                ModuleRootModificationUtil.setModuleSdk(module, newProjectSDK);
                // Update the project SDK to the original SDK.
                WriteAction.run(() -> ProjectRootManager.getInstance(project).setProjectSdk(currentProjectSDK));
            }
        } else {
            // If the current SDK is null, that means a project SDK is not set. So we choose and set the project SDK.
            Sdk selectedSDK = ProjectSettingsService.getInstance(project).chooseAndSetSdk();
            // If the selected SDK is not null, set it as the module SDK as well.
            if (selectedSDK != null) {
                final Module module = ModuleUtilCore.findModuleForFile(file, project);
                if (module != null) {
                    WriteAction.run(() -> ModuleRootModificationUtil.setSdkInherited(module));
                }
            }
        }
    }
}
