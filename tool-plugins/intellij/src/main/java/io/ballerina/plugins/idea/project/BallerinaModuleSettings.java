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

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.util.xmlb.XmlSerializerUtil;
import io.ballerina.plugins.idea.BallerinaConstants;
import io.ballerina.plugins.idea.configuration.BallerinaConfigurableProvider;
import io.ballerina.plugins.idea.configuration.BallerinaModuleSettingsConfigurable;
import org.jetbrains.annotations.NotNull;

/**
 * Handles module settings.
 */
@State(
        name = BallerinaConstants.BALLERINA_MODULE_SESTTINGS_SERVICE_NAME,
        storages = @Storage(value = StoragePathMacros.MODULE_FILE)
)
public class BallerinaModuleSettings implements
        PersistentStateComponent<BallerinaModuleSettings.BallerinaModuleSettingsState> {

    @NotNull
    private final BallerinaModuleSettingsState myState = new BallerinaModuleSettingsState();

    @NotNull
    private final Module myModule;

    public BallerinaModuleSettings(@NotNull Module module) {
        myModule = module;
    }

    public static BallerinaModuleSettings getInstance(@NotNull Module module) {
        return ModuleServiceManager.getService(module, BallerinaModuleSettings.class);
    }

    private void cleanResolveCaches() {
        Project project = myModule.getProject();
        if (!project.isDisposed()) {
            ResolveCache.getInstance(project).clearCache(true);
            DaemonCodeAnalyzer.getInstance(project).restart();
        }
    }

    @NotNull
    @Override
    public BallerinaModuleSettingsState getState() {
        return myState;
    }

    @Override
    public void loadState(BallerinaModuleSettingsState state) {
        XmlSerializerUtil.copyBean(state, myState);
    }


    static class BallerinaModuleSettingsState {

    }

    public static void showModulesConfigurable(@NotNull Project project) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        if (!project.isDisposed()) {
            ShowSettingsUtil.getInstance().editConfigurable(project, new BallerinaConfigurableProvider
                    .BallerinaProjectSettingsConfigurable(project));
        }
    }

    public static void showModulesConfigurable(@NotNull Module module) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        if (!module.isDisposed()) {
            ShowSettingsUtil.getInstance().editConfigurable(module.getProject(),
                    new BallerinaModuleSettingsConfigurable(module, true));
        }
    }
}
