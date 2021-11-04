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
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Range;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CancellationException;

import static io.ballerina.PerformanceAnalyzerNodeVisitor.ACTION_INVOCATION_KEY;
import static io.ballerina.PerformanceAnalyzerNodeVisitor.ENDPOINTS_KEY;

/**
 * Implements functionality to find endpoints and action invocations.
 *
 * @since 2.0.0
 */
public class EndpointsFinder {

    /**
     * Get endpoints and action invocations in a given range of a file.
     *
     * @param fileUri          .bal file url
     * @param workspaceManager ballerina workspaceManager
     * @param range            range of file to find invocations
     * @return json of endpoints and invocations
     */
    public static JsonObject getEndpoints(String fileUri, WorkspaceManager workspaceManager,
                                          Range range) {

        Path path = Path.of(fileUri);
        String file = StringUtils.substringAfterLast(fileUri, File.separator);
        try {
            Optional<SemanticModel> semanticModel = workspaceManager.semanticModel(path);
            Optional<Module> module = workspaceManager.module(path);
            if (semanticModel.isEmpty() || module.isEmpty()) {
                return null;
            }
            Module defaultModule = module.get();

            if (range == null) {
                return null;
            }

            PerformanceAnalyzerNodeVisitor nodeVisitor =
                    new PerformanceAnalyzerNodeVisitor(semanticModel.get(), file, range);
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

            return json;
        } catch (CancellationException ignore) {
            // Ignore the cancellation exception
        } catch (Throwable e) {
            //
        }
        return null;
    }
}
