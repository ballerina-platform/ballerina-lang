/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
