package io.ballerina.plugins.idea.webview.diagram.actions.editorLayout;

import io.ballerina.plugins.idea.webview.diagram.split.SplitFileEditor;

/**
 * Split view Layout Change Action.
 */
public class EditorAndPreviewLayoutChangeAction extends BaseChangeSplitLayoutAction {
    protected EditorAndPreviewLayoutChangeAction() {
        super(SplitFileEditor.SplitEditorLayout.SPLIT);
    }
}
