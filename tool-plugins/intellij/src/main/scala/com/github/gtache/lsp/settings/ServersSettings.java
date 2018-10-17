package com.github.gtache.lsp.settings;

import com.github.gtache.lsp.PluginMain;
import com.github.gtache.lsp.client.languageserver.serverdefinition.LanguageServerDefinition;
import com.github.gtache.lsp.client.languageserver.serverdefinition.UserConfigurableServerDefinition;
import com.github.gtache.lsp.settings.gui.ServersGUI;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JComponent;

/**
 * Class used to manage the settings related to the LSP.
 */
public final class ServersSettings implements Configurable {

    private static final Logger LOG = Logger.getInstance(ServersSettings.class);
    @Nullable
    private static ServersGUI lspGUI;
    private static ServersSettings instance;

    private ServersSettings() {
    }

    public static ServersSettings getInstance() {
        if (instance == null) {
            instance = new ServersSettings();
        }
        return instance;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Language Server Protocol";
    }

    @Override
    public String getHelpTopic() {
        return "com.github.gtache.lsp.settings.ServersSettings";
    }

    @Override
    public JComponent createComponent() {
        lspGUI = new ServersGUI();
        setGUIFields(PluginMain.getExtToServerDefinitionJava());
        return lspGUI.getRootPanel();
    }

    private void setGUIFields(final Map<String, LanguageServerDefinition> map) {
        final Map<String, UserConfigurableServerDefinition> filtered = map.entrySet().stream()
                .filter(e -> e.getValue() instanceof UserConfigurableServerDefinition)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (UserConfigurableServerDefinition) e.getValue()));
        if (!filtered.isEmpty()) {
            lspGUI.clear();
        }
        for (final UserConfigurableServerDefinition definition : filtered.values()) {
            lspGUI.addServerDefinition(definition);
        }
    }

    @Override
    public boolean isModified() {
        return lspGUI.isModified();
    }

    @Override
    public void apply() {
        lspGUI.apply();
    }

    @Override
    public void reset() {
        lspGUI.reset();
    }

    @Override
    public void disposeUIResources() {
        lspGUI = null;
    }
}
