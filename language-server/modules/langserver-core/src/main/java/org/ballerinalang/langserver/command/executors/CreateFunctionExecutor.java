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
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageClient;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static org.ballerinalang.langserver.command.CommandUtil.applyWorkspaceEdit;
import static org.ballerinalang.langserver.command.CommandUtil.getFunctionNode;
import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;

/**
 * Represents the command executor for creating a function.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.command.LSCommandExecutor")
public class CreateFunctionExecutor implements LSCommandExecutor {

    private static final String COMMAND = "CREATE_FUNC";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String documentUri = null;
        String returnType = null;
        String returnValue = null;
        String funcArgs = "";
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();

        int line = -1;
        int column = -1;

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
                    line = Integer.parseInt(argVal);
                    break;
                case CommandConstants.ARG_KEY_NODE_COLUMN:
                    column = Integer.parseInt(argVal);
                    break;
                default:
            }
        }

        if (line == -1 || column == -1 || documentUri == null) {
            throw new LSCommandExecutorException("Invalid parameters received for the create function command!");
        }

        WorkspaceDocumentManager documentManager = context.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);
        LSCompiler lsCompiler = context.get(ExecuteCommandKeys.LS_COMPILER_KEY);

        BLangInvocation functionNode = getFunctionNode(line, column, documentUri, documentManager, lsCompiler, context);
        if (functionNode == null) {
            throw new LSCommandExecutorException("Couldn't find the function node!");
        }
        String functionName = functionNode.name.getValue();

        Path filePath = Paths.get(URI.create(documentUri));
        Path compilationPath = getUntitledFilePath(filePath.toString()).orElse(filePath);
        String fileContent = null;
        try {
            fileContent = documentManager.getFileContent(compilationPath);
        } catch (WorkspaceDocumentException e) {
            throw new LSCommandExecutorException("Error occurred while reading the file:" + filePath.toString(), e);
        }
        BLangNode parent = functionNode.parent;
        BLangPackage packageNode = CommonUtil.getPackageNode(functionNode);

        String[] contentComponents = fileContent.split("\\n|\\r\\n|\\r");
        int totalLines = contentComponents.length;
        int lastNewLineCharIndex = Math.max(fileContent.lastIndexOf('\n'), fileContent.lastIndexOf('\r'));
        int lastCharCol = fileContent.substring(lastNewLineCharIndex + 1).length();

        List<TextEdit> edits = new ArrayList<>();
        if (parent != null && packageNode != null) {
            PackageID currentPkgId = packageNode.packageID;
            BiConsumer<String, String> importsConsumer = (orgName, alias) -> {
                boolean notFound = packageNode.getImports().stream().noneMatch(
                        pkg -> (pkg.orgName.value.equals(orgName) && pkg.alias.value.equals(alias))
                );
                if (notFound) {
                    String pkgName = orgName + "/" + alias;
                    edits.add(addPackage(pkgName, packageNode, context));
                }
            };
            returnType = CommonUtil.FunctionGenerator.generateTypeDefinition(importsConsumer, currentPkgId, parent);
            returnValue = CommonUtil.FunctionGenerator.generateReturnValue(importsConsumer, currentPkgId, parent,
                                                                           "    return {%1};");
            List<String> arguments = CommonUtil.FunctionGenerator.getFuncArguments(importsConsumer, currentPkgId,
                                                                                   functionNode);
            if (arguments != null) {
                funcArgs = String.join(", ", arguments);
            }
        } else {
            throw new LSCommandExecutorException("Error occurred when retrieving function node!");
        }
        LanguageClient client = context.get(ExecuteCommandKeys.LANGUAGE_SERVER_KEY).getClient();
        String editText = CommonUtil.FunctionGenerator.createFunction(functionName, funcArgs, returnType, returnValue);
        Range range = new Range(new Position(totalLines, lastCharCol + 1), new Position(totalLines + 3, lastCharCol));
        edits.add(new TextEdit(range, editText));
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(textDocumentIdentifier, edits);
        return applyWorkspaceEdit(Collections.singletonList(textDocumentEdit), client);
    }

    private TextEdit addPackage(String pkgName, BLangPackage srcOwnerPkg, LSContext context) {
        DiagnosticPos pos = null;

        // Filter the imports except the runtime import
        List<BLangImportPackage> imports = CommonUtil.getCurrentFileImports(srcOwnerPkg, context);

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
