/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.command;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.position.PositionTreeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.common.LSDocumentIdentifierImpl;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.compiler.LSClientLogger.logError;
import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;

/**
 * Utilities for the command related operations.
 */
public class CommandUtil {

    private CommandUtil() {
    }

    public static Pair<BLangNode, Object> getBLangNode(int line, int column, String uri,
                                                       WorkspaceDocumentManager documentManager, LSContext context)
            throws CompilationFailedException {
        Position position = new Position();
        position.setLine(line);
        position.setCharacter(column + 1);
        context.put(DocumentServiceKeys.FILE_URI_KEY, uri);
        TextDocumentIdentifier identifier = new TextDocumentIdentifier(uri);
        context.put(DocumentServiceKeys.POSITION_KEY, new TextDocumentPositionParams(identifier, position));
        List<BLangPackage> bLangPackages = LSModuleCompiler.getBLangPackages(context, documentManager,
                                                                             LSCustomErrorStrategy.class, true, false,
                                                                             true);
        context.put(DocumentServiceKeys.BLANG_PACKAGES_CONTEXT_KEY, bLangPackages);
        // Get the current package.
        BLangPackage currentBLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        // Run the position calculator for the current package.
        PositionTreeVisitor positionTreeVisitor = new PositionTreeVisitor(context);
        currentBLangPackage.accept(positionTreeVisitor);
        return new ImmutablePair<>(context.get(NodeContextKeys.NODE_KEY),
                                   context.get(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY));
    }

    /**
     * Sends a message to the language server client.
     *
     * @param client      Language Server client
     * @param messageType message type
     * @param message     message
     */
    public static void notifyClient(LanguageClient client, MessageType messageType, String message) {
        client.showMessage(new MessageParams(messageType, message));
    }

    /**
     * Clears diagnostics of the client by sending an text edit event.
     *
     * @param client      Language Server client
     * @param diagHelper  diagnostics helper
     * @param documentUri Current text document URI
     * @param context     {@link LSContext}
     */
    public static void clearDiagnostics(ExtendedLanguageClient client, DiagnosticsHelper diagHelper, String documentUri,
                                        LSContext context) {
        context.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        try {
            LSDocumentIdentifier lsDocument = new LSDocumentIdentifierImpl(documentUri);
            diagHelper.compileAndSendDiagnostics(client, context, lsDocument, docManager);
        } catch (CompilationFailedException e) {
            String msg = "Computing 'diagnostics' failed!";
            TextDocumentIdentifier identifier = new TextDocumentIdentifier(documentUri);
            logError(msg, e, identifier, (Position) null);
        }
    }

    /**
     * Apply a given single text edit.
     *
     * @param editText   Edit text to be inserted
     * @param range      Line Range to be processed
     * @param identifier Document identifier
     * @param client     Language Client
     * @return {@link ApplyWorkspaceEditParams}     Workspace edit params
     */
    public static ApplyWorkspaceEditParams applySingleTextEdit(String editText, Range range,
                                                               VersionedTextDocumentIdentifier identifier,
                                                               LanguageClient client) {

        ApplyWorkspaceEditParams applyWorkspaceEditParams = new ApplyWorkspaceEditParams();
        TextEdit textEdit = new TextEdit(range, editText);
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(identifier,
                                                                 Collections.singletonList(textEdit));
        Either<TextDocumentEdit, ResourceOperation> documentChange = Either.forLeft(textDocumentEdit);
        WorkspaceEdit workspaceEdit = new WorkspaceEdit(Collections.singletonList(documentChange));
        applyWorkspaceEditParams.setEdit(workspaceEdit);
        if (client != null) {
            client.applyEdit(applyWorkspaceEditParams);
        }
        return applyWorkspaceEditParams;
    }

    /**
     * Apply a workspace edit for the current instance.
     *
     * @param documentChanges List of either document edits or set of resource changes for current session
     * @param client          Language Client
     * @return {@link Object}   workspace edit parameters
     */
    public static Object applyWorkspaceEdit(List<Either<TextDocumentEdit, ResourceOperation>> documentChanges,
                                            LanguageClient client) {
        WorkspaceEdit workspaceEdit = new WorkspaceEdit(documentChanges);
        ApplyWorkspaceEditParams applyWorkspaceEditParams = new ApplyWorkspaceEditParams(workspaceEdit);
        if (client != null) {
            client.applyEdit(applyWorkspaceEditParams);
        }
        return applyWorkspaceEditParams;
    }

    public static BLangObjectTypeNode getObjectNode(int line, int column, String uri,
                                                    WorkspaceDocumentManager documentManager, LSContext context)
            throws CompilationFailedException {
        Pair<BLangNode, Object> bLangNode = getBLangNode(line, column, uri, documentManager, context);
        if (bLangNode.getLeft() instanceof BLangObjectTypeNode) {
            return (BLangObjectTypeNode) bLangNode.getLeft();
        }
        if (bLangNode.getRight() instanceof BLangObjectTypeNode) {
            return (BLangObjectTypeNode) bLangNode.getRight();
        } else {
            BLangNode parent = bLangNode.getLeft().parent;
            while (parent != null) {
                if (parent instanceof BLangObjectTypeNode) {
                    return (BLangObjectTypeNode) parent;
                }
                parent = parent.parent;
            }
            return null;
        }
    }

    public static BLangInvocation getFunctionInvocationNode(int line, int column, String uri,
                                                            WorkspaceDocumentManager documentManager, LSContext context)
            throws CompilationFailedException {
        Pair<BLangNode, Object> bLangNode = getBLangNode(line, column, uri, documentManager, context);
        if (bLangNode.getLeft() instanceof BLangInvocation) {
            return (BLangInvocation) bLangNode.getLeft();
        } else if (bLangNode.getRight() instanceof BLangInvocation) {
            return (BLangInvocation) bLangNode.getRight();
        } else {
            BLangNode parent = bLangNode.getLeft().parent;
            while (parent != null) {
                if (parent instanceof BLangInvocation) {
                    return (BLangInvocation) parent;
                }
                parent = parent.parent;
            }
            return null;
        }
    }

    /**
     * Get content of range of a given file uri.
     *
     * @param documentManager   document manager
     * @param uri   file uri
     * @param range content range
     * @return content of range
     * @throws WorkspaceDocumentException   when file not found
     * @throws IOException  when I/O error occurs
     */
    public static String getContentOfRange(WorkspaceDocumentManager documentManager, String uri, Range range)
            throws WorkspaceDocumentException, IOException {
        Optional<Path> filePath = CommonUtil.getPathFromURI(uri);
        if (!filePath.isPresent()) {
            return "";
        }
        Path compilationPath = getUntitledFilePath(filePath.toString()).orElse(filePath.get());
        String fileContent = documentManager.getFileContent(compilationPath);

        BufferedReader reader = new BufferedReader(new StringReader(fileContent));
        StringBuilder capture = new StringBuilder();
        int lineNum = 1;
        int sLine = range.getStart().getLine() + 1;
        int eLine = range.getEnd().getLine() + 1;
        int sChar = range.getStart().getCharacter();
        int eChar = range.getEnd().getCharacter();
        String line;
        while ((line = reader.readLine()) != null && lineNum <= eLine) {
            if (lineNum >= sLine) {
                if (sLine == eLine) {
                    // single line range
                    capture.append(line, sChar, eChar);
                    if (line.length() == eChar) {
                        capture.append(System.lineSeparator());
                    }
                } else if (lineNum == sLine) {
                    // range start line
                    capture.append(line.substring(sChar)).append(System.lineSeparator());
                } else if (lineNum == eLine) {
                    // range end line
                    capture.append(line, 0, eChar);
                    if (line.length() == eChar) {
                        capture.append(System.lineSeparator());
                    }
                } else {
                    // range middle line
                    capture.append(line).append(System.lineSeparator());
                }
            }
            lineNum++;
        }
        return capture.toString();
    }
}
