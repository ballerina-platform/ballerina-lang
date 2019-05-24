package io.ballerina.plugins.idea.webview.diagram.preview;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.google.common.base.Strings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.ballerina.plugins.idea.extensions.editoreventmanager.BallerinaEditorEventManager;
import io.ballerina.plugins.idea.sdk.BallerinaSdk;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.editor.EditorEventManagerBase;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Objects;

import static org.wso2.lsp4intellij.utils.FileUtils.editorToURIString;

class BallerinaDiagramUtils {
    private static final Logger LOG = Logger.getInstance(BallerinaDiagramUtils.class);

    private static final String COMPOSER_LIB_RESOURCE_PATH = "/lib/tools/composer-library";
    private static final String TEMPLATES_CLASSPATH = "/fileTemplates/diagram";
    private static final String LOADER_TEMPLATE_NAME = "loading-wheel";
    private static final String WEBVIEW_TEMPLATE_NAME = "webview";
    private static final String STYLES_TEMPLATE_NAME = "styles";
    private static final String SCRIPT_TEMPLATE_NAME = "loaded-script";
    private static final String LANG_CLIENT_TEMPLATE_NAME = "lang-client";
    private static final String FIREBUG_TEMPLATE_NAME = "firebug";
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
    static String getLoadingWheel(@NotNull VirtualFile file, DiagramHtmlPanel panel, Project project) {
        try {
            if (panel == null) {
                return "";
            }

            BallerinaSdk balSdk = BallerinaSdkUtil.getBallerinaSdkFor(project);
            if (balSdk.getSdkPath() == null) {
                LOG.debug("No Ballerina SDK is found for the project: " + project.getName());
                return "";
            }
            if (!balSdk.hasWebviewSupport()) {
                LOG.debug("Detected ballerina sdk version does not have diagram editor support" + project.getName());
                return "";
            }

            Handlebars handlebars = new Handlebars().with(new ClassPathTemplateLoader(TEMPLATES_CLASSPATH));
            Template loaderTemplate = handlebars.compile(LOADER_TEMPLATE_NAME);

            HashMap<String, String> loaderContents = new HashMap<>();
            loaderContents.put("resourceRoot", Paths.get(balSdk.getSdkPath(), COMPOSER_LIB_RESOURCE_PATH).toUri()
                    .toString());
            Context loaderContext = Context.newBuilder(loaderContents).resolver(MapValueResolver.INSTANCE)
                    .build();
            return loaderTemplate.apply(loaderContext);

        } catch (IOException | RuntimeException e) {
            LOG.warn("Error occurred when constructing webview content: ", e);
            return "";
        }
    }


    @NotNull
    static String generateDiagramHtml(@NotNull VirtualFile file, DiagramHtmlPanel panel, Project project) {

        // Requests the AST from the Ballerina language server.
        Editor editor = editorFromVirtualFile(file, project);
        EditorEventManager manager = EditorEventManagerBase.forEditor(editor);
        BallerinaEditorEventManager balManager = (BallerinaEditorEventManager) manager;
        if (balManager == null) {
            LOG.debug("Editor event manager is null for: " + editor.toString());
            return "";
        }

        // Requests AST from the language server.
        String ast = balManager.getAST();
        if (Strings.isNullOrEmpty(ast)) {
            LOG.debug("Received an empty AST response");
            return "";
        }

        BallerinaSdk balSdk = BallerinaSdkUtil.getBallerinaSdkFor(project);
        if (balSdk.getSdkPath() == null) {
            LOG.debug("No Ballerina SDK is found for the project: " + project.getName());
            return "";
        }
        if (!balSdk.hasWebviewSupport()) {
            LOG.debug("Detected ballerina sdk version does not have diagram editor support" + project.getName());
            return "";
        }

        return getWebviewContent(editorToURIString(editor), ast, panel, balSdk.getSdkPath());
    }

    private static Editor editorFromVirtualFile(VirtualFile file, Project project) {
        FileEditor[] allEditors = FileEditorManager.getInstance(project).getAllEditors(file);
        if (allEditors.length > 0 && allEditors[0] instanceof TextEditor) {
            return ((TextEditor) allEditors[0]).getEditor();
        }
        return null;
    }

    private static String getWebviewContent(String uri, String ast, DiagramHtmlPanel myPanel, String sdkPath) {
        try {
            if (myPanel == null) {
                return "";
            }

            Handlebars handlebars = new Handlebars().with(new ClassPathTemplateLoader(TEMPLATES_CLASSPATH));
            Template scriptTemplate = handlebars.compile(SCRIPT_TEMPLATE_NAME);
            Template langClientTemplate = handlebars.compile(LANG_CLIENT_TEMPLATE_NAME);
            Template webviewTemplate = handlebars.compile(WEBVIEW_TEMPLATE_NAME);

            // Injects ast response to the mocked language client template.
            HashMap<String, String> langClientContents = new HashMap<>();
            langClientContents.put("ast", ast);
            Context langClientContext = Context.newBuilder(langClientContents).resolver(MapValueResolver.INSTANCE)
                    .build();

            // Constructs the script to be run when loaded.
            HashMap<String, String> scriptContents = new HashMap<>();
            scriptContents.put("docUri", uri);
            scriptContents.put("windowWidth", Integer.toString(myPanel.getComponent().getWidth()));
            scriptContents.put("windowHeight", Integer.toString(myPanel.getComponent().getHeight()));
            scriptContents.put("getLangClient", langClientTemplate.apply(langClientContext));
            Context scriptContext = Context.newBuilder(scriptContents).resolver(MapValueResolver.INSTANCE).build();

            // Constructs the final webview HTML template.
            HashMap<String, String> webviewContents = new HashMap<>();
            webviewContents.put("resourceRoot", Paths.get(sdkPath, COMPOSER_LIB_RESOURCE_PATH).toUri().toString());
            webviewContents.put("body", BODY_TEMPLATE);
            webviewContents.put("bodyCssClass", BODY_CSS_CLASS);
            webviewContents.put("styles", handlebars.compile(STYLES_TEMPLATE_NAME).text());
            webviewContents.put("fireBug", handlebars.compile(FIREBUG_TEMPLATE_NAME).text());
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
}
