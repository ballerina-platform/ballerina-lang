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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.util.references.ReferencesKeys;
import org.ballerinalang.langserver.util.references.ReferencesUtil;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
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
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.File;
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
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String uri = null;
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
                    uri = argVal;
                    textDocumentIdentifier.setUri(uri);
                    context.put(DocumentServiceKeys.FILE_URI_KEY, uri);
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

        if (line == -1 || column == -1 || uri == null) {
            throw new LSCommandExecutorException("Invalid parameters received for the create function command!");
        }

        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);

        BLangInvocation functionNode = null;
        try {
            LSDocumentIdentifier lsDocument = docManager.getLSDocument(CommonUtil.getPathFromURI(uri).get());
            Position pos = new Position(line, column + 1);
            context.put(ReferencesKeys.OFFSET_CURSOR_N_TRY_NEXT_BEST, true);
            context.put(ReferencesKeys.DO_NOT_SKIP_NULL_SYMBOLS, true);
            SymbolReferencesModel.Reference refAtCursor = ReferencesUtil.getReferenceAtCursor(context, lsDocument, pos);
            BLangNode bLangNode = refAtCursor.getbLangNode();
            if (bLangNode instanceof BLangInvocation) {
                functionNode = (BLangInvocation) bLangNode;
            }
        } catch (WorkspaceDocumentException | CompilationFailedException | TokenOrSymbolNotFoundException e) {
            throw new LSCommandExecutorException("Error while compiling the source!");
        }
        if (functionNode == null) {
            throw new LSCommandExecutorException("Couldn't find the function node!");
        }
//        String functionName = functionNode.name.getValue();

        BLangNode parent = functionNode.parent;
        BLangPackage packageNode = CommonUtil.getPackageNode(functionNode);

        int eLine = 0;
        int eCol = 0;

        BLangCompilationUnit cUnit = getCurrentCUnit(context, packageNode);
        for (TopLevelNode topLevelNode : cUnit.topLevelNodes) {
            if (topLevelNode.getPosition().getEndLine() > eLine) {
                eLine = topLevelNode.getPosition().getEndLine();
            }
        }

        List<TextEdit> edits = new ArrayList<>();
        if (parent != null && packageNode != null) {
//            PackageID currentPkgId = packageNode.packageID;
            ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);
            //TODO: Fix this when #26382 is avaialble
//            returnType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, parent, context);
//            returnValue = FunctionGenerator.generateReturnValue(importsAcceptor, currentPkgId, parent,
//                                                                "    return {%1};", context);
//            List<String> arguments = FunctionGenerator.getFuncArguments(importsAcceptor, currentPkgId, functionNode,
//                                                                        context);
//            if (arguments != null) {
//                funcArgs = String.join(", ", arguments);
//            }
            edits.addAll(importsAcceptor.getNewImportTextEdits());
        } else {
            throw new LSCommandExecutorException("Error occurred when retrieving function node!");
        }
        LanguageClient client = context.get(ExecuteCommandKeys.LANGUAGE_CLIENT_KEY);
//        String modifiers = "";
//        boolean prependLineFeed = true;
//        String padding = "";
        if (functionNode.expr != null) {
//            padding = StringUtils.repeat(' ', 4);
            BTypeSymbol tSymbol = functionNode.expr.type.tsymbol;
            Pair<DiagnosticPos, Boolean> nodeLocation = getNodeLocationAndHasFunctions(tSymbol.name.value, context);
//            if (!nodeLocation.getRight()) {
//                prependLineFeed = false;
//            }
            eLine = nodeLocation.getLeft().eLine - 1;
            String cUnitName = nodeLocation.getLeft().src.cUnitName;
            String sourceRoot = context.get(DocumentServiceKeys.SOURCE_ROOT_KEY);
            String pkgName = nodeLocation.getLeft().src.pkgID.name.toString();
            String docUri = new File(sourceRoot).toPath().resolve("src").resolve(pkgName)
                    .resolve(cUnitName).toUri().toString();
            textDocumentIdentifier.setUri(docUri);
//            if (!nodeLocation.getLeft().src.pkgID.equals(functionNode.pos.src.pkgID)) {
//                modifiers += "public ";
//            }
        }
        //TODO: Fix this
//        String editText = FunctionGenerator.createFunction(functionName, funcArgs, returnType, returnValue, modifiers,
//                                                           prependLineFeed, padding);
        String editText = "";
        Range range = new Range(new Position(eLine, eCol), new Position(eLine, eCol));
        edits.add(new TextEdit(range, editText));
        TextDocumentEdit textDocumentEdit = new TextDocumentEdit(textDocumentIdentifier, edits);
        return applyWorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)), client);
    }

    private BLangCompilationUnit getCurrentCUnit(LSContext context, BLangPackage packageNode) {
    /*
    In windows platform, relative file path key components are separated with "\" while antlr always uses "/"
     */
        String relativePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        String currentCUnitName = relativePath.replace("\\", "/");
        BLangPackage sourceOwnerPkg = CommonUtil.getSourceOwnerBLangPackage(relativePath, packageNode);

        Optional<BLangCompilationUnit> currentCUnit = sourceOwnerPkg.getCompilationUnits().stream()
                .filter(cUnit -> cUnit.name.equals(currentCUnitName))
                .findAny();

        if (!currentCUnit.isPresent()) {
            throw new UserErrorException("Not supported due to compilation failures!");
        }
        return currentCUnit.get();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
