package io.ballerina.plugins.idea.webview.diagram.actions.editorLayout;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.project.DumbAware;
import io.ballerina.plugins.idea.webview.diagram.actions.ToolbarActionUtil;
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
