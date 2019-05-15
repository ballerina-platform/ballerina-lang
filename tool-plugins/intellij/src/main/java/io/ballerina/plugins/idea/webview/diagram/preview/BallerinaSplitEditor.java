package io.ballerina.plugins.idea.webview.diagram.preview;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.pom.Navigatable;
import io.ballerina.plugins.idea.webview.diagram.settings.DiagramApplicationSettings;
import io.ballerina.plugins.idea.webview.diagram.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;

public class BallerinaSplitEditor extends SplitFileEditor<TextEditor, BallerinaDiagramVisualizer> implements TextEditor {
    private boolean myAutoScrollPreview = DiagramApplicationSettings.getInstance().getDiagramPreviewSettings().isAutoScrollPreview();

    public BallerinaSplitEditor(@NotNull TextEditor mainEditor, @NotNull BallerinaDiagramVisualizer secondEditor) {
        super(mainEditor, secondEditor);

        DiagramApplicationSettings.SettingsChangedListener settingsChangedListener =
                new DiagramApplicationSettings.SettingsChangedListener() {
                    @Override
                    public void beforeSettingsChanged(@NotNull DiagramApplicationSettings newSettings) {
                        boolean oldAutoScrollPreview = DiagramApplicationSettings.getInstance().getDiagramPreviewSettings().isAutoScrollPreview();

                        ApplicationManager.getApplication().invokeLater(() -> {
                            if (oldAutoScrollPreview == myAutoScrollPreview) {
                                setAutoScrollPreview(newSettings.getDiagramPreviewSettings().isAutoScrollPreview());
                            }
                        });
                    }
                };

        ApplicationManager.getApplication().getMessageBus().connect(this)
                .subscribe(DiagramApplicationSettings.SettingsChangedListener.TOPIC, settingsChangedListener);

        mainEditor.getEditor().getCaretModel().addCaretListener(new MyCaretListener());
    }

    @NotNull
    @Override
    public String getName() {
        return "Diagram split editor";
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

    public boolean isAutoScrollPreview() {
        return myAutoScrollPreview;
    }

    public void setAutoScrollPreview(boolean autoScrollPreview) {
        myAutoScrollPreview = autoScrollPreview;
    }

    private class MyCaretListener implements CaretListener {
        @Override
        public void caretPositionChanged(CaretEvent e) {
            if (!isAutoScrollPreview()) return;

            final Editor editor = e.getEditor();
            if (editor.getCaretModel().getCaretCount() != 1) {
                return;
            }
        }
    }
}
