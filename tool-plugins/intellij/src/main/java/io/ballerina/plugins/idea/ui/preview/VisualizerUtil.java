package io.ballerina.plugins.idea.ui.preview;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.ballerina.plugins.idea.extensions.editoreventmanager.BallerinaEditorEventManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.editor.EditorEventManagerBase;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class VisualizerUtil {
    private static final Logger LOG = Logger.getInstance(VisualizerUtil.class);

    public static String md5(String buffer, @NonNls String key) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Cannot find 'md5' algorithm; ", e);
        }

        Objects.requireNonNull(md5).update(buffer.getBytes(StandardCharsets.UTF_8));
        byte[] code = md5.digest(key.getBytes(StandardCharsets.UTF_8));
        BigInteger bi = new BigInteger(code).abs();
        return bi.abs().toString(16);
    }

    @NotNull
    public static String generateMarkdownHtml(@NotNull VirtualFile file) {
        String text = "";
        Project project = ProjectUtil.guessProjectForFile(file);
        if (project == null) {
            text = "";
        }
        EditorEventManager manager = EditorEventManagerBase.forEditor(editorFromVirtualFile(file, project));
        BallerinaEditorEventManager balManager = (BallerinaEditorEventManager) manager;
        if (balManager != null) {
            text = balManager.getAST();
        }
        String html = "<html><header><title>This is title</title></header><body>" + text + "</body></html>";
        return html;
    }

    private static Editor editorFromVirtualFile(VirtualFile file, Project project) {
        FileEditor[] allEditors = FileEditorManager.getInstance(project).getAllEditors(file);
        if (allEditors.length > 0 && allEditors[0] instanceof TextEditor) {
            return ((TextEditor) allEditors[0]).getEditor();
        }
        return null;
    }
}

