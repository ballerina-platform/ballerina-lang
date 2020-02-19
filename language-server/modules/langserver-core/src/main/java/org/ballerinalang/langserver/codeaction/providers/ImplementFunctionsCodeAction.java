/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers;

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Code Action provider for implementing functions of an object.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ImplementFunctionsCodeAction extends AbstractCodeActionProvider {
    private static final String NO_IMPL_FOUND_FOR_FUNCTION = "no implementation found for the function";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                           List<Diagnostic> diagnostics) {
        List<CodeAction> actions = new ArrayList<>();
        List<DiagnosticPos> addedObjPosition = new ArrayList<>();
        WorkspaceDocumentManager documentManager = lsContext.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        try {
            BLangPackage bLangPackage = LSModuleCompiler.getBLangPackage(lsContext, documentManager,
                                                                         LSCustomErrorStrategy.class, false, false);
            for (Diagnostic diagnostic : diagnostics) {
                if (diagnostic.getMessage().startsWith(NO_IMPL_FOUND_FOR_FUNCTION)) {
                    CodeAction codeAction = getNoImplementationFoundCommand(diagnostic, addedObjPosition,
                                                                            bLangPackage, lsContext);
                    if (codeAction != null) {
                        actions.add(codeAction);
                    }
                }
            }
        } catch (CompilationFailedException e) {
            // ignore
        }
        return actions;
    }

    private static CodeAction getNoImplementationFoundCommand(Diagnostic diagnostic,
                                                              List<DiagnosticPos> addedObjPosition,
                                                              BLangPackage bLangPackage, LSContext context) {
        Position position = diagnostic.getRange().getStart();
        int line = position.getLine();
        int column = position.getCharacter();
        String uri = context.get(CodeActionKeys.FILE_URI_KEY);
        Optional<BLangTypeDefinition> objType = bLangPackage.topLevelNodes.stream()
                .filter(topLevelNode -> {
                    if (topLevelNode instanceof BLangTypeDefinition) {
                        org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition pos =
                                topLevelNode.getPosition();
                        return ((pos.getStartLine() == line || pos.getEndLine() == line ||
                                (pos.getStartLine() < line && pos.getEndLine() > line)) &&
                                (pos.getStartColumn() <= column && pos.getEndColumn() <= column));
                    }
                    return false;
                }).findAny().map(t -> (BLangTypeDefinition) t);

        List<TextEdit> edits = new ArrayList<>();
        if (objType.isPresent()) {
            BLangTypeDefinition typeDefinition = objType.get();
            if (addedObjPosition.contains(typeDefinition.pos)) {
                // Skip, CodeAction for typeDefinition is already added
                return null;
            }
            addedObjPosition.add(typeDefinition.pos);

            BSymbol symbol = typeDefinition.symbol;
            if (symbol instanceof BObjectTypeSymbol) {
                BObjectTypeSymbol typeSymbol = (BObjectTypeSymbol) symbol;
                typeSymbol.referencedFunctions.stream()
                        .filter(func -> !func.symbol.bodyExist)
                        .forEach(func -> edits
                                .addAll(getNewFunctionEditText(func, objType.get(), bLangPackage, context)));
            }
        }

        if (!edits.isEmpty()) {
            List<Diagnostic> diagnostics = new ArrayList<>();
            diagnostics.add(diagnostic);
            String commandTitle = CommandConstants.IMPLEMENT_FUNCS_TITLE;
            CodeAction action = new CodeAction(commandTitle);
            action.setKind(CodeActionKind.QuickFix);
            action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                    new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), edits)))));
            action.setDiagnostics(diagnostics);
            return action;
        }
        return null;
    }

    private static List<TextEdit> getNewFunctionEditText(BAttachedFunction function, BLangTypeDefinition object,
                                                         BLangPackage packageNode, LSContext context) {
        String returnType;
        String returnValue;
        String funcArgs = "";
        PackageID currentPkgId = packageNode.packageID;
        List<TextEdit> edits = new ArrayList<>();
        BiConsumer<String, String> importsAcceptor = (orgName, alias) -> {
            boolean notFound = packageNode.getImports().stream().noneMatch(
                    pkg -> (pkg.orgName.value.equals(orgName) && pkg.alias.value.equals(alias))
            );
            if (notFound) {
                String pkgName = orgName + "/" + alias;
                edits.add(createPackageImportTextEdit(pkgName, context));
            }
        };
        returnType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, function.type.retType);
        returnValue = FunctionGenerator.generateReturnValue(importsAcceptor, currentPkgId, function.type.retType,
                                                            "        return {%1};");
        List<String> arguments = getFuncArguments(importsAcceptor, currentPkgId, function.symbol);
        if (arguments != null) {
            funcArgs = String.join(", ", arguments);
        }
        boolean prependLineFeed = false;
        if (object.typeNode instanceof BLangObjectTypeNode) {
            BLangObjectTypeNode typeNode = (BLangObjectTypeNode) object.typeNode;
            prependLineFeed = typeNode.functions.size() > 0;
        }
        boolean isPublic = (function.type.tsymbol.flags & Flags.PUBLIC) == Flags.PUBLIC;
        String modifiers = (isPublic) ? "public " : "";
        String editText = FunctionGenerator.createFunction(function.funcName.value, funcArgs, returnType, returnValue,
                                                           modifiers, false, StringUtils.repeat(' ', 4));
        Position editPos = new Position(object.pos.eLine - 1, 0);
        edits.add(new TextEdit(new Range(editPos, editPos), editText));
        return edits;
    }

    /**
     * Get the function arguments from the function.
     *
     * @param importsAcceptor imports accepter
     * @param currentPkgId    current package ID
     * @param bLangInvocation {@link BInvokableSymbol}
     * @return {@link List} List of arguments
     */
    private static List<String> getFuncArguments(BiConsumer<String, String> importsAcceptor,
                                                 PackageID currentPkgId, BInvokableSymbol bLangInvocation) {
        List<String> list = new ArrayList<>();
        if (bLangInvocation.params.isEmpty()) {
            return null;
        }
        for (BVarSymbol bVarSymbol : bLangInvocation.params) {
            String argType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, bVarSymbol.type);
            String argName = bVarSymbol.name.value;
            list.add(argType + " " + argName);
        }
        BVarSymbol restParam = bLangInvocation.restParam;
        if (restParam != null && (restParam.type instanceof BArrayType)) {
            String argType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId,
                                                                      ((BArrayType) restParam.type).eType);
            list.add(argType + "... " + restParam.getName().getValue());
        }
        return (!list.isEmpty()) ? list : null;
    }

    private static TextEdit createPackageImportTextEdit(String pkgName, LSContext context) {
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
}
