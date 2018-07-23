package io.ballerina.plugins.idea.ui.actions.editorLayout;

import io.ballerina.plugins.idea.ui.split.SplitFileEditor;

public class PreviewOnlyLayoutChangeAction extends BaseChangeSplitLayoutAction {
  protected PreviewOnlyLayoutChangeAction() {
    super(SplitFileEditor.SplitEditorLayout.SECOND);
  }
}
