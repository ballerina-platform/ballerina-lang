package io.ballerina.plugins.idea.webview.diagram.actions;

import io.ballerina.plugins.idea.webview.diagram.split.SplitFileEditor;

/**
 * Editor layout change action.
 */
public class EditorOnlyLayoutChangeAction extends BaseChangeSplitLayoutAction {
    protected EditorOnlyLayoutChangeAction() {
        super(SplitFileEditor.SplitEditorLayout.FIRST);
    }
}
