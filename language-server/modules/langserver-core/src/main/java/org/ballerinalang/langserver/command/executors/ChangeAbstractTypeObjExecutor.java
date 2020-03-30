/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.command.executors;

import com.google.gson.JsonObject;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.model.Whitespace;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.command.CommandUtil.applyWorkspaceEdit;

/**
 * Represents the change abstract type command executor.
 *
 * @since 1.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class ChangeAbstractTypeObjExecutor implements LSCommandExecutor {

    public static final String COMMAND = "CHANGE_ABSTRACT_TYPE_OBJ";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String documentUri = null;
        int sLine = -1;
        int sCol = -1;
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();

        for (Object arg : context.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            String argKey = ((JsonObject) arg).get(ARG_KEY).getAsString();
            String argVal = ((JsonObject) arg).get(ARG_VALUE).getAsString();
            switch (argKey) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    documentUri = argVal;
                    textDocumentIdentifier.setUri(documentUri);
                    context.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
                    break;
                case CommandConstants.ARG_KEY_NODE_LINE:
                    sLine = Integer.parseInt(argVal);
                    break;
                case CommandConstants.ARG_KEY_NODE_COLUMN:
                    sCol = Integer.parseInt(argVal);
                    break;
                default:
            }
        }

        if (sLine == -1 || sCol == -1 || documentUri == null) {
            throw new LSCommandExecutorException("Invalid parameters received for the change abstract type command!");
        }

        int line = sLine;
        int col = sCol;
        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        try {
            BLangPackage bLangPackage = LSModuleCompiler.getBLangPackage(context, docManager,
                                                                         LSCustomErrorStrategy.class, false, false);

            Optional<BLangTypeDefinition> objType = bLangPackage.topLevelNodes.stream()
                    .filter(topLevelNode -> {
                        if (topLevelNode instanceof BLangTypeDefinition) {
                            org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition pos =
                                    topLevelNode.getPosition();
                            return ((pos.getStartLine() == line || pos.getEndLine() == line ||
                                    (pos.getStartLine() < line && pos.getEndLine() > line)) &&
                                    (pos.getStartColumn() <= col && pos.getEndColumn() <= col));
                        }
                        return false;
                    }).findAny().map(t -> (BLangTypeDefinition) t);

            if (!objType.isPresent()) {
                throw new LSCommandExecutorException("Could not locate the object node!");
            }

            String editText;
            TextEdit textEdit;
            boolean isAbstract = (objType.get().symbol.flags & Flags.ABSTRACT) == Flags.ABSTRACT;
            Iterator<Whitespace> iterator = objType.get().getWS().iterator();

            if (!isAbstract) {
                int colBeforeObjKeyword = objType.get().pos.sCol;
                boolean isFirst = true;
                StringBuilder str = new StringBuilder();
                while (iterator.hasNext()) {
                    Whitespace next = iterator.next();
                    if ("object".equals(next.getPrevious())) {
                        break;
                    }
                    if (!isFirst) {
                        str.append(next.getWs());
                    }
                    str.append(next.getPrevious());
                    isFirst = false;
                }
                colBeforeObjKeyword += str.toString().length();

                editText = " abstract";
                Position position = new Position(objType.get().pos.sLine - 1, colBeforeObjKeyword - 1);
                textEdit = new TextEdit(new Range(position, position), editText);
            } else {
                int colBeforeLeftBrace = objType.get().pos.sCol;
                boolean isFirst = true;
                StringBuilder str = new StringBuilder();
                boolean skipNextWS = false;
                boolean loop = true;
                while (iterator.hasNext() && loop) {
                    Whitespace next = iterator.next();
                    String prev = next.getPrevious();
                    if ("{".equals(prev)) {
                        loop = false;
                    }
                    if (!isFirst) {
                        String ws = next.getWs();
                        if (!skipNextWS) {
                            str.append(ws);
                        } else {
                            skipNextWS = false;
                        }
                        colBeforeLeftBrace += ws.length();
                    }
                    if (!"abstract".equals(prev)) {
                        str.append(prev);
                    } else {
                        skipNextWS = true;
                    }
                    colBeforeLeftBrace += prev.length();
                    isFirst = false;
                }
                colBeforeLeftBrace += str.toString().length();

                editText = str.toString();
                Position start = new Position(objType.get().pos.sLine - 1, objType.get().pos.sCol - 1);
                Position end = new Position(objType.get().pos.sLine - 1, colBeforeLeftBrace - 1);
                textEdit = new TextEdit(new Range(start, end), editText);
            }

            LanguageClient client = context.get(ExecuteCommandKeys.LANGUAGE_CLIENT_KEY);
            List<TextEdit> edits = new ArrayList<>();
            edits.add(textEdit);
            TextDocumentEdit textDocumentEdit = new TextDocumentEdit(textDocumentIdentifier, edits);
            return applyWorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)), client);
        } catch (CompilationFailedException e) {
            throw new LSCommandExecutorException("Error while compiling the source!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
