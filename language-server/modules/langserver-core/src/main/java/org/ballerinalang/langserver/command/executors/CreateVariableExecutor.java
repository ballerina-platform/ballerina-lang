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

import com.google.gson.JsonObject;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import static org.ballerinalang.langserver.command.CommandUtil.applyWorkspaceEdit;
import static org.ballerinalang.langserver.command.CommandUtil.getFunctionInvocationNode;
import static org.ballerinalang.langserver.common.utils.CommonUtil.createVariableDeclaration;

/**
 * Represents the create variable command executor.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class CreateVariableExecutor implements LSCommandExecutor {

    public static final String COMMAND = "CREATE_VAR";

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
            throw new LSCommandExecutorException("Invalid parameters received for the create variable command!");
        }

        WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        BLangInvocation functionNode;
        try {
            functionNode = getFunctionInvocationNode(sLine, sCol, documentUri, documentManager, context);
        } catch (CompilationFailedException e) {
            throw new LSCommandExecutorException("Error while compiling the source!");
        }
        if (functionNode == null) {
            throw new LSCommandExecutorException("Couldn't find the function node!");
        }
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        BLangPackage packageNode = CommonUtil.getPackageNode(functionNode);
        Set<String> nameEntries = CommonUtil.getAllNameEntries(functionNode, compilerContext);
        String variableName = CommonUtil.generateVariableName(functionNode, nameEntries);

        if (packageNode == null) {
            throw new LSCommandExecutorException("Package node cannot be null");
        }
        PackageID currentPkgId = packageNode.packageID;
        List<TextEdit> edits = new ArrayList<>();
        BiConsumer<String, String> importsAcceptor = (orgName, alias) -> {
            boolean notFound = packageNode.getImports().stream().noneMatch(
                    pkg -> (pkg.orgName.value.equals(orgName) && pkg.alias.value.equals(alias))
            );
            if (notFound) {
                String pkgName = orgName + "/" + alias;
                edits.add(addPackage(pkgName, context));
            }
        };

        String variableType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId,
                                                                                  functionNode.type);

        String editText = createVariableDeclaration(variableName, variableType);
        Position position = new Position(functionNode.pos.sLine - 1, functionNode.pos.sCol - 1);

        LanguageClient client = context.get(ExecuteCommandKeys.LANGUAGE_CLIENT_KEY);
        edits.add(new TextEdit(new Range(position, position), editText));
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(textDocumentIdentifier, edits);
        return applyWorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)), client);
    }

    private TextEdit addPackage(String pkgName, LSContext context) {
        DiagnosticPos pos = null;

        // Filter the imports except the runtime import
        List<BLangImportPackage> imports = CommonUtil.getCurrentFileImports(context);

        if (!imports.isEmpty()) {
            BLangImportPackage lastImport = CommonUtil.getLastItem(imports);
            pos = lastImport.getPosition();
        }

        int endCol = 0;
        int endLine = pos == null ? 0 : pos.getEndLine();

        String editText = "import " + pkgName + ";\n";
        Range range = new Range(new Position(endLine, endCol), new Position(endLine, endCol));
        return new TextEdit(range, editText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
