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
package io.ballerina.plugins.idea.webview.diagram.preview;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.pom.Navigatable;
import io.ballerina.plugins.idea.webview.diagram.settings.DiagramApplicationSettings;
import io.ballerina.plugins.idea.webview.diagram.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;

/**
 * Split editor implementation for ballerina diagram editor.
 */
public class BallerinaSplitEditor extends SplitFileEditor<TextEditor, BallerinaDiagramEditor> implements TextEditor {
    private boolean myAutoScrollPreview = DiagramApplicationSettings.getInstance().getDiagramPreviewSettings()
            .isAutoScrollPreview();

    BallerinaSplitEditor(@NotNull TextEditor mainEditor, @NotNull BallerinaDiagramEditor secondEditor) {
        super(mainEditor, secondEditor);

        DiagramApplicationSettings.SettingsChangedListener settingsChangedListener =
                new DiagramApplicationSettings.SettingsChangedListener() {
                    @Override
                    public void beforeSettingsChanged(@NotNull DiagramApplicationSettings newSettings) {
                        boolean oldAutoScrollPreview = DiagramApplicationSettings.getInstance().
                                getDiagramPreviewSettings().isAutoScrollPreview();

                        ApplicationManager.getApplication().invokeLater(() -> {
                            if (oldAutoScrollPreview == myAutoScrollPreview) {
                                setAutoScrollPreview(newSettings.getDiagramPreviewSettings().isAutoScrollPreview());
                            }
                        });
                    }
                };

        ApplicationManager.getApplication().getMessageBus().connect(this)
                .subscribe(DiagramApplicationSettings.SettingsChangedListener.TOPIC, settingsChangedListener);

        mainEditor.getEditor().getCaretModel().addCaretListener(new MyCaretListener());
    }

    @NotNull
    @Override
    public String getName() {
        return "Diagram split editor";
    }

    @NotNull
    @Override
    public Editor getEditor() {
        return getMainEditor().getEditor();
    }

    @Override
    public boolean canNavigateTo(@NotNull Navigatable navigatable) {
        return getMainEditor().canNavigateTo(navigatable);
    }

    @Override
    public void navigateTo(@NotNull Navigatable navigatable) {
        getMainEditor().navigateTo(navigatable);
    }

    public boolean isAutoScrollPreview() {
        return myAutoScrollPreview;
    }

    private void setAutoScrollPreview(boolean autoScrollPreview) {
        myAutoScrollPreview = autoScrollPreview;
    }

    private class MyCaretListener implements CaretListener {
        @Override
        public void caretPositionChanged(@NotNull CaretEvent e) {
            // Todo
        }
    }
}
