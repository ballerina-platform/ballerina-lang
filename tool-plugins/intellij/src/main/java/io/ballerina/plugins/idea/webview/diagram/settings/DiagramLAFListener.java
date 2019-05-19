package io.ballerina.plugins.idea.webview.diagram.settings;

import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.UIManager;

class DiagramLAFListener implements LafManagerListener {
    private boolean isLastLAFWasDarcula = UIUtil.isUnderDarcula();

    @Override
    public void lookAndFeelChanged(LafManager source) {
        final UIManager.LookAndFeelInfo newLookAndFeel = source.getCurrentLookAndFeel();
        final boolean isNewLookAndFeelDarcula = isDarcula(newLookAndFeel);

        if (isNewLookAndFeelDarcula == isLastLAFWasDarcula) {
            return;
        }

        updateCssSettingsForced(isNewLookAndFeelDarcula);
    }

    private void updateCssSettingsForced(boolean isDarcula) {
        final DiagramCssSettings currentCssSettings = DiagramApplicationSettings.getInstance().getDiagramCssSettings();
        final String stylesheetUri = StringUtil.isEmpty(currentCssSettings.getStylesheetUri()) ?
                DiagramCssSettings.getDefaultCssSettings(isDarcula).getStylesheetUri() :
                currentCssSettings.getStylesheetUri();

        DiagramApplicationSettings.getInstance().setDiagramCssSettings(
                new DiagramCssSettings(currentCssSettings.isUriEnabled(), stylesheetUri,
                        currentCssSettings.isTextEnabled(), currentCssSettings.getStylesheetText()));
        isLastLAFWasDarcula = isDarcula;
    }

    private static boolean isDarcula(@Nullable UIManager.LookAndFeelInfo laf) {
        if (laf == null) {
            return false;
        }
        return laf.getName().contains("Darcula");
    }
}
