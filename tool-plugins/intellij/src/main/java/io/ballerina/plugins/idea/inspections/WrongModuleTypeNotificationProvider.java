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

package io.ballerina.plugins.idea.inspections;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.containers.ContainerUtil;
import icons.BallerinaIcons;
import io.ballerina.plugins.idea.BallerinaFileType;
import io.ballerina.plugins.idea.sdk.BallerinaSdkService;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Provides wrong module type message if the ballerina file is not in a Ballerina module.
 */
public class WrongModuleTypeNotificationProvider extends EditorNotifications.Provider<EditorNotificationPanel>
        implements DumbAware {

    private static final Key<EditorNotificationPanel> KEY = Key.create("Wrong module type");
    private static final String DONT_ASK_TO_CHANGE_MODULE_TYPE_KEY = "do.not.ask.to.change.module.type";

    private final Project myProject;

    public WrongModuleTypeNotificationProvider(@NotNull Project project) {
        myProject = project;
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
        Module module = ModuleUtilCore.findModuleForFile(file, myProject);
        return (module == null || BallerinaSdkService.getInstance(myProject).isBallerinaModule(module)
                || getIgnoredModules(myProject).contains(module.getName())
                || !BallerinaSdkUtils.autoDetectSdk(myProject).isEmpty()) ? null : createPanel(myProject, module);
    }

    @NotNull
    private static EditorNotificationPanel createPanel(@NotNull Project project, @NotNull Module module) {
        EditorNotificationPanel panel = new EditorNotificationPanel();
        panel.setText(String.format("'%s' is not a Ballerina Module. Some features will not work.", module.getName()));
        panel.setToolTipText("You can fix this by navigating to File -> Project Structure -> Modules and adding '"
                + module.getName() + "' as a Ballerina module.");
        panel.icon(BallerinaIcons.ICON);
        return panel;
    }

    @NotNull
    private static Set<String> getIgnoredModules(@NotNull Project project) {
        String value = PropertiesComponent.getInstance(project).getValue(DONT_ASK_TO_CHANGE_MODULE_TYPE_KEY, "");
        return ContainerUtil.newLinkedHashSet(StringUtil.split(value, ","));
    }
}
