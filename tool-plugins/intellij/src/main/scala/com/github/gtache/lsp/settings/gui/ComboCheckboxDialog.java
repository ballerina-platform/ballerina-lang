package com.github.gtache.lsp.settings.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ComboCheckboxDialog extends DialogWrapper {
    private final ComboCheckboxDialogContentPanel contentPanelWrapper;
    private int exitCode;

    public ComboCheckboxDialog(final Project project, final String title, final List<String> serverDefinitions, final List<String> serverWrappers) {
        super(project, false, IdeModalityType.PROJECT);
        contentPanelWrapper = new ComboCheckboxDialogContentPanel(new ArrayList<>(serverDefinitions), new ArrayList<>(serverWrappers));
        exitCode = -1;
        setTitle(title);
        init();
    }


    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPanelWrapper.getRootPane();
    }

    @Override
    protected void doOKAction() {
        exitCode = contentPanelWrapper.getComboBoxIndex();
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        exitCode = -1;
        super.doCancelAction();
    }

    public int getExitCode() {
        return exitCode;
    }
}
