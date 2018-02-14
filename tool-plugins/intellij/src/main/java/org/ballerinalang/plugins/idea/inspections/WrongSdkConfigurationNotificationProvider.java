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

package org.ballerinalang.plugins.idea.inspections;

import com.intellij.ProjectTopics;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.roots.ModuleRootAdapter;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.messages.MessageBusConnection;
import org.ballerinalang.plugins.idea.BallerinaFileType;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.project.BallerinaLibrariesService;
import org.ballerinalang.plugins.idea.sdk.BallerinaSdkService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides a notifications if a non Ballerina SDK is added to a Ballerina project or a module.
 */
public class WrongSdkConfigurationNotificationProvider extends EditorNotifications.Provider<EditorNotificationPanel>
        implements DumbAware {

    private static final Key<EditorNotificationPanel> KEY = Key.create("Setup Ballerina SDK");

    private final Project myProject;

    public WrongSdkConfigurationNotificationProvider(@NotNull Project project,
                                                     @NotNull EditorNotifications notifications) {
        myProject = project;
        MessageBusConnection connection = myProject.getMessageBus().connect(project);
        connection.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootAdapter() {

            @Override
            public void rootsChanged(ModuleRootEvent event) {
                notifications.updateAllNotifications();
            }
        });
        connection.subscribe(BallerinaLibrariesService.LIBRARIES_TOPIC,
                newRootUrls -> notifications.updateAllNotifications());
    }

    @NotNull
    @Override
    public Key<EditorNotificationPanel> getKey() {
        return KEY;
    }

    @Override
    public EditorNotificationPanel createNotificationPanel(@NotNull VirtualFile file, @NotNull FileEditor fileEditor) {
        if (file.getFileType() != BallerinaFileType.INSTANCE) {
            return null;
        }

        PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
        if (psiFile == null) {
            return null;
        }

        if (psiFile.getLanguage() != BallerinaLanguage.INSTANCE) {
            return null;
        }

        Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
        if (module == null) {
            return null;
        }

        String sdkHomePath = BallerinaSdkService.getInstance(myProject).getSdkHomePath(module);
        if (StringUtil.isEmpty(sdkHomePath)) {
            return createMissingSdkPanel(myProject, module);
        }
        return null;
    }

    @NotNull
    private static EditorNotificationPanel createMissingSdkPanel(@NotNull Project project, @Nullable Module module) {
        EditorNotificationPanel panel = new EditorNotificationPanel();
        panel.setText(ProjectBundle.message("project.sdk.not.defined"));
        panel.createActionLabel(ProjectBundle.message("project.sdk.setup"),
                () -> BallerinaSdkService.getInstance(project).chooseAndSetSdk(module));
        return panel;
    }
}
