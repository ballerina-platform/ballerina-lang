/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.api.types.TypeSymbol;
import io.ballerina.compiler.api.types.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR;

/**
 * Code Action for type guard variable assignment.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class TypeGuardCodeAction extends AbstractCodeActionProvider {
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    PositionDetails positionDetails, List<Diagnostic> allDiagnostics,
                                                    SyntaxTree syntaxTree, LSContext context) {
        String diagnosticMsg = diagnostic.getMessage().toLowerCase(Locale.ROOT);
        if (!(diagnosticMsg.contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED))) {
            return Collections.emptyList();
        }

        NonTerminalNode matchedNode = positionDetails.matchedNode();
        Symbol matchedSymbol = positionDetails.matchedSymbol();
        TypeSymbol typeDescriptor = positionDetails.matchedSymbolTypeDesc();
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        try {
            if (typeDescriptor.typeKind() == TypeDescKind.UNION) {
                UnionTypeSymbol unionType = (UnionTypeSymbol) typeDescriptor;
                boolean isRemoteInvocation = matchedSymbol instanceof Qualifiable &&
                        ((Qualifiable) matchedSymbol).qualifiers().contains(Qualifier.REMOTE);
                if (!isRemoteInvocation) {
                    // Add type guard code action
                    String commandTitle = String.format(CommandConstants.TYPE_GUARD_TITLE, matchedSymbol.name());
                    List<TextEdit> tEdits = getTypeGuardCodeActionEdits(context, uri, matchedNode,
                                                                        matchedSymbol, unionType);
                    if (!tEdits.isEmpty()) {
                        return Collections.singletonList(createQuickFixCodeAction(commandTitle, tEdits, uri));
                    }
                }
            }
        } catch (WorkspaceDocumentException | IOException e) {
            //ignore
        }
        return Collections.emptyList();
    }

    private static List<TextEdit> getTypeGuardCodeActionEdits(LSContext context, String uri,
                                                              NonTerminalNode matchedNode,
                                                              Symbol matchedSymbol,
                                                              UnionTypeSymbol unionType)
            throws WorkspaceDocumentException, IOException {
        LinePosition startLine = matchedNode.lineRange().startLine();
        LinePosition endLine = matchedNode.lineRange().endLine();
        int sLine = startLine.line();
        int sCol = startLine.offset();
        int eLine = endLine.line();
        int eCol = endLine.offset();
        Position startPos = new Position(sLine, sCol);
        Position endPosWithSemiColon = new Position(eLine, eCol + 1);
        Position endPos = new Position(eLine, eCol);
        Range newTextRange = new Range(startPos, endPosWithSemiColon);

        List<TextEdit> edits = new ArrayList<>();
        String spaces = StringUtils.repeat(' ', sCol);
        String padding = LINE_SEPARATOR + LINE_SEPARATOR + spaces;

        boolean hasError = unionType.memberTypeDescriptors().stream().anyMatch(s -> s.typeKind() == TypeDescKind.ERROR);

        List<TypeSymbol> members = new ArrayList<>((unionType).memberTypeDescriptors());
        long errorTypesCount = unionType.memberTypeDescriptors().stream()
                .filter(t -> t.typeKind() == TypeDescKind.ERROR)
                .count();
        if (members.size() == 1) {
            // Skip type guard
            return edits;
        }
        boolean transitiveBinaryUnion = unionType.memberTypeDescriptors().size() - errorTypesCount == 1;
        if (transitiveBinaryUnion) {
            members.removeIf(s -> s.typeKind() == TypeDescKind.ERROR);
        }
        // Check is binary union type with error type
        if ((unionType.memberTypeDescriptors().size() == 2 || transitiveBinaryUnion) && hasError) {
            members.forEach(bType -> {
                if (bType.typeKind() == TypeDescKind.NIL) {
                    // if (foo() is error) {...}
                    String newText = String.format("if (%s is error) {%s}", matchedNode.toSourceCode(), padding);
                    edits.add(new TextEdit(newTextRange, newText));
                } else {
                    // if (foo() is int) {...} else {...}
                    String type = bType.signature();
                    String newText = String.format("if (%s is %s) {%s} else {%s}", matchedNode.toSourceCode(), type,
                                                   padding, padding);
                    edits.add(new TextEdit(newTextRange, newText));
                }
            });
        } else {
            CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
            String name = matchedSymbol != null ? matchedSymbol.name() : unionType.signature();
            String varName = CommonUtil.generateVariableName(name, CommonUtil.getAllNameEntries(compilerContext));
            String typeDef = unionType.signature();
            boolean addErrorTypeAtEnd;

            List<TypeSymbol> tMembers = new ArrayList<>((unionType).memberTypeDescriptors());
            if (errorTypesCount > 1) {
                tMembers.removeIf(s -> s.typeKind() == TypeDescKind.ERROR);
                addErrorTypeAtEnd = true;
            } else {
                addErrorTypeAtEnd = false;
            }
            List<String> memberTypes = new ArrayList<>();
            IntStream.range(0, tMembers.size())
                    .forEachOrdered(value -> {
                        memberTypes.add(tMembers.get(value).signature());
                    });

            if (addErrorTypeAtEnd) {
                memberTypes.add("error");
            }

            String newText = String.format("%s %s = %s;%s", typeDef, varName, matchedNode.toSourceCode(),
                                           LINE_SEPARATOR);
            newText += spaces + IntStream.range(0, memberTypes.size() - 1)
                    .mapToObj(value -> {
                        return String.format("if (%s is %s) {%s}", varName, memberTypes.get(value), padding);
                    })
                    .collect(Collectors.joining(" else "));
            newText += String.format(" else {%s}", padding);
            edits.add(new TextEdit(newTextRange, newText));
        }
        return edits;
    }
}
