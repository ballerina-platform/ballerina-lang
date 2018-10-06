package com.github.gtache.lsp.settings;

import com.github.gtache.lsp.settings.gui.MiscGUI;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

/**
 * Class representing MISC settings.
 */
public final class MiscSettings implements Configurable {

    private static final Logger LOG = Logger.getInstance(MiscSettings.class);
    @Nullable
    private static MiscGUI miscGUI;
    private static MiscSettings instance;

    private MiscSettings() {
    }

    public static MiscSettings getInstance() {
        if (instance == null) {
            instance = new MiscSettings();
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
        return "com.github.gtache.lsp.settings.MiscSettings";
    }

    @Override
    public JComponent createComponent() {
        miscGUI = new MiscGUI();
        return miscGUI.getRootPanel();
    }

    @Override
    public boolean isModified() {
        return miscGUI.isModified();
    }

    @Override
    public void apply() {
        miscGUI.apply();
    }

    @Override
    public void reset() {
        miscGUI.reset();
    }

    @Override
    public void disposeUIResources() {
        miscGUI = null;
    }
}
