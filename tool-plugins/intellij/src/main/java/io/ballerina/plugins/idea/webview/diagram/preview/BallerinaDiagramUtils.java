/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.ballerina.plugins.idea.webview.diagram.preview;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.google.common.base.Strings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtils;
import io.ballerina.plugins.idea.settings.autodetect.BallerinaAutoDetectionSettings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Objects;

import static org.wso2.lsp4intellij.utils.FileUtils.editorToProjectFolderUri;
import static org.wso2.lsp4intellij.utils.FileUtils.editorToURIString;
import static org.wso2.lsp4intellij.utils.FileUtils.pathToUri;

/**
 * Diagram utilities.
 */
public class BallerinaDiagramUtils {
    private static final Logger LOG = Logger.getInstance(BallerinaDiagramUtils.class);

    private static final String COMPOSER_LIB_RESOURCE_PATH = "/lib/tools/composer-library";
    private static final String TEMPLATES_CLASSPATH = "/fileTemplates/diagram";
    private static final String WEBVIEW_TEMPLATE_NAME = "webview";
    private static final String STYLES_TEMPLATE_NAME = "styles";
    private static final String SCRIPT_TEMPLATE_NAME = "scripts";
    private static final String LANG_CLIENT_TEMPLATE_NAME = "lang-client";
    private static final String FIREBUG_TEMPLATE_NAME = "firebug";
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
    static String generateDiagramHtml(@NotNull VirtualFile file, DiagramHtmlPanel panel, Project project) {

        // Requests the AST from the Ballerina language server.
        Editor editor = getEditorFor(file, project);

        // Retrieves attached ballerina SDk path of the project.
        String balSdkPath = BallerinaSdkUtils.getBallerinaSdkFor(project).getSdkPath();

        // Checks for the user-configured auto detection settings.
        if (Strings.isNullOrEmpty(balSdkPath)
                && BallerinaAutoDetectionSettings.getInstance(project).getIsAutoDetectionEnabled()) {
            balSdkPath = BallerinaSdkUtils.autoDetectSdk(project);
        }

        if (Strings.isNullOrEmpty(balSdkPath)) {
            LOG.warn(String.format("No Ballerina SDK is found for the project: %s", project.getName()));
            return "";
        }

        if (editor != null && !editor.isDisposed()) {
            String filePath = Objects.requireNonNull(FileDocumentManager.getInstance()
                    .getFile(editor.getDocument())).getPath();

            // Tries to detect a whether a ballerina project exists for the given file and if not,
            // returns and empty string.
            String ballerinaProjectRoot = BallerinaSdkUtils.searchForBallerinaProjectRoot(filePath,
                    editorToProjectFolderUri(editor));

            // If a ballerina project is not found, diagram editor treats it as an individual bal file.
            if (ballerinaProjectRoot.isEmpty()) {
                return getWebviewContent(ballerinaProjectRoot, editorToURIString(editor), panel, balSdkPath);
            } else {
                return getWebviewContent(pathToUri(ballerinaProjectRoot), editorToURIString(editor), panel,
                        balSdkPath);
            }
        }
        return "";
    }

    static Editor getEditorFor(VirtualFile file, Project project) {
        FileEditor[] allEditors = FileEditorManager.getInstance(project).getAllEditors(file);
        if (allEditors.length > 0 && allEditors[0] instanceof TextEditor) {
            return ((TextEditor) allEditors[0]).getEditor();
        }
        return null;
    }

    private static String getWebviewContent(String sourceRootUri, String docUri, DiagramHtmlPanel myPanel,
                                            String sdkPath) {
        try {
            if (myPanel == null) {
                return "";
            }
            Handlebars handlebars = new Handlebars().with(new ClassPathTemplateLoader(TEMPLATES_CLASSPATH));
            Template scriptTemplate = handlebars.compile(SCRIPT_TEMPLATE_NAME);
            Template webviewTemplate = handlebars.compile(WEBVIEW_TEMPLATE_NAME);

            // Constructs the script to be run when loaded.
            HashMap<String, String> scriptContents = new HashMap<>();
            scriptContents.put("sourceRootUri", sourceRootUri);
            scriptContents.put("docUri", docUri);
            scriptContents.put("getLangClient", handlebars.compile(LANG_CLIENT_TEMPLATE_NAME).text());
            Context scriptContext = Context.newBuilder(scriptContents).resolver(MapValueResolver.INSTANCE).build();

            // Constructs the final webview HTML template.
            HashMap<String, String> webviewContents = new HashMap<>();
            webviewContents.put("resourceRoot", Paths.get(sdkPath, COMPOSER_LIB_RESOURCE_PATH).toUri().toString());
            webviewContents.put("bodyCssClass", BODY_CSS_CLASS);
            webviewContents.put("styles", handlebars.compile(STYLES_TEMPLATE_NAME).text());
            webviewContents.put("fireBug", handlebars.compile(FIREBUG_TEMPLATE_NAME).text());
            webviewContents.put("scripts", scriptTemplate.apply(scriptContext));
            Context webviewContext = Context.newBuilder(webviewContents).resolver(MapValueResolver.INSTANCE).build();
            return webviewTemplate.apply(webviewContext);
        } catch (IOException | RuntimeException e) {
            LOG.warn("Error occurred when constructing webview content: ", e);
            return "";
        }
    }
}
