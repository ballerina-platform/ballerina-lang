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

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.api.types.TypeSymbol;
import io.ballerina.compiler.api.types.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.UnionType;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Code Action for error type handle.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ErrorTypeCodeAction extends AbstractCodeActionProvider {

    public ErrorTypeCodeAction(List<CodeActionNodeType> nodeTypes) {
        super(nodeTypes);
    }

    @Override
    public List<CodeAction> getNodeBasedCodeActions(NonTerminalNode matchedNode, CodeActionNodeType matchedNodeType,
                                                    List<Diagnostic> allDiagnostics, SyntaxTree syntaxTree,
                                                    LSContext context) {
        return super.getNodeBasedCodeActions(matchedNode, matchedNodeType, allDiagnostics, syntaxTree, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    PositionDetails positionDetails,
                                                    List<Diagnostic> allDiagnostics, SyntaxTree syntaxTree,
                                                    LSContext context) {
        String diagnosticMsg = diagnostic.getMessage().toLowerCase(Locale.ROOT);
        if (!(diagnosticMsg.contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED))) {
            return Collections.emptyList();
        }

        Symbol matchedSymbol = positionDetails.matchedSymbol();
        TypeSymbol typeDescriptor = positionDetails.matchedSymbolTypeDesc();
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        Position pos = diagnostic.getRange().getStart();
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        List<TextEdit> importEdits = new ArrayList<>();

        if (typeDescriptor.typeKind() != TypeDescKind.UNION) {
            return Collections.emptyList();
        }

        boolean hasErrorMemberType = ((UnionType) typeDescriptor).getMemberTypes().stream()
                .anyMatch(member -> member.getKind() == TypeKind.ERROR);
        if (hasErrorMemberType) {
            String name = (matchedSymbol != null) ? matchedSymbol.name() : typeDescriptor.signature();
            String varName = CommonUtil.generateVariableName(name, CommonUtil.getAllNameEntries(compilerContext));

            String typeName = ((UnionType) typeDescriptor).getMemberTypes().stream()
                    .filter(member -> member.getKind() != TypeKind.ERROR)
                    .map(member -> ((TypeSymbol) member).signature())
                    .collect(Collectors.joining("|"));
            return addErrorTypeBasedCodeActions(uri, matchedSymbol, typeDescriptor, pos, importEdits, typeName,
                                                varName);
        }
        return Collections.emptyList();
    }

    private static List<CodeAction> addErrorTypeBasedCodeActions(String uri, Symbol matchedSymbol,
                                                                 TypeSymbol typeSymbol,
                                                                 Position pos, List<TextEdit> importEdits,
                                                                 String type,
                                                                 String varName) {
        List<CodeAction> actions = new ArrayList<>();
        // add code action for `check`
        if (matchedSymbol != null) {
            boolean hasError = false;
            if (typeSymbol.typeKind() == TypeDescKind.ERROR) {
                hasError = true;
            } else if (typeSymbol.typeKind() == TypeDescKind.UNION) {
                UnionTypeSymbol unionType = (UnionTypeSymbol) typeSymbol;
                hasError = unionType.memberTypeDescriptors().stream().anyMatch(s -> s.typeKind() == TypeDescKind.ERROR);
            }
            if (hasError) {
                String panicCmd = String.format(CommandConstants.CREATE_VARIABLE_TITLE + " with '%s'", "Check");
                String edit = type + " " + varName + " = check ";
                List<TextEdit> edits = new ArrayList<>();
                Position insertPos = new Position(pos.getLine(), pos.getCharacter());
                edits.add(new TextEdit(new Range(insertPos, insertPos), edit));
                edits.addAll(importEdits);
                actions.add(createQuickFixCodeAction(panicCmd, edits, uri));
            }
        }
        return actions;
    }
}
