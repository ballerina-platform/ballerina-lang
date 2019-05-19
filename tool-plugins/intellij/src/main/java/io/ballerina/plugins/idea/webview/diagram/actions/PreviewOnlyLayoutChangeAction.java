package io.ballerina.plugins.idea.webview.diagram.actions;

import io.ballerina.plugins.idea.webview.diagram.split.SplitFileEditor;

/**
 * Diagram viewer layout change action.
 */
public class PreviewOnlyLayoutChangeAction extends BaseChangeSplitLayoutAction {
    protected PreviewOnlyLayoutChangeAction() {
        super(SplitFileEditor.SplitEditorLayout.SECOND);
    }
}
