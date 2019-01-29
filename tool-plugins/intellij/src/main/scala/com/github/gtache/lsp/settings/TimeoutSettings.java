package com.github.gtache.lsp.settings;

import com.github.gtache.lsp.settings.gui.TimeoutGUI;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Settings for the Timeouts
 */
public final class TimeoutSettings implements Configurable {

    private static final Logger LOG = Logger.getInstance(TimeoutSettings.class);
    @Nullable
    private static TimeoutGUI timeoutGUI;
    private static TimeoutSettings instance;

    private TimeoutSettings() {
    }

    public static TimeoutSettings getInstance() {
        if (instance == null) {
            instance = new TimeoutSettings();
        }
        return instance;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Timeouts";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "com.github.gtache.lsp.settings.TimeoutSettings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        timeoutGUI = new TimeoutGUI();
        return timeoutGUI.getRootPanel();
    }

    @Override
    public boolean isModified() {
        return timeoutGUI.isModified();
    }

    @Override
    public void apply() {
        timeoutGUI.apply();
    }

    @Override
    public void reset() {
        timeoutGUI.reset();
    }

    @Override
    public void disposeUIResources() {
        timeoutGUI = null;
    }
}
