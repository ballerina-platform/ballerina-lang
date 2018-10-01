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

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.ProjectExtension;
import com.intellij.openapi.util.AsyncResult;
import io.ballerina.plugins.idea.BallerinaExternalAnnotator;
import io.ballerina.plugins.idea.preloading.BallerinaLanguageServerPreloadingActivity;
import io.ballerina.plugins.idea.sdk.BallerinaSdkService;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides the capability to update module SDKs when the project SDK is changed.
 */
public class BallerinaProjectExtension extends ProjectExtension {

    @Override
    public void projectSdkChanged(@Nullable Sdk sdk) {
        // First we get the date context from the focus (current project).
        AsyncResult<DataContext> dataContextFromFocus = DataManager.getInstance().getDataContextFromFocus();
        // Get the result from the data context.
        DataContext result = dataContextFromFocus.getResult();
        if (result == null) {
            return;
        }
        // Get the current project.
        Project project = DataKeys.PROJECT.getData(result);
        if (project == null) {
            return;
        }
        // Get all modules in the project.
        Module[] modules = ModuleManager.getInstance(project).getModules();
        // Iterate through all modules.
        for (Module module : modules) {
            // Check whether the module is a Ballerina module.
            if (BallerinaSdkService.getInstance(project).isBallerinaModule(module)) {
                // If it is a Ballerina module, inherit the project SDK.
                WriteAction.run(() -> ModuleRootModificationUtil.setSdkInherited(module));
            }
        }
        // Reset Ballerina external annotator.
        BallerinaExternalAnnotator.reset();

        //Register language server definition for the new SDK
        BallerinaLanguageServerPreloadingActivity langServerLoader = new BallerinaLanguageServerPreloadingActivity();
        langServerLoader.registerServerDefinition(project);
    }

    @Override
    public void readExternal(@NotNull Element element) {
        // We don't have any use of this method at the moment.
    }

    @Override
    public void writeExternal(@NotNull Element element) {
        // We don't have any use of this method at the moment.
    }
}
