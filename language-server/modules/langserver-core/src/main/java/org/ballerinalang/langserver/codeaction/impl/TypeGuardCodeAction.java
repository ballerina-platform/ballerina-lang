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
package org.ballerinalang.langserver.codeaction.impl;

import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.api.types.UnionTypeDescriptor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.LSCodeActionProviderException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider.createQuickFixCodeAction;
import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR;

/**
 * Code Action for type guard variable assignment.
 *
 * @since 2.0.0
 */
public class TypeGuardCodeAction implements DiagBasedCodeAction {
    private final NonTerminalNode scopedNode;
    private final Symbol scopedSymbol;
    private final BallerinaTypeDescriptor typeDescriptor;

    public TypeGuardCodeAction(BallerinaTypeDescriptor typeDescriptor, NonTerminalNode scopedNode,
                               Symbol scopedSymbol) {
        this.typeDescriptor = typeDescriptor;
        this.scopedNode = scopedNode;
        this.scopedSymbol = scopedSymbol;
    }

    @Override
    public List<CodeAction> get(Diagnostic diagnostic, List<Diagnostic> allDiagnostics, LSContext context)
            throws LSCodeActionProviderException {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        try {
            if (typeDescriptor.kind() == TypeDescKind.UNION) {
                UnionTypeDescriptor unionType = (UnionTypeDescriptor) typeDescriptor;
                boolean isRemoteInvocation = scopedSymbol instanceof Qualifiable &&
                        ((Qualifiable) scopedSymbol).qualifiers().contains(Qualifier.REMOTE);
                if (!isRemoteInvocation) {
                    // Add type guard code action
                    String commandTitle = String.format(CommandConstants.TYPE_GUARD_TITLE, scopedSymbol.name());
                    List<TextEdit> tEdits = getTypeGuardCodeActionEdits(context, uri, scopedNode,
                                                                        scopedSymbol, unionType);
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
                                                              NonTerminalNode scopedNode,
                                                              Symbol scopedSymbol,
                                                              UnionTypeDescriptor unionType)
            throws WorkspaceDocumentException, IOException {
        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        int sLine = scopedNode.lineRange().startLine().line();
        int sCol = scopedNode.lineRange().startLine().offset();
        int eLine = scopedNode.lineRange().endLine().line();
        int eCol = scopedNode.lineRange().endLine().offset();
        Position startPos = new Position(sLine, sCol);
        Position endPosWithSemiColon = new Position(eLine, eCol + 1);
        Position endPos = new Position(eLine, eCol);
        Range newTextRange = new Range(startPos, endPosWithSemiColon);

        List<TextEdit> edits = new ArrayList<>();
        String spaces = StringUtils.repeat(' ', sCol);
        String padding = LINE_SEPARATOR + LINE_SEPARATOR + spaces;
        String content = CommandUtil.getContentOfRange(docManager, uri, new Range(startPos, endPos));
        // Remove last line feed
        while (content.endsWith(LINE_SEPARATOR)) {
            content = content.substring(0, content.length() - 1);
        }

        boolean hasError = unionType.memberTypeDescriptors().stream().anyMatch(s -> s.kind() == TypeDescKind.ERROR);

        List<BallerinaTypeDescriptor> members = new ArrayList<>((unionType).memberTypeDescriptors());
        long errorTypesCount = unionType.memberTypeDescriptors().stream().filter(t -> t.kind() == TypeDescKind.ERROR)
                .count();
        if (members.size() == 1) {
            // Skip type guard
            return edits;
        }
        boolean transitiveBinaryUnion = unionType.memberTypeDescriptors().size() - errorTypesCount == 1;
        if (transitiveBinaryUnion) {
            members.removeIf(s -> s.kind() == TypeDescKind.ERROR);
        }
        // Check is binary union type with error type
        if ((unionType.memberTypeDescriptors().size() == 2 || transitiveBinaryUnion) && hasError) {
            String finalContent = content;
            members.forEach(bType -> {
                if (bType.kind() == TypeDescKind.NIL) {
                    // if (foo() is error) {...}
                    String newText = String.format("if (%s is error) {%s}", finalContent, padding);
                    edits.add(new TextEdit(newTextRange, newText));
                } else {
                    // if (foo() is int) {...} else {...}
                    String type = bType.signature();
                    String newText = String.format("if (%s is %s) {%s} else {%s}", finalContent, type, padding,
                                                   padding);
                    edits.add(new TextEdit(newTextRange, newText));
                }
            });
        } else {
            CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
            String name = scopedSymbol != null ? scopedSymbol.name() : unionType.signature();
            String varName = CommonUtil.generateVariableName(name, CommonUtil.getAllNameEntries(compilerContext));
            String typeDef = unionType.signature();
            boolean addErrorTypeAtEnd;

            List<BallerinaTypeDescriptor> tMembers = new ArrayList<>((unionType).memberTypeDescriptors());
            if (errorTypesCount > 1) {
                tMembers.removeIf(s -> s.kind() == TypeDescKind.ERROR);
                addErrorTypeAtEnd = true;
            } else {
                addErrorTypeAtEnd = false;
            }
            List<String> memberTypes = new ArrayList<>();
            IntStream.range(0, tMembers.size())
                    .forEachOrdered(value -> {
                        BallerinaTypeDescriptor bType = tMembers.get(value);
                        String bTypeName = bType.signature();
                        boolean isErrorType = bType instanceof BErrorType;
                        if (isErrorType && !addErrorTypeAtEnd) {
                            memberTypes.add(bTypeName);
                        } else if (!isErrorType) {
                            memberTypes.add(bTypeName);
                        }
                    });

            if (addErrorTypeAtEnd) {
                memberTypes.add("error");
            }

            String newText = String.format("%s %s = %s;%s", typeDef, varName, content, LINE_SEPARATOR);
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
