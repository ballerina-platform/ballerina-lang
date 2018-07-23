package io.ballerina.plugins.idea.ui.actions.editorLayout;

import io.ballerina.plugins.idea.ui.split.SplitFileEditor;

public class EditorOnlyLayoutChangeAction extends BaseChangeSplitLayoutAction {
  protected EditorOnlyLayoutChangeAction() {
    super(SplitFileEditor.SplitEditorLayout.FIRST);
  }
}
