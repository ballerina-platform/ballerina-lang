package io.ballerina.plugins.idea.webview.diagram.preview;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import io.ballerina.plugins.idea.webview.diagram.split.SplitTextEditorProvider;
import org.jetbrains.annotations.NotNull;

public class BallerinaSplitEditorProvider extends SplitTextEditorProvider {
  public BallerinaSplitEditorProvider() {
    super(new PsiAwareTextEditorProvider(), new BallerinaDiagramEditorProvider());
  }

  @Override
  protected FileEditor createSplitEditor(@NotNull final FileEditor firstEditor, @NotNull FileEditor secondEditor) {
    if (!(firstEditor instanceof TextEditor) || !(secondEditor instanceof BallerinaDiagramEditor)) {
      throw new IllegalArgumentException("Main editor should be TextEditor");
    }
    return new BallerinaSplitEditor(((TextEditor)firstEditor), ((BallerinaDiagramEditor)secondEditor));
  }
}