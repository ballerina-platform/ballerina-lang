/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.inspections;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.PluginsAdvertiser;
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.UnknownFeature;
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.UnknownFeaturesCollector;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import io.ballerina.plugins.idea.BallerinaFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LSPInstallationNotificationProvider extends EditorNotifications.Provider<EditorNotificationPanel>
        implements DumbAware {

    private static final Key<EditorNotificationPanel> KEY = Key.create("LSP Support Plugin is not installed");
    private final Project myProject;
    private final EditorNotifications myNotifications;
    private final Set<String> myEnabledExtensions = new HashSet<>();

    public LSPInstallationNotificationProvider(Project project, final EditorNotifications notifications) {
        myProject = project;
        myNotifications = notifications;
    }

    @NotNull
    @Override
    public Key<EditorNotificationPanel> getKey() {
        return KEY;
    }

    @Nullable
    @Override
    public EditorNotificationPanel createNotificationPanel(@NotNull VirtualFile file, @NotNull FileEditor fileEditor) {

        if (file.getFileType() != BallerinaFileType.INSTANCE) {
            return null;
        } else {
            final String extension = file.getExtension();
            final String fileName = file.getName();
            final EditorNotificationPanel panel = extension != null ? createPanel("*." + extension) : null;
            if (panel != null) {
                return panel;
            }
            return createPanel(fileName);
        }
    }

    private EditorNotificationPanel createPanel(String extension) {
        PluginsAdvertiser.Plugin LSPPlugin = new PluginsAdvertiser.Plugin();
        LSPPlugin.myPluginId = "com.github.gtache.lsp";
        LSPPlugin.myPluginName = "LSP Support";

        return createPanel(extension, LSPPlugin);
    }

    @Nullable
    private EditorNotificationPanel createPanel(final String extension, PluginsAdvertiser.Plugin plugin) {
        EditorNotificationPanel panel = new EditorNotificationPanel();
        panel.setText("LSP Support Plugin is either not installed or enabled properly");
        if (isDisabled(plugin)) {
            panel.createActionLabel("Enable LSP Support plugin", () -> {
                myEnabledExtensions.add(extension);
                myNotifications.updateAllNotifications();
            });
        } else {
            panel.createActionLabel("Install Support Plugin", () -> {
                Set<String> pluginIds = new HashSet<>();
                pluginIds.add(plugin.myPluginId);
                PluginsAdvertiser.installAndEnablePlugins(pluginIds, () -> {
                    myEnabledExtensions.add(extension);
                    myNotifications.updateAllNotifications();
                });
            });
        }
        panel.createActionLabel("Ignore extension", () -> {
            UnknownFeaturesCollector.getInstance(myProject).ignoreFeature(createExtensionFeature(extension));
            myNotifications.updateAllNotifications();
        });
        return panel;
    }

    private static UnknownFeature createExtensionFeature(String extension) {
        return new UnknownFeature(FileTypeFactory.FILE_TYPE_FACTORY_EP.getName(), "File Type", extension);
    }

    private boolean isDisabled(PluginsAdvertiser.Plugin plugin) {
        final List<String> disabledPlugins = PluginManagerCore.getDisabledPlugins();

        if (disabledPlugins.contains(plugin.myPluginId)) {
            return true;
        }
        return false;
    }

}
