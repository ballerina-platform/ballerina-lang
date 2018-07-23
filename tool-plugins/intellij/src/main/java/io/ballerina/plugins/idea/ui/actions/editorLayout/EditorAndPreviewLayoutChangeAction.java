package io.ballerina.plugins.idea.ui.actions.editorLayout;

import io.ballerina.plugins.idea.ui.split.SplitFileEditor;

public class EditorAndPreviewLayoutChangeAction extends BaseChangeSplitLayoutAction {
  protected EditorAndPreviewLayoutChangeAction() {
    super(SplitFileEditor.SplitEditorLayout.SPLIT);
  }
}
