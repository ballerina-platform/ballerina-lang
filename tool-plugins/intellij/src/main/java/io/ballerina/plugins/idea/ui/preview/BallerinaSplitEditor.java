package io.ballerina.plugins.idea.ui.preview;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.pom.Navigatable;
import io.ballerina.plugins.idea.ui.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;

public class BallerinaSplitEditor extends SplitFileEditor<TextEditor, BallerinaDiagramVisualizer> implements TextEditor {

    public BallerinaSplitEditor(@NotNull TextEditor mainEditor, @NotNull BallerinaDiagramVisualizer secondEditor) {
        super(mainEditor, secondEditor);
       // mainEditor.getEditor().getCaretModel().addCaretListener(new MyCaretListener());
    }

    @NotNull
    @Override
    public String getName() {
        return "Ballerina split editor";
    }

    @NotNull
    @Override
    public Editor getEditor() {
        return getMainEditor().getEditor();
    }

    @Override
    public boolean canNavigateTo(@NotNull Navigatable navigatable) {
        return getMainEditor().canNavigateTo(navigatable);
    }

    @Override
    public void navigateTo(@NotNull Navigatable navigatable) {
        getMainEditor().navigateTo(navigatable);
    }

//    private class MyCaretListener implements CaretListener {
//        @Override
//        public void caretPositionChanged(CaretEvent e) {
//            if (!isAutoScrollPreview())
//                return;
//
//            final Editor editor = e.getEditor();
//            if (editor.getCaretModel().getCaretCount() != 1) {
//                return;
//            }
//
//            final int offset = editor.logicalPositionToOffset(e.getNewPosition());
//            getSecondEditor().scrollToSrcOffset(offset);
//        }
//
//        @Override
//        public void caretAdded(CaretEvent e) {
//
//        }
//
//        @Override
//        public void caretRemoved(CaretEvent e) {
//
//        }
//    }
}
