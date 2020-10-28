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

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.ClassDefinition;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Code Action for implementing functions of an object.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ImplementFunctionsCodeAction extends AbstractCodeActionProvider {
    private static final String NO_IMPL_FOUND_FOR_FUNCTION = "no implementation found for the function";

    @Override
    public boolean isEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    PositionDetails positionDetails, List<Diagnostic> allDiagnostics,
                                                    SyntaxTree syntaxTree, LSContext context) {
        if (!(diagnostic.getMessage().startsWith(NO_IMPL_FOUND_FOR_FUNCTION))) {
            new ArrayList<>();
        }
        BLangPackage bLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        Position position = diagnostic.getRange().getStart();
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        NonTerminalNode classType = positionDetails.matchedNode();

        List<TextEdit> edits = new ArrayList<>();
        if (classType.kind() == SyntaxKind.CLASS_DEFINITION) {
            ClassDefinition classDefinition = (ClassDefinition) classType;
            int one = 1;
//            if (symbol instanceof BObjectTypeSymbol) {
//                BObjectTypeSymbol typeSymbol = (BObjectTypeSymbol) symbol;
//                typeSymbol.referencedFunctions.stream()
//                        .filter(func -> !func.symbol.bodyExist)
//                        .forEach(func -> edits
//                                .addAll(getNewFunctionEditText(func, classType.get(), bLangPackage, context)));
//            }
        }

        if (!edits.isEmpty()) {
            String commandTitle = CommandConstants.IMPLEMENT_FUNCS_TITLE;
            return Collections.singletonList(createQuickFixCodeAction(commandTitle, edits, uri));
        }
        return null;
    }

    private static List<TextEdit> getNewFunctionEditText(BAttachedFunction function, BLangTypeDefinition object,
                                                         BLangPackage packageNode, LSContext context) {
        String funcArgs = "";
        PackageID currentPkgId = packageNode.packageID;
        List<TextEdit> edits = new ArrayList<>();
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);
        //TODO: Fix this
        String returnType = "";
        String returnValue = "";

//        String returnType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId,
//                                                                     function.type.retType, context);
//        String returnValue = FunctionGenerator.generateReturnValue(importsAcceptor, currentPkgId, function.type
//        .retType,
//                                                                   "        return {%1};", context);
        List<String> arguments = getFuncArguments(importsAcceptor, currentPkgId, function.symbol, context);
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
        Position editPos = new Position(object.pos.lineRange().endLine().line() - 1, 0);
        edits.addAll(importsAcceptor.getNewImportTextEdits());
        edits.add(new TextEdit(new Range(editPos, editPos), editText));
        return edits;
    }

    /**
     * Get the function arguments from the function.
     *
     * @param importsAcceptor imports accepter
     * @param currentPkgId    current package ID
     * @param bLangInvocation {@link BInvokableSymbol}
     * @param context         {@link LSContext}
     * @return {@link List} List of arguments
     */
    private static List<String> getFuncArguments(ImportsAcceptor importsAcceptor,
                                                 PackageID currentPkgId, BInvokableSymbol bLangInvocation,
                                                 LSContext context) {
        List<String> list = new ArrayList<>();
        if (bLangInvocation.params.isEmpty()) {
            return null;
        }
        for (BVarSymbol bVarSymbol : bLangInvocation.params) {
            //TODO: Fix this
//            String argType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId, bVarSymbol.type,
//                                                                      context);
            String argType = "";
            String argName = bVarSymbol.name.value;
            list.add(argType + " " + argName);
        }
        BVarSymbol restParam = bLangInvocation.restParam;
        if (restParam != null && (restParam.type instanceof BArrayType)) {
            //TODO: Fix this
//            String argType = FunctionGenerator.generateTypeDefinition(importsAcceptor, currentPkgId,
//                                                                      ((BArrayType) restParam.type).eType, context);
            String argType = "";
            list.add(argType + "... " + restParam.getName().getValue());
        }
        return (!list.isEmpty()) ? list : null;
    }
}
