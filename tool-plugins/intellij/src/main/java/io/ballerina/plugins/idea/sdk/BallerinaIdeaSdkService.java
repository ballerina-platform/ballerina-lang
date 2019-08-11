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

import com.intellij.ProjectTopics;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ObjectUtils;
import io.ballerina.plugins.idea.BallerinaConstants;
import io.ballerina.plugins.idea.BallerinaModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides Ballerina SDK service.
 */
public class BallerinaIdeaSdkService extends BallerinaSdkService {

    public BallerinaIdeaSdkService(@NotNull Project project) {
        super(project);
        myProject.getMessageBus().connect(project).subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
            @Override
            public void rootsChanged(ModuleRootEvent event) {
                incModificationCount();
            }
        });
    }

    @Override
    public String getSdkHomePath(@Nullable Module module) {
        ComponentManager holder = ObjectUtils.notNull(module, myProject);
        return CachedValuesManager.getManager(myProject).getCachedValue(holder, () -> {
            Sdk sdk = getBallerinaSdk(module);
            return CachedValueProvider.Result.create(sdk != null ? sdk.getHomePath() : null, this);
        });
    }

    @Nullable
    @Override
    public String getSdkVersion(@Nullable Module module) {
        String parentVersion = super.getSdkVersion(module);
        if (parentVersion != null) {
            return parentVersion;
        }

        ComponentManager holder = ObjectUtils.notNull(module, myProject);
        return CachedValuesManager.getManager(myProject).getCachedValue(holder, () -> {
            Sdk sdk = getBallerinaSdk(module);
            return CachedValueProvider.Result.create(sdk != null ? sdk.getVersionString() : null, this);
        });
    }

    @Override
    public void chooseAndSetSdk(@Nullable Module module) {
        Sdk projectSdk = ProjectSettingsService.getInstance(myProject).chooseAndSetSdk();
        if (projectSdk == null && module != null) {
            ApplicationManager.getApplication().runWriteAction(() -> {
                if (!module.isDisposed()) {
                    ModuleRootModificationUtil.setSdkInherited(module);
                }
            });
        }
        // Need to prompt a restart action to clear and re initiate language server instance from the new SDK.
        // Todo - Figure out a way to apply language server changes without restarting.
        if (isBallerinaSdk(projectSdk)) {
            BallerinaSdkUtils.showRestartDialog(myProject);
        }
    }

    @Override
    public boolean isBallerinaModule(@Nullable Module module) {
        return super.isBallerinaModule(module) && ModuleType.get(module) == BallerinaModuleType.getInstance();
    }

    @Nullable
    public Configurable createSdkConfigurable() {
        return null;
    }

    private Sdk getBallerinaSdk(@Nullable Module module) {
        if (module != null) {
            Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
            if (sdk != null && sdk.getSdkType() instanceof BallerinaSdkType) {
                return sdk;
            }
        }
        Sdk sdk = ProjectRootManager.getInstance(myProject).getProjectSdk();
        return sdk != null && sdk.getSdkType() instanceof BallerinaSdkType ? sdk : null;
    }

    private boolean isBallerinaSdk(Sdk sdk) {
        return sdk != null && BallerinaConstants.BALLERINA_SDK_TYPE.equals(sdk.getSdkType().getName());
    }


}
