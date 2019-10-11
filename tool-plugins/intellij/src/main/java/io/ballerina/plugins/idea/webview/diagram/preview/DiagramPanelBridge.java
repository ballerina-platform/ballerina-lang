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
import io.ballerina.plugins.idea.extensions.client.BallerinaEditorEventManager;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTDidChangeResponse;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTResponse;
import io.ballerina.plugins.idea.extensions.server.BallerinaEndpoint;
import io.ballerina.plugins.idea.extensions.server.BallerinaEndpointsResponse;
import io.ballerina.plugins.idea.extensions.server.ModulesResponse;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.editor.EditorEventManagerBase;

import java.util.List;
import java.util.Objects;

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
    public BallerinaEndpoint[] endpoints;

    public DiagramPanelBridge(Project project, VirtualFile file) {
        this.myProject = project;
        this.myFile = file;
    }

    public String getAst() {
        try {
            // Requests AST from the language server.
            BallerinaEditorEventManager editorManager = getEditorManagerFor(myProject, myFile);
            BallerinaASTResponse astResponse = editorManager.getAST();

            // Returns BallerinaASTResponse as a JSON string.
            return new Gson().toJson(astResponse);
        } catch (Exception e) {
            LOG.warn("Error occurred when handling diagram update.", e);
            return new Gson().toJson(new BallerinaASTResponse(null, false));
        }
    }

    public String getProjectAst(String eventData) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject sourceParams = parser.parse(eventData).getAsJsonObject();
            String sourceRoot = sourceParams.get("sourceRoot").getAsString();
            BallerinaEditorEventManager editorManager = getEditorManagerFor(myProject, myFile);
            ModulesResponse astResponse = editorManager.getProjectAST(sourceRoot);

            // Returns BallerinaASTResponse as a JSON string.
            return new Gson().toJson(astResponse);
        } catch (Exception e) {
            LOG.warn("Error occurred when handling diagram update.", e);
            return new Gson().toJson(new BallerinaASTResponse(null, false));
        }
    }

    public void handleDiagramEdit(String eventData) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject didChangeParams = parser.parse(eventData).getAsJsonObject();
            String uri = didChangeParams.get("textDocumentIdentifier").getAsJsonObject().get("uri").getAsString();
            JsonObject ast = didChangeParams.get("ast").getAsJsonObject();

            // Notify AST change to the language server.
            BallerinaEditorEventManager editorManager = getEditorManagerFor(myProject, myFile);
            BallerinaASTDidChangeResponse astDidChangeResponse = editorManager.astDidChange(ast, uri);
        } catch (Exception e) {
            LOG.warn("Error occurred when handling diagram edit event.", e);
        }
    }

    public BallerinaEndpoint[] getEndpoints() {
        try {
            // Requests the available list of endpoints.
            BallerinaEditorEventManager editorManager = getEditorManagerFor(myProject, myFile);
            BallerinaEndpointsResponse response = editorManager.getEndpoints();

            if (response == null) {
                LOG.warn("Error occurred when fetching endpoints response.");
                return new BallerinaEndpoint[0];
            }
            List<BallerinaEndpoint> epList = response.getEndpoints();
            if (epList != null && !epList.isEmpty()) {
                return epList.toArray(new BallerinaEndpoint[0]);
            } else {
                LOG.warn("Received list of endpoints are empty/null.");
                return new BallerinaEndpoint[0];
            }

        } catch (Exception e) {
            LOG.warn("Error occurred when fetching endpoints from the language server.", e);
            return new BallerinaEndpoint[0];
        }
    }

    public void gotoSource(String eventData) {
        JsonParser parser = new JsonParser();
        JsonObject sourceParams = parser.parse(eventData).getAsJsonObject();
        JsonObject rangeStart = sourceParams.get("range").getAsJsonObject().get("start").getAsJsonObject();
        JsonObject rangeEnd = sourceParams.get("range").getAsJsonObject().get("end").getAsJsonObject();

        BallerinaEditorEventManager editorManager = getEditorManagerFor(myProject, myFile);
        editorManager.setFocus(rangeStart, rangeEnd);
    }

    private BallerinaEditorEventManager getEditorManagerFor(Project project, VirtualFile file) {
        Editor editor = BallerinaDiagramUtils.getEditorFor(file, project);
        EditorEventManager manager = EditorEventManagerBase.forEditor(editor);
        BallerinaEditorEventManager editorEventManager = (BallerinaEditorEventManager) manager;
        if (editorEventManager == null) {
            LOG.warn("Editor event manager is null for: " + Objects.requireNonNull(editor).toString());
        }
        return editorEventManager;
    }


    public void log(@Nullable String text) {
        Logger.getInstance(DiagramPanelBridge.class).warn(text);
    }
}
