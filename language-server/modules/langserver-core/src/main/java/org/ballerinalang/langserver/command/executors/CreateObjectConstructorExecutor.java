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
package org.ballerinalang.langserver.command.executors;

import com.google.gson.internal.LinkedTreeMap;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.command.LSCommandExecutor;
import org.ballerinalang.langserver.command.LSCommandExecutorException;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;

import static org.ballerinalang.langserver.command.CommandUtil.applySingleTextEdit;

/**
 * Command executor for creating object constructor.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.command.LSCommandExecutor")
public class CreateObjectConstructorExecutor implements LSCommandExecutor {

    private static final String COMMAND = "CREATE_CONSTRUCTOR";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String documentUri;
        int line = 0;
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();
        for (Object arg : context.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            if (((LinkedTreeMap) arg).get(ARG_KEY).equals(CommandConstants.ARG_KEY_DOC_URI)) {
                documentUri = (String) ((LinkedTreeMap) arg).get(ARG_VALUE);
                textDocumentIdentifier.setUri(documentUri);
                context.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
            } else if (((LinkedTreeMap) arg).get(ARG_KEY).equals(CommandConstants.ARG_KEY_NODE_LINE)) {
                line = Integer.parseInt((String) ((LinkedTreeMap) arg).get(ARG_VALUE));
            }
        }
        LSCompiler lsCompiler = context.get(ExecuteCommandKeys.LS_COMPILER_KEY);
        WorkspaceDocumentManager documentManager = context.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);
        BLangPackage bLangPackage = lsCompiler.getBLangPackage(context, documentManager,
                false, LSCustomErrorStrategy.class, false).getRight();
        context.put(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY, bLangPackage);
        context.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY, bLangPackage.symbol.getName().getValue());
        String relativeSourcePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        BLangPackage srcOwnerPkg = CommonUtil.getSourceOwnerBLangPackage(relativeSourcePath, bLangPackage);
        int finalLine = line;
        
        /*
        In the ideal situation Command execution exception should never throw. If thrown, create constructor command
        has been executed over a non object type node.
         */
        TopLevelNode objectNode = CommonUtil.getCurrentFileTopLevelNodes(srcOwnerPkg, context).stream()
                .filter(topLevelNode -> topLevelNode instanceof BLangTypeDefinition
                        && ((BLangTypeDefinition) topLevelNode).symbol.kind.equals(SymbolKind.OBJECT)
                        && topLevelNode.getPosition().getStartLine() - 1 == finalLine)
                .findAny()
                .orElseThrow(() -> new LSCommandExecutorException("Error Executing Create Constructor Command"));
        List<BLangVariable> fields = ((BLangObjectTypeNode) ((BLangTypeDefinition) objectNode).typeNode).fields;

        DiagnosticPos zeroBasedIndex = CommonUtil.toZeroBasedPosition(CommonUtil.getLastItem(fields).getPosition());
        int lastFieldLine = zeroBasedIndex.getEndLine();
        int lastFieldOffset = zeroBasedIndex.getStartColumn();
        String constructorSnippet = CommandUtil.getObjectConstructorSnippet(fields, lastFieldOffset);
        Range range = new Range(new Position(lastFieldLine + 1, 0), new Position(lastFieldLine + 1, 0));

        return applySingleTextEdit(constructorSnippet, range, textDocumentIdentifier,
                context.get(ExecuteCommandKeys.LANGUAGE_SERVER_KEY).getClient());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
