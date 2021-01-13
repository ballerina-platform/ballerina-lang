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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.command.CommandUtil.applyWorkspaceEdit;

/**
 * Represents the command executor for creating a function.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class CreateFunctionExecutor implements LSCommandExecutor {

    public static final String COMMAND = "CREATE_FUNC";

    /**
     * {@inheritDoc}
     *
     * @param context
     */
    @Override
    public Object execute(ExecuteCommandContext context) throws LSCommandExecutorException {
        String uri = null;
//        String returnType;
//        String returnValue;
//        String funcArgs = "";
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();

        Position position = null;

        for (CommandArgument arg : context.getArguments()) {
            switch (arg.key()) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    uri = arg.valueAs(String.class);
                    textDocumentIdentifier.setUri(uri);
                    break;
                case CommandConstants.ARG_KEY_NODE_POS:
                    position = arg.valueAs(Position.class);
                    break;
                default:
            }
        }

        Optional<Path> filePath = CommonUtil.getPathFromURI(uri);
        if (position == null || filePath.isEmpty()) {
            throw new LSCommandExecutorException("Invalid parameters received for the create function command!");
        }

        SyntaxTree syntaxTree = context.workspace().syntaxTree(filePath.get()).orElseThrow();
        NonTerminalNode matchedNode = CommonUtil.findNode(new Range(position, position), syntaxTree);
        Node identifier = null;
        while (matchedNode != null) {
            if (matchedNode.kind() == SyntaxKind.FUNCTION_CALL) {
                FunctionCallExpressionNode callExprNode = (FunctionCallExpressionNode) matchedNode;
                identifier = callExprNode.functionName();
                break;
            }
            matchedNode = matchedNode.parent();
        }

        if (matchedNode == null) {
            return new LSCommandExecutorException("Couldn't find a matching node");
        }
        SemanticModel semanticModel = context.workspace().semanticModel(filePath.get()).orElseThrow();
        String relPath = context.workspace().relativePath(filePath.get()).orElseThrow();
        TypeSymbol matchedTypeSymbol = semanticModel.type(relPath, identifier.lineRange()).orElse(null);
        if (matchedTypeSymbol == null) {
            return Collections.emptyList();
        }
        //TODO: Need to get return type of the function invocation blocked due to #27211
//        Path path = CommonUtil.getPathFromURI(uri).get();
//        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
//        SyntaxTree syntaxTree;
//        try {
//            syntaxTree = docManager.getTree(path);
//        } catch (WorkspaceDocumentException e) {
//            throw new LSCommandExecutorException("Error while retrieving syntax tree for path: " + path.toString());
//        }

//        BLangInvocation functionNode = null;
//        Position pos = new Position(line, column + 1);
//        PositionDetails cursorDetails = CodeActionUtil.findCursorDetails(new Range(pos, pos), syntaxTree, context);

//        if (functionNode == null) {
//            throw new LSCommandExecutorException("Couldn't find the function node!");
//        }
//        String functionName = functionNode.name.getValue();

//        BLangNode parent = functionNode.parent;
//        BLangPackage packageNode = CommonUtil.getPackageNode(functionNode);

        int eLine = 0;
        int eCol = 0;

//        BLangCompilationUnit cUnit = getCurrentCUnit(context, packageNode);
//        for (TopLevelNode topLevelNode : cUnit.topLevelNodes) {
//            if (topLevelNode.getPosition().lineRange().endLine().line() > eLine) {
//                eLine = topLevelNode.getPosition().lineRange().endLine().line();
//            }
//        }

        List<TextEdit> edits = new ArrayList<>();
//        if (parent != null && packageNode != null) {
//            PackageID currentPkgId = packageNode.packageID;
//            ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);
        //TODO: Fix this when #26382 is avaialble

//            returnType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, parent, context);
//            returnValue = FunctionGenerator.generateReturnValue(importsAcceptor, currentPkgId, parent,
//                                                                "    return {%1};", context);
//            List<String> arguments = FunctionGenerator.getFuncArguments(importsAcceptor, currentPkgId, functionNode,
//                                                                        context);
//            if (arguments != null) {
//                funcArgs = String.join(", ", arguments);
//            }
//            edits.addAll(importsAcceptor.getNewImportTextEdits());
//        } else {
//            throw new LSCommandExecutorException("Error occurred when retrieving function node!");
//        }
        LanguageClient client = context.getLanguageClient();
//        String modifiers = "";
//        boolean prependLineFeed = true;
//        String padding = "";
//        if (functionNode.expr != null) {
//            padding = StringUtils.repeat(' ', 4);
//            BTypeSymbol tSymbol = functionNode.expr.type.tsymbol;
//            Triple<Location, PackageID, Boolean> nodeLocation =
//                    getNodeLocationAndHasFunctions(tSymbol.name.value, context);
//            if (!nodeLocation.getRight()) {
//                prependLineFeed = false;
//            }
//            eLine = nodeLocation.getLeft().lineRange().endLine().line() - 1;
//            String cUnitName = nodeLocation.getLeft().lineRange().filePath();
//            String sourceRoot = context.get(DocumentServiceKeys.SOURCE_ROOT_KEY);
//            String pkgName = tSymbol.pkgID.name.value;
//            String docUri = new File(sourceRoot).toPath().resolve("src").resolve(pkgName)
//                    .resolve(cUnitName).toUri().toString();
//            textDocumentIdentifier.setUri(docUri);
//            if (!nodeLocation.getMiddle().equals(packageNode.packageID)) {
//                modifiers += "public ";
//            }
//        }
        //TODO: Fix this
//        String editText = FunctionGenerator.createFunction(functionName, funcArgs, returnType, returnValue, modifiers,
//                                                           prependLineFeed, padding);
        String editText = "";
        Range range = new Range(new Position(eLine, eCol), new Position(eLine, eCol));
        edits.add(new TextEdit(range, editText));
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(textDocumentIdentifier, edits);
        return applyWorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)), client);
    }

//    private BLangCompilationUnit getCurrentCUnit(LSContext context, BLangPackage packageNode) {
//    /*
//    In windows platform, relative file path key components are separated with "\" while antlr always uses "/"
//     */
//        String relativePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
//        String currentCUnitName = relativePath.replace("\\", "/");
//        BLangPackage sourceOwnerPkg = CommonUtil.getSourceOwnerBLangPackage(relativePath, packageNode);
//
//        Optional<BLangCompilationUnit> currentCUnit = sourceOwnerPkg.getCompilationUnits().stream()
//                .filter(cUnit -> cUnit.name.equals(currentCUnitName))
//                .findAny();
//
//        if (!currentCUnit.isPresent()) {
//            throw new UserErrorException("Not supported due to compilation failures!");
//        }
//        return currentCUnit.get();
//    }

//    private Triple<Location, PackageID, Boolean> getNodeLocationAndHasFunctions(String name,
//                                                                                               LSContext context) {
//        List<BLangPackage> bLangPackages = context.get(DocumentServiceKeys.BLANG_PACKAGES_CONTEXT_KEY);
//        Location pos;
//        PackageID pkgId;
//        boolean hasFunctions = false;
//        for (BLangPackage bLangPackage : bLangPackages) {
//            for (BLangCompilationUnit cUnit : bLangPackage.getCompilationUnits()) {
//                for (TopLevelNode node : cUnit.getTopLevelNodes()) {
//                    if (node instanceof BLangTypeDefinition) {
//                        BLangTypeDefinition typeDefinition = (BLangTypeDefinition) node;
//                        if (typeDefinition.name.value.equals(name)) {
//                            pos = typeDefinition.getPosition();
//                            pkgId = bLangPackage.packageID;
//                            if (typeDefinition.symbol instanceof BObjectTypeSymbol) {
//                                BObjectTypeSymbol typeSymbol = (BObjectTypeSymbol) typeDefinition.symbol;
//                                hasFunctions = typeSymbol.attachedFuncs.size() > 0;
//                            }
//                            return new ImmutableTriple<>(pos, pkgId, hasFunctions);
//                        }
//                    }
//                }
//            }
//        }
//        return new ImmutableTriple<>(null, null, hasFunctions);
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
