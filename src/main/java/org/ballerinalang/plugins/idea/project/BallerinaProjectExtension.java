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

package org.ballerinalang.plugins.idea.project;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.ProjectExtension;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BallerinaProjectExtension extends ProjectExtension {

    @Override
    public void projectSdkChanged(@Nullable Sdk sdk) {
        super.projectSdkChanged(sdk);
        Project project = ProjectManager.getInstance().getDefaultProject();
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            if (module != null) {
                WriteAction.run(() -> ModuleRootModificationUtil.setSdkInherited(module));
            }
        }
    }

    @Override
    public void readExternal(@NotNull Element element) {

    }

    @Override
    public void writeExternal(@NotNull Element element) {

    }
}
