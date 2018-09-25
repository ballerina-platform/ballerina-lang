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

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.InstalledPluginsTableModel;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.PluginManagerConfigurable;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.plugins.PluginManagerMain;
import com.intellij.ide.plugins.PluginManagerUISettings;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.options.ShowSettingsUtil;
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

import static com.intellij.ide.plugins.PluginManagerCore.getDisabledPlugins;

/**
 * Provides message/notification with the fix, if the LSP dependency plugin is not installed or not enabled.
 */
public class LSPluginInstallationNotificationProvider extends EditorNotifications.Provider<EditorNotificationPanel>
        implements DumbAware {

    private static final Key<EditorNotificationPanel> KEY = Key.create("LSP Support Plugin is not installed");
    private static final String LSP_PLUGIN_ID = "com.github.gtache.lsp";
    private static final String LSP_PLUGIN_NAME = "LSP Support";
    private final Project myProject;
    private final EditorNotifications myNotifications;
    private final Set<String> myEnabledExtensions = new HashSet<>();

    public LSPluginInstallationNotificationProvider(Project project, final EditorNotifications notifications) {
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

        if (file.getFileType() != BallerinaFileType.INSTANCE || isAlreadyInstalled()) {
            return null;
        }

        final String extension = file.getExtension();
        final String fileName = file.getName();
        if (extension != null && (isIgnored("*." + extension) || isIgnored(fileName))) {
            return null;
        }
        return extension != null ? createPanel("*." + extension) : null;
    }

    private boolean isIgnored(String extension) {
        return myEnabledExtensions.contains(extension) || UnknownFeaturesCollector.getInstance(myProject)
                .isIgnored(createExtensionFeature(extension));
    }

    private EditorNotificationPanel createPanel(String extension) {
        PluginsAdvertiser.Plugin lspPlugin = new PluginsAdvertiser.Plugin();
        lspPlugin.myPluginId = LSP_PLUGIN_ID;
        lspPlugin.myPluginName = LSP_PLUGIN_NAME;

        return createPanel(extension, lspPlugin);
    }

    private EditorNotificationPanel createPanel(final String extension, PluginsAdvertiser.Plugin plugin) {
        EditorNotificationPanel panel = new EditorNotificationPanel();

        final IdeaPluginDescriptor disabledPlugin = getDisabledPlugin(plugin);

        if (disabledPlugin != null) {
            panel.setText("LSP Support Plugin is not enabled to provide ballerina language server features (code "
                    + "completion, diagnostics, hover support etc)");
            panel.createActionLabel("Enable Plugin", () -> {
                myEnabledExtensions.add(extension);
                myNotifications.updateAllNotifications();
                enablePlugin(myProject, disabledPlugin);
            });
        } else {
            panel.setText("LSP Support plugin is not installed to enable ballerina language server features (code "
                    + "completion, diagnostics, hover support etc.)");
            panel.createActionLabel("Install Plugin", () -> {
                Set<String> pluginIds = new HashSet<>();
                pluginIds.add(plugin.myPluginId);
                PluginsAdvertiser.installAndEnablePlugins(pluginIds, () -> {
                    myEnabledExtensions.add(extension);
                    myNotifications.updateAllNotifications();
                });
            });
        }
        panel.createActionLabel("Ignore", myNotifications::updateAllNotifications);
        return panel;
    }

    private static UnknownFeature createExtensionFeature(String extension) {
        return new UnknownFeature(FileTypeFactory.FILE_TYPE_FACTORY_EP.getName(), "File Type", extension);
    }

    private static boolean isAlreadyInstalled() {
        final IdeaPluginDescriptor[] installedPlugins = PluginManagerCore.getPlugins();

        for (IdeaPluginDescriptor plugin : installedPlugins) {
            if (plugin.getName().equals(LSP_PLUGIN_NAME)) {
                return true;
            }
        }
        return false;
    }

    private static IdeaPluginDescriptor getDisabledPlugin(PluginsAdvertiser.Plugin plugin) {

        final List<String> disabledPlugins = getDisabledPlugins();

        return disabledPlugins.contains(plugin.myPluginId) ?
                PluginManager.getPlugin(PluginId.getId(plugin.myPluginId)) :
                null;
    }

    private static void enablePlugin(Project project, IdeaPluginDescriptor lspPlugin) {
        final PluginManagerConfigurable managerConfigurable = new PluginManagerConfigurable(
                PluginManagerUISettings.getInstance());
        final PluginManagerMain createPanel = managerConfigurable.getOrCreatePanel();
        ShowSettingsUtil.getInstance().editConfigurable(project, managerConfigurable, () -> {
            final InstalledPluginsTableModel pluginsModel = (InstalledPluginsTableModel) createPanel.getPluginsModel();
            final IdeaPluginDescriptor[] descriptors = new IdeaPluginDescriptor[1];
            descriptors[0] = lspPlugin;
            pluginsModel.enableRows(descriptors, Boolean.TRUE);
            createPanel.getPluginTable().select(descriptors);
        });
    }
}
