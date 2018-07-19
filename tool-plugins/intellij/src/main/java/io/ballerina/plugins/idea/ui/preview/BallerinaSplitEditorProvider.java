package io.ballerina.plugins.idea.ui.preview;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import io.ballerina.plugins.idea.ui.split.SplitTextEditorProvider;
import org.jetbrains.annotations.NotNull;

public class BallerinaSplitEditorProvider extends SplitTextEditorProvider {
  public BallerinaSplitEditorProvider() {
    super(new PsiAwareTextEditorProvider(), new BallerinaDiagramVisualizerProvider());
  }

  @Override
  protected FileEditor createSplitEditor(@NotNull final FileEditor firstEditor, @NotNull FileEditor secondEditor) {
    if (!(firstEditor instanceof TextEditor) || !(secondEditor instanceof BallerinaDiagramVisualizer)) {
      throw new IllegalArgumentException("Main editor should be TextEditor");
    }
    return new BallerinaSplitEditor(((TextEditor)firstEditor), ((BallerinaDiagramVisualizer)secondEditor));
  }
}