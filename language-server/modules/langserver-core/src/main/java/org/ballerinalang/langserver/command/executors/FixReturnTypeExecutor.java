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
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.command.LSCommandExecutor;
import org.ballerinalang.langserver.command.LSCommandExecutorException;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.types.TypeKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.langserver.command.CommandUtil.applyWorkspaceEdit;

/**
 * Command executor for changing return type of a function.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.command.LSCommandExecutor")
public class FixReturnTypeExecutor implements LSCommandExecutor {

    public static final String COMMAND = "CHANGE_RETURN_TYPE";

    private static final Pattern FQ_TYPE_PATTERN = Pattern.compile("(.*)/([^:]*):(?:.*:)?(.*)");

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String documentUri = null;
        String type = null;
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
                case CommandConstants.ARG_KEY_NODE_TYPE:
                    type = argVal;
                    break;
                default:
            }
        }

        if (sLine == -1 || sCol == -1 || documentUri == null || type == null) {
            throw new LSCommandExecutorException("Invalid parameters received for the change return type command!");
        }

        WorkspaceDocumentManager documentManager = context.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);
        LSCompiler lsCompiler = context.get(ExecuteCommandKeys.LS_COMPILER_KEY);

        List<TextEdit> edits = getReturnTypeTextEdits(sLine, sCol, type, documentUri, documentManager, lsCompiler,
                                                      context);
        if (edits == null) {
            throw new LSCommandExecutorException("Couldn't find the function node!");
        }

        LanguageClient client = context.get(ExecuteCommandKeys.LANGUAGE_SERVER_KEY).getClient();
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(textDocumentIdentifier, edits);
        return applyWorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)), client);
    }

    private static List<TextEdit> getReturnTypeTextEdits(int line, int column, String type, String uri,
                                                         WorkspaceDocumentManager documentManager,
                                                         LSCompiler lsCompiler,
                                                         LSContext context) {
        List<TextEdit> edits = new ArrayList<>();
        Node bLangNode = CommandUtil.getBLangNodeByPosition(line, column, uri, documentManager, lsCompiler, context);
        if (bLangNode instanceof BLangFunction) {
            // Process full-qualified BType name  eg. ballerina/http:Client and if required; add an auto-import
            Matcher matcher = FQ_TYPE_PATTERN.matcher(type);
            String editText = type;
            if (matcher.find() && matcher.groupCount() > 2) {
                String orgName = matcher.group(1);
                String alias = matcher.group(2);
                String typeName = matcher.group(3);
                String pkgId = orgName + "/" + alias;
                PackageID currentPkgId = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY).packageID;
                if (pkgId.equals(currentPkgId.toString()) || ("ballerina".equals(orgName) && "builtin".equals(alias))) {
                    editText = typeName;
                } else {
                    edits.addAll(CommonUtil.getAutoImportTextEdits(context, orgName, alias));
                    editText = alias + UtilSymbolKeys.PKG_DELIMITER_KEYWORD + typeName;
                }
            }

            // Process function node
            Position start = new Position(0, 0);
            Position end = new Position(0, 0);
            BLangFunction func = (BLangFunction) bLangNode;
            if (func.returnTypeNode instanceof BLangValueType
                    && TypeKind.NIL.equals(((BLangValueType) func.returnTypeNode).getTypeKind())
                    && func.returnTypeNode.getWS() == null) {
                // eg. function test() {...}
                start.setLine(func.returnTypeNode.pos.sLine - 1);
                start.setCharacter(func.returnTypeNode.pos.eCol - 1);
                end.setLine(func.returnTypeNode.pos.eLine - 1);
                end.setCharacter(func.returnTypeNode.pos.eCol - 1);
                editText = " returns (" + editText + ")";
            } else {
                // eg. function test() returns () {...}
                start.setLine(func.returnTypeNode.pos.sLine - 1);
                start.setCharacter(func.returnTypeNode.pos.sCol - 1);
                end.setLine(func.returnTypeNode.pos.eLine - 1);
                end.setCharacter(func.returnTypeNode.pos.eCol - 1);
            }
            edits.add(new TextEdit(new Range(start, end), editText));
            return edits;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
