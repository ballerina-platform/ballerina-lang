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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import io.ballerina.plugins.idea.extensions.editoreventmanager.BallerinaEditorEventManager;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTDidChangeResponse;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTResponse;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.editor.EditorEventManagerBase;

/**
 * DiagramPanelBridge.
 */
@SuppressWarnings("unused")
public class DiagramPanelBridge {

    private static final Logger LOG = Logger.getInstance(DiagramPanelBridge.class);
    private static final NotificationGroup DIAGRAM_NOTIFICATION_GROUP = NotificationGroup
            .toolWindowGroup("Diagram headers group", ToolWindowId.MESSAGES_WINDOW);

    private Project myProject;
    private VirtualFile myFile;

    public DiagramPanelBridge(Project project, VirtualFile file) {
        this.myProject = project;
        this.myFile = file;
    }

    public String getAst() {
        try {
            // Requests the AST from the Ballerina language server.
            Editor editor = BallerinaDiagramUtils.getEditorFor(myFile, myProject);
            EditorEventManager manager = EditorEventManagerBase.forEditor(editor);
            BallerinaEditorEventManager editorManager = (BallerinaEditorEventManager) manager;
            if (editorManager == null) {
                LOG.debug("Editor event manager is null for: " + editor.toString());
                return "";
            }

            // Requests AST from the language server.
            BallerinaASTResponse astResponse = editorManager.getAST();
            if (astResponse == null) {
                LOG.debug("Error occurred when fetching AST response.");
                return "";
            }
            Gson gson = new Gson();
            return gson.toJson(astResponse);
        } catch (Exception e) {
            LOG.warn("Error occurred when handling diagram update.", e);
            return "";
        }
    }

    public void handleDiagramEdit(String eventData) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject didChangeParams = parser.parse(eventData).getAsJsonObject();
            String uri = didChangeParams.get("textDocumentIdentifier").getAsJsonObject().get("uri").getAsString();
            JsonObject ast = didChangeParams.get("ast").getAsJsonObject();

            // Requests the AST from the Ballerina language server.
            Editor editor = BallerinaDiagramUtils.getEditorFor(myFile, myProject);
            EditorEventManager manager = EditorEventManagerBase.forEditor(editor);
            BallerinaEditorEventManager editorManager = (BallerinaEditorEventManager) manager;
            if (editorManager == null) {
                LOG.debug(String.format("Editor event manager is null for: %s", editor.toString()));
                return;
            }

            // Notify AST change to the language server.
            BallerinaASTDidChangeResponse astDidChangeResponse = editorManager.astDidChange(ast, uri);
            if (astDidChangeResponse == null) {
                LOG.debug("Error occurred when fetching astDidChange response.");
            }
        } catch (Exception e) {
            LOG.warn("Error occurred when handling diagram edit event.", e);
        }
    }

    public void log(@Nullable String text) {
        Logger.getInstance(DiagramPanelBridge.class).warn(text);
    }
}
