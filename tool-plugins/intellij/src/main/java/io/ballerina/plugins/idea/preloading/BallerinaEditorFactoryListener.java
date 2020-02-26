package io.ballerina.plugins.idea.preloading;

import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFileBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.ballerina.plugins.idea.BallerinaConstants.BAL_FILE_EXT;
import static io.ballerina.plugins.idea.preloading.LSPUtils.registerServerDefinition;

/**
 * Editor listener implementation which is used to handle ballerina source files inside non-ballerina projects.
 */
public class BallerinaEditorFactoryListener implements EditorFactoryListener {

    private Project project;
    private boolean balSourcesFound = false;

    public BallerinaEditorFactoryListener(Project project) {
        this.project = project;
    }

    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        Project project = event.getEditor().getProject();
        if (project == null) {
            return;
        }
        VirtualFile file = FileDocumentManager.getInstance().getFile(event.getEditor().getDocument());
        if (!balSourcesFound && project.equals(this.project) && isBalFile(file)) {
            registerServerDefinition(project);
            balSourcesFound = true;
        }
    }

    public static boolean isBalFile(@Nullable VirtualFile file) {
        if (file == null || file.getExtension() == null || file instanceof LightVirtualFileBase) {
            return false;
        }
        String fileUrl = file.getUrl();
        if (fileUrl.isEmpty() || fileUrl.startsWith("jar:")) {
            return false;
        }

        return file.getExtension().equals(BAL_FILE_EXT);
    }
}
