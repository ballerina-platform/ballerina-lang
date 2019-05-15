package io.ballerina.plugins.idea.webview.diagram.actions.editorLayout;

import io.ballerina.plugins.idea.webview.diagram.split.SplitFileEditor;

public class EditorOnlyLayoutChangeAction extends BaseChangeSplitLayoutAction {
  protected EditorOnlyLayoutChangeAction() {
    super(SplitFileEditor.SplitEditorLayout.FIRST);
  }
}
