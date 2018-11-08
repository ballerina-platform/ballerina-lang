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
import org.ballerinalang.langserver.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.command.LSCommandExecutor;
import org.ballerinalang.langserver.command.LSCommandExecutorException;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageClient;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashSet;
import java.util.Set;

import static org.ballerinalang.langserver.command.CommandUtil.applySingleTextEdit;
import static org.ballerinalang.langserver.command.CommandUtil.getFunctionNode;
import static org.ballerinalang.langserver.common.utils.CommonUtil.createVariableDeclaration;

/**
 * Represents the create variable command executor.
 * 
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.command.LSCommandExecutor")
public class CreateVariableExecutor implements LSCommandExecutor {

    private static final String COMMAND = "CREATE_VAR";

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
            String argKey = ((LinkedTreeMap) arg).get(ARG_KEY).toString();
            String argVal = ((LinkedTreeMap) arg).get(ARG_VALUE).toString();
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
            throw new LSCommandExecutorException("Invalid parameters received for the create variable command!");
        }

        WorkspaceDocumentManager documentManager = context.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);
        LSCompiler lsCompiler = context.get(ExecuteCommandKeys.LS_COMPILER_KEY);
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);

        BLangInvocation functionNode = getFunctionNode(sLine, sCol, documentUri, documentManager, lsCompiler, context);
        String variableName = CommonUtil.generateName(1, getAllEntries(functionNode, compilerContext));
        String variableType = CommonUtil.FunctionGenerator.getFuncReturnSignature(functionNode.type);

        String editText = createVariableDeclaration(variableName, variableType);
        Position position = new Position(sLine - 1, sCol - 1);

        LanguageClient client = context.get(ExecuteCommandKeys.LANGUAGE_SERVER_KEY).getClient();
        return applySingleTextEdit(editText, new Range(position, position), textDocumentIdentifier, client);
    }

    private static Set<String> getAllEntries(BLangInvocation functionNode, CompilerContext context) {
        Set<String> strings = new HashSet<>();
        BLangPackage packageNode = CommonUtil.getPackageNode(functionNode);
        if (packageNode != null) {
            packageNode.getGlobalVariables().forEach(globalVar -> strings.add(globalVar.name.value));
            packageNode.getGlobalEndpoints().forEach(endpoint -> strings.add(endpoint.getName().getValue()));
            packageNode.getServices().forEach(service -> strings.add(service.name.value));
            packageNode.getFunctions().forEach(func -> strings.add(func.name.value));
        }
        BLangNode bLangNode = functionNode.parent;
        while (bLangNode != null && !(bLangNode instanceof BLangBlockStmt)) {
            bLangNode = bLangNode.parent;
        }
        if(bLangNode != null) {
            SymbolResolver symbolResolver = SymbolResolver.getInstance(context);
            SymbolTable symbolTable = SymbolTable.getInstance(context);
            BLangBlockStmt blockStmt = (BLangBlockStmt) bLangNode;
//            symbolTable.pkgEnvMap.get()
//            SymbolEnv.createBlockEnv(blockStmt, blockStmt.);
//            Map<Name, Scope.ScopeEntry> entries = symbolResolver.getAllVisibleInScopeSymbols();
//            entries.forEach((name, scopeEntry) -> strings.add(name.value));
        }
        return strings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
