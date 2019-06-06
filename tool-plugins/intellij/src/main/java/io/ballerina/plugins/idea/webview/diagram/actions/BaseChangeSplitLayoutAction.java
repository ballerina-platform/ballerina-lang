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
package io.ballerina.plugins.idea.webview.diagram.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.project.DumbAware;
import io.ballerina.plugins.idea.webview.diagram.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class BaseChangeSplitLayoutAction extends AnAction implements DumbAware, Toggleable {
    @Nullable
    private final SplitFileEditor.SplitEditorLayout myLayoutToSet;

    BaseChangeSplitLayoutAction(@Nullable SplitFileEditor.SplitEditorLayout layoutToSet) {
        myLayoutToSet = layoutToSet;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final SplitFileEditor splitFileEditor = ToolbarActionUtil.findSplitEditor(e);
        e.getPresentation().setEnabled(splitFileEditor != null);

        if (myLayoutToSet != null && splitFileEditor != null) {
            e.getPresentation()
                    .putClientProperty(SELECTED_PROPERTY, splitFileEditor.getCurrentEditorLayout() == myLayoutToSet);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final SplitFileEditor splitFileEditor = ToolbarActionUtil.findSplitEditor(e);

        if (splitFileEditor != null) {
            if (myLayoutToSet == null) {
                splitFileEditor.triggerLayoutChange();
            } else {
                splitFileEditor.triggerLayoutChange(myLayoutToSet, true);
                e.getPresentation().putClientProperty(SELECTED_PROPERTY, true);
            }
        }
    }
}
