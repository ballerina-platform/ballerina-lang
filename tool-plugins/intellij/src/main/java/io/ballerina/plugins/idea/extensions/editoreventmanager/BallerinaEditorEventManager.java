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
package io.ballerina.plugins.idea.extensions.editoreventmanager;

import com.google.gson.JsonElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import io.ballerina.plugins.idea.extensions.client.BallerinaRequestManager;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTRequest;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTResponse;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.wso2.lsp4intellij.client.languageserver.ServerOptions;
import org.wso2.lsp4intellij.client.languageserver.requestmanager.RequestManager;
import org.wso2.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper;
import org.wso2.lsp4intellij.editor.EditorEventManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Editor event manager extension for Ballerina language.
 */
public class BallerinaEditorEventManager extends EditorEventManager {

    private final int getAstTimeout = 2000;

    public BallerinaEditorEventManager(Editor editor, DocumentListener documentListener,
            EditorMouseListener mouseListener, EditorMouseMotionListener mouseMotionListener,
            RequestManager requestManager, ServerOptions serverOptions, LanguageServerWrapper wrapper) {
        super(editor, documentListener, mouseListener, mouseMotionListener, requestManager, serverOptions, wrapper);
    }

    public String getAST() {
        BallerinaRequestManager ballerinaRequestManager = (BallerinaRequestManager) getRequestManager();
        BallerinaASTRequest astRequest = new BallerinaASTRequest();
        astRequest.setDocumentIdentifier(getIdentifier());
        CompletableFuture<BallerinaASTResponse> future = ballerinaRequestManager.ast(astRequest);
        if (future != null) {
            try {
                BallerinaASTResponse response = future.get(getAstTimeout, TimeUnit.MILLISECONDS);
                JsonElement ast = response.getAst();
                return ast != null ? ast.toString() : "";
            } catch (TimeoutException e) {
                LOG.warn(e);
                return null;
            } catch (InterruptedException | JsonRpcException | ExecutionException e) {
                LOG.warn(e);
                // Todo - Enable after fixing
                // wrapper.crashed(e);
                return null;
            }
        }
        return "";
    }

    @Override
    public void documentChanged(DocumentEvent event) {
        if (editor.isDisposed()) {
            return;
        }
        if (event.getDocument() == editor.getDocument()) {
            DidChangeTextDocumentParams changeParams = getChangesParams();
            changeParams.getContentChanges().get(0).setText(editor.getDocument().getText());
            this.getRequestManager().didChange(changeParams);
        } else {
            LOG.error("Wrong document for the EditorEventManager");
        }
    }
}
