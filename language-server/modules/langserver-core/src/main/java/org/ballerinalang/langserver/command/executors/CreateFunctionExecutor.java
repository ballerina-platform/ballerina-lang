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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.command.LSCommandExecutor;
import org.ballerinalang.langserver.command.LSCommandExecutorException;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static org.ballerinalang.langserver.command.CommandUtil.applyWorkspaceEdit;
import static org.ballerinalang.langserver.command.CommandUtil.getFunctionInvocationNode;
import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;

/**
 * Represents the command executor for creating a function.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.command.LSCommandExecutor")
public class CreateFunctionExecutor implements LSCommandExecutor {

    public static final String COMMAND = "CREATE_FUNC";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String documentUri = null;
        String returnType;
        String returnValue;
        String funcArgs = "";
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();

        int line = -1;
        int column = -1;

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

        BLangInvocation functionNode;
        try {
            functionNode = getFunctionInvocationNode(line, column, documentUri, documentManager, context);
        } catch (CompilationFailedException e) {
            throw new LSCommandExecutorException("Error while compiling the source!");
        }
        if (functionNode == null) {
            throw new LSCommandExecutorException("Couldn't find the function node!");
        }
        String functionName = functionNode.name.getValue();

        Path filePath = Paths.get(URI.create(documentUri));
        Path compilationPath = getUntitledFilePath(filePath.toString()).orElse(filePath);
        String fileContent;
        try {
            fileContent = documentManager.getFileContent(compilationPath);
        } catch (WorkspaceDocumentException e) {
            throw new LSCommandExecutorException("Error occurred while reading the file:" + filePath.toString(), e);
        }
        BLangNode parent = functionNode.parent;
        BLangPackage packageNode = CommonUtil.getPackageNode(functionNode);

        String[] contentComponents = fileContent.split("\\n|\\r\\n|\\r");
        int eLine = contentComponents.length;
        int lastNewLineCharIndex = Math.max(fileContent.lastIndexOf('\n'), fileContent.lastIndexOf('\r'));
        int eCol = fileContent.substring(lastNewLineCharIndex + 1).length();

        List<TextEdit> edits = new ArrayList<>();
        if (parent != null && packageNode != null) {
            PackageID currentPkgId = packageNode.packageID;
            BiConsumer<String, String> importsAcceptor = (orgName, alias) -> {
                boolean notFound = packageNode.getImports().stream().noneMatch(
                        pkg -> (pkg.orgName.value.equals(orgName) && pkg.alias.value.equals(alias))
                );
                if (notFound) {
                    String pkgName = orgName + "/" + alias;
                    edits.add(addPackage(pkgName, context));
                }
            };
            returnType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, parent);
            returnValue = FunctionGenerator.generateReturnValue(importsAcceptor, currentPkgId, parent,
                                                                "    return {%1};");
            List<String> arguments = FunctionGenerator.getFuncArguments(importsAcceptor, currentPkgId, functionNode,
                                                                        context);
            if (arguments != null) {
                funcArgs = String.join(", ", arguments);
            }
        } else {
            throw new LSCommandExecutorException("Error occurred when retrieving function node!");
        }
        LanguageClient client = context.get(ExecuteCommandKeys.LANGUAGE_SERVER_KEY).getClient();
        String modifiers = "";
        boolean prependLineFeed = true;
        String padding = "";
        if (functionNode.expr != null) {
            padding = StringUtils.repeat(' ', 4);
            BTypeSymbol tSymbol = functionNode.expr.type.tsymbol;
            Pair<DiagnosticPos, Boolean> nodeLocation = getNodeLocationAndHasFunctions(tSymbol.name.value, context);
            if (!nodeLocation.getRight()) {
                prependLineFeed = false;
            }
            eLine = nodeLocation.getLeft().eLine - 1;
            eCol = 0;
            String cUnitName = nodeLocation.getLeft().src.cUnitName;
            String sourceRoot = context.get(DocumentServiceKeys.SOURCE_ROOT_KEY);
            String pkgName = nodeLocation.getLeft().src.pkgID.name.toString();
            String uri = new File(sourceRoot).toPath().resolve("src").resolve(pkgName)
                    .resolve(cUnitName).toUri().toString();
            textDocumentIdentifier.setUri(uri);
            if (!nodeLocation.getLeft().src.pkgID.equals(functionNode.pos.src.pkgID)) {
                modifiers += "public ";
            }
        }
        String editText = FunctionGenerator.createFunction(functionName, funcArgs, returnType, returnValue, modifiers,
                                                           prependLineFeed, padding);
        Range range = new Range(new Position(eLine, eCol), new Position(eLine, eCol));
        edits.add(new TextEdit(range, editText));
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(textDocumentIdentifier, edits);
        return applyWorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)), client);
    }

    private Pair<DiagnosticPos, Boolean> getNodeLocationAndHasFunctions(String name, LSContext context) {
        List<BLangPackage> bLangPackages = context.get(DocumentServiceKeys.BLANG_PACKAGES_CONTEXT_KEY);
        DiagnosticPos pos;
        boolean hasFunctions = false;
        for (BLangPackage bLangPackage : bLangPackages) {
            for (BLangCompilationUnit cUnit : bLangPackage.getCompilationUnits()) {
                for (TopLevelNode node : cUnit.getTopLevelNodes()) {
                    if (node instanceof BLangTypeDefinition) {
                        BLangTypeDefinition typeDefinition = (BLangTypeDefinition) node;
                        if (typeDefinition.name.value.equals(name)) {
                            pos = typeDefinition.getPosition();
                            if (typeDefinition.symbol instanceof BObjectTypeSymbol) {
                                BObjectTypeSymbol typeSymbol = (BObjectTypeSymbol) typeDefinition.symbol;
                                hasFunctions = typeSymbol.attachedFuncs.size() > 0;
                            }
                            return new ImmutablePair<>(pos, hasFunctions);
                        }
                    }
                }
            }
        }
        return new ImmutablePair<>(null, hasFunctions);
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
