package io.ballerina.plugins.idea.webview.diagram.preview;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.ballerina.plugins.idea.extensions.editoreventmanager.BallerinaEditorEventManager;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.editor.EditorEventManagerBase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Objects;

import static org.wso2.lsp4intellij.utils.FileUtils.editorToURIString;

public class BallerinaDiagramUtils {
    private static final Logger LOG = Logger.getInstance(BallerinaDiagramUtils.class);

    private static final String TEMPLATES_CLASSPATH = "/fileTemplates/diagram";
    private static final String RESOURCE_COMPOSER = "/composer/composer.js";
    private static final String RESOURCE_CODEPOINTS = "/composer/font/codepoints.js";
    private static final String RESOURCE_THEME = "/composer/themes/ballerina-default.min.css";
    private static final String RESOURCE_FONT = "/composer/font/font/font-ballerina.css";
    private static final String WEBVIEW_TEMPLATE_NAME = "webview";
    private static final String STYLES_TEMPLATE_NAME = "styles";
    private static final String SCRIPT_TEMPLATE_NAME = "loaded-script";
    private static final String LANG_CLIENT_TEMPLATE_NAME = "lang-client";
    private static final String BODY_TEMPLATE = "<div id=\"warning\"></div>\n"
            + "<div class=\"ballerina-editor design-view-container\" id=\"diagram\"></div>\n";
    private static final String DISABLE_EDITING_CSS =
            "#diagram > div > div > div.diagram-controllers > div.ui.sticky > div > div > div:nth-child(1){\n"
                    + "display: none;\n" + "}";
    private static final String BODY_CSS_CLASS = "diagram";

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
    static String generateDiagramHtml(@NotNull VirtualFile file, DiagramHtmlPanel myPanel) {
        String ast = "";
        Project project = ProjectUtil.guessProjectForFile(file);
        if (project == null) {
            return ast;
        }

        // Requests the AST from the Ballerina language server.
        Editor editor = editorFromVirtualFile(file, project);
        EditorEventManager manager = EditorEventManagerBase.forEditor(editor);
        BallerinaEditorEventManager balManager = (BallerinaEditorEventManager) manager;
        if (balManager != null) {
            ast = balManager.getAST();
        }

        if (ast != null && !ast.isEmpty()) {
            return getWebviewContent(editorToURIString(editor), ast, myPanel);
        }
        return "";
    }

    private static Editor editorFromVirtualFile(VirtualFile file, Project project) {
        FileEditor[] allEditors = FileEditorManager.getInstance(project).getAllEditors(file);
        if (allEditors.length > 0 && allEditors[0] instanceof TextEditor) {
            return ((TextEditor) allEditors[0]).getEditor();
        }
        return null;
    }

    private static String getWebviewContent(String uri, String ast, DiagramHtmlPanel myPanel) {
        try {
            if (myPanel == null) {
                return "";
            }
            //Reads required styles contents from the plugin jar.
            String composerJsContent = getFileContent(RESOURCE_COMPOSER);
            String codePointsJsContent = getFileContent(RESOURCE_CODEPOINTS);
            String themeCssConent = getFileContent(RESOURCE_THEME);
            String fontCssContent = getFileContent(RESOURCE_FONT);

            myPanel.setCSS(themeCssConent);
            myPanel.setCSS(fontCssContent);
            Handlebars handlebars = new Handlebars().with(new ClassPathTemplateLoader(TEMPLATES_CLASSPATH));
            HashMap<String, String> webviewContents = new HashMap<>();
            HashMap<String, String> scriptContents = new HashMap<>();
            HashMap<String, String> langClientContents = new HashMap<>();
            Template webviewTemplate = handlebars.compile(WEBVIEW_TEMPLATE_NAME);
            Template scriptTemplate = handlebars.compile(SCRIPT_TEMPLATE_NAME);
            Template langClientTemplate = handlebars.compile(LANG_CLIENT_TEMPLATE_NAME);

            // Injects ast response to the mocked language client.
            langClientContents.put("ast", ast);
            Context langClientContext = Context.newBuilder(langClientContents).resolver(MapValueResolver.INSTANCE)
                    .build();

            // Constructs the script to be run when loaded.
            scriptContents.put("docUri", uri);
            scriptContents.put("windowWidth", Integer.toString(myPanel.getComponent().getWidth()));
            scriptContents.put("windowHeight", Integer.toString(myPanel.getComponent().getHeight()));
            scriptContents.put("getLangClient", langClientTemplate.apply(langClientContext));
            Context scriptContext = Context.newBuilder(scriptContents).resolver(MapValueResolver.INSTANCE).build();

            // Constructs the final webview HTML template.
            webviewContents.put("body", BODY_TEMPLATE);
            webviewContents.put("bodyCssClass", BODY_CSS_CLASS);
            webviewContents.put("themeCss", themeCssConent);
            webviewContents.put("fontCss", fontCssContent);
            webviewContents.put("styles", handlebars.compile(STYLES_TEMPLATE_NAME).text());
            webviewContents.put("codePoints", codePointsJsContent);
            webviewContents.put("composer", composerJsContent);
            webviewContents.put("loadedScript", scriptTemplate.apply(scriptContext));
            //Todo - remove when the editing support is added.
            webviewContents.put("disableEdit", DISABLE_EDITING_CSS);
            Context webviewContext = Context.newBuilder(webviewContents).resolver(MapValueResolver.INSTANCE).build();
            return webviewTemplate.apply(webviewContext);

        } catch (IOException | RuntimeException e) {
            LOG.warn("Error occurred when constructing webview content: ", e);
            return "";
        }
    }

    private static String getFileContent(String resource) throws IOException, RuntimeException {
        File file;
        URL res = BallerinaDiagramUtils.class.getResource(resource);
        if (res.getProtocol().equals("jar")) {
            try {
                InputStream input = BallerinaDiagramUtils.class.getResourceAsStream(resource);
                file = File.createTempFile("tempfile", ".tmp");
                OutputStream out = new FileOutputStream(file);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.close();
                file.deleteOnExit();
            } catch (IOException e) {
                LOG.warn("Error occurred when reading the file at " + resource, e);
                throw e;
            }
        } else {
            //this will probably work in your IDE, but not from a JAR.
            file = new File(res.getFile());
        }

        if (!file.exists()) {
            throw new RuntimeException("Error: File " + file + " not found!");
        } else {
            return FileUtils.readFileToString(file, "UTF-8");
        }
    }
}

