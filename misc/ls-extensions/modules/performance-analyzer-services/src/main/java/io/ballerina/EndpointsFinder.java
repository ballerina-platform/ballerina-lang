/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.component.Node;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CancellationException;

import static io.ballerina.ProgramAnalyzerNodeVisitor.ACTION_INVOCATION_KEY;
import static io.ballerina.ProgramAnalyzerNodeVisitor.ENDPOINTS_KEY;

public class EndpointsFinder {

    public static String getEndpoints(String fileUri, WorkspaceManager workspaceManager) {

        Path path = Path.of(fileUri);
        try {
            Optional<SemanticModel> semanticModel = workspaceManager.semanticModel(path);
            Optional<Module> module = workspaceManager.module(path);
            if (semanticModel.isEmpty() || module.isEmpty()) {
                return null;
            }
            Module defaultModule = module.get();

            ProgramAnalyzerNodeVisitor nodeVisitor = new ProgramAnalyzerNodeVisitor();
            nodeVisitor.setSemanticModel(semanticModel.get());

            for (DocumentId currentDocumentID : defaultModule.documentIds()) {
                Document document = defaultModule.document(currentDocumentID);

                SyntaxTree syntaxTree = document.syntaxTree();
                nodeVisitor.setDocument(document);
                syntaxTree.rootNode().accept(nodeVisitor);
            }
            GsonBuilder builder = new GsonBuilder();
            builder.serializeNulls();
            Gson gson = builder.create();

            HashMap<String, Object> endpointsAndActionInvocations = nodeVisitor.getActionInvocations();
            Node actionInvocations = (Node) endpointsAndActionInvocations.get(ACTION_INVOCATION_KEY);

            JsonElement endPointsJson = gson.toJsonTree(endpointsAndActionInvocations.get(ENDPOINTS_KEY));
            JsonElement nextNodesJson = gson.toJsonTree(actionInvocations.getNextNode());
            JsonObject actionInvocationsJson = new JsonObject();
            actionInvocationsJson.add("nextNode", nextNodesJson);

            JsonObject json = new JsonObject();
            json.add(ENDPOINTS_KEY, endPointsJson);
            json.add(ACTION_INVOCATION_KEY, actionInvocationsJson);

            return json.toString();
        } catch (CancellationException ignore) {
            // Ignore the cancellation exception
        } catch (Throwable e) {
            //
        }
        return null;
    }
}
