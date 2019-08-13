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
package io.ballerina.plugins.idea.extensions.client;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTDidChange;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTDidChangeResponse;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTRequest;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTResponse;
import io.ballerina.plugins.idea.extensions.server.BallerinaEndpointsResponse;
import io.ballerina.plugins.idea.extensions.server.ModulesRequest;
import io.ballerina.plugins.idea.extensions.server.ModulesResponse;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.client.languageserver.ServerOptions;
import org.wso2.lsp4intellij.client.languageserver.requestmanager.RequestManager;
import org.wso2.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.utils.ApplicationUtils;
import org.wso2.lsp4intellij.utils.DocumentUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Editor event manager extension for Ballerina language.
 */
public class BallerinaEditorEventManager extends EditorEventManager {

    private static final Logger LOG = Logger.getInstance(BallerinaEditorEventManager.class);
    private static final int TIMEOUT_AST = 3000;
    private static final int TIMEOUT_PROJECT_AST = 5000;
    private static final int TIMEOUT_ENDPOINTS = 2000;

    public BallerinaEditorEventManager(Editor editor, DocumentListener documentListener,
                                       EditorMouseListener mouseListener, EditorMouseMotionListener mouseMotionListener,
                                       RequestManager requestManager, ServerOptions serverOptions,
                                       LanguageServerWrapper wrapper) {
        super(editor, documentListener, mouseListener, mouseMotionListener, requestManager, serverOptions, wrapper);
    }

    @Nullable
    public BallerinaASTResponse getAST() throws TimeoutException, ExecutionException, InterruptedException {
        BallerinaRequestManager ballerinaRequestManager = (BallerinaRequestManager) getRequestManager();
        BallerinaASTRequest astRequest = new BallerinaASTRequest();
        astRequest.setDocumentIdentifier(getIdentifier());
        CompletableFuture<BallerinaASTResponse> future = ballerinaRequestManager.ast(astRequest);
        if (future != null) {
            try {
                return future.get(TIMEOUT_AST, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw e;
            } catch (InterruptedException | JsonRpcException | ExecutionException e) {
                // Todo - Enable after fixing
                // wrapper.crashed(e);
                throw e;
            }
        }
        return null;
    }

    @Nullable
    public ModulesResponse getProjectAST(String sourceRoot) throws TimeoutException, ExecutionException,
            InterruptedException {
        BallerinaRequestManager ballerinaRequestManager = (BallerinaRequestManager) getRequestManager();
        ModulesRequest ballerinaModuleRequest = new ModulesRequest();
        ballerinaModuleRequest.setSourceRoot(sourceRoot);
        CompletableFuture<ModulesResponse> future = ballerinaRequestManager.modules(ballerinaModuleRequest);
        if (future != null) {
            try {
                return future.get(TIMEOUT_PROJECT_AST, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw e;
            } catch (InterruptedException | JsonRpcException | ExecutionException e) {
                // Todo - Enable after fixing
                // wrapper.crashed(e);
                throw e;
            }
        }
        return null;
    }

    @Nullable
    public BallerinaASTDidChangeResponse astDidChange(JsonObject ast, String uri) throws
            TimeoutException, ExecutionException, InterruptedException {

        BallerinaRequestManager ballerinaRequestManager = (BallerinaRequestManager) getRequestManager();
        BallerinaASTDidChange didChangeNotification = new BallerinaASTDidChange();
        didChangeNotification.setTextDocumentIdentifier(new VersionedTextDocumentIdentifier(uri, Integer.MAX_VALUE));
        didChangeNotification.setAst(ast);
        CompletableFuture<BallerinaASTDidChangeResponse> future = ballerinaRequestManager
                .astDidChange(didChangeNotification);
        if (future != null) {
            try {
                return future.get(TIMEOUT_AST, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw e;
            } catch (InterruptedException | JsonRpcException | ExecutionException e) {
                // Todo - Enable after fixing
                // wrapper.crashed(e);
                throw e;
            }
        }
        return null;
    }

    public BallerinaEndpointsResponse getEndpoints() {
        BallerinaRequestManager ballerinaRequestManager = (BallerinaRequestManager) getRequestManager();
        CompletableFuture<BallerinaEndpointsResponse> future = ballerinaRequestManager
                .endpoints();
        if (future != null) {
            try {
                return future.get(TIMEOUT_ENDPOINTS, TimeUnit.MILLISECONDS);
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
        return null;
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

    public void setFocus(JsonObject start, JsonObject end) {
        try {
            Position startPos = new Position(start.get("line").getAsInt() - 1, start.get("character").getAsInt() - 1);
            Position endPos = new Position(end.get("line").getAsInt() - 1, end.get("character").getAsInt() - 1);
            int startOffset = DocumentUtils.LSPPosToOffset(editor, startPos);
            int endOffset = DocumentUtils.LSPPosToOffset(editor, endPos);
            ApplicationUtils.invokeLater(() -> {
                // Scrolls to the given offset.
                editor.getScrollingModel().scrollTo(new LogicalPosition(startPos.getLine(), startPos.getCharacter()),
                        ScrollType.CENTER);
                // Highlights selected range.
                editor.getSelectionModel().setSelection(startOffset, endOffset);
            });
        } catch (Exception e) {
            LOG.warn("Couldn't process source focus request from diagram editor.", e);
        }
    }
}
