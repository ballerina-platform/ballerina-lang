package io.ballerina.plugins.idea.webview.diagram.actions.editorLayout;

import io.ballerina.plugins.idea.webview.diagram.split.SplitFileEditor;

public class PreviewOnlyLayoutChangeAction extends BaseChangeSplitLayoutAction {
  protected PreviewOnlyLayoutChangeAction() {
    super(SplitFileEditor.SplitEditorLayout.SECOND);
  }
}
