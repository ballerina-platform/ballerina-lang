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
package org.ballerinalang.langserver.codeaction.builder.impl;

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.langserver.codeaction.builder.DiagBasedCodeAction;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.LSCodeActionProviderException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel.Reference;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
    private SymbolReferencesModel.Reference refAtCursor;

    public TypeGuardCodeAction(SymbolReferencesModel.Reference refAtCursor) {
        this.refAtCursor = refAtCursor;
    }

    @Override
    public List<CodeAction> get(Diagnostic diagnostic, List<Diagnostic> allDiagnostics, LSContext context)
            throws LSCodeActionProviderException {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        BSymbol symbolAtCursor = refAtCursor.getSymbol();
        boolean isInvocation = symbolAtCursor instanceof BInvokableSymbol;
        boolean isRemoteInvocation =
                symbolAtCursor != null && (symbolAtCursor.flags & Flags.REMOTE) == Flags.REMOTE;

        boolean hasDefaultInitFunction = false;
        boolean hasCustomInitFunction = false;
        if (refAtCursor.getbLangNode() instanceof BLangInvocation) {
            hasDefaultInitFunction = symbolAtCursor instanceof BObjectTypeSymbol;
            hasCustomInitFunction = symbolAtCursor instanceof BInvokableSymbol &&
                    symbolAtCursor.name.value.endsWith("init");
        }
        boolean isInitInvocation = hasDefaultInitFunction || hasCustomInitFunction;

        String commandTitle;
        try {
            if (isInvocation || isInitInvocation) {
                BType returnType;
                if (hasDefaultInitFunction) {
                    returnType = symbolAtCursor.type;
                } else if (hasCustomInitFunction) {
                    returnType = symbolAtCursor.owner.type;
                } else {
                    returnType = ((BInvokableSymbol) symbolAtCursor).retType;
                }
                if (returnType instanceof BUnionType) {
                    BUnionType unionType = (BUnionType) returnType;
                    if (!isRemoteInvocation) {
                        // Add type guard code action
                        commandTitle = String.format(CommandConstants.TYPE_GUARD_TITLE, symbolAtCursor.name);
                        List<TextEdit> tEdits = getTypeGuardCodeActionEdits(context, uri, refAtCursor, unionType);
                        if (!tEdits.isEmpty()) {
                            return Collections.singletonList(createQuickFixCodeAction(commandTitle, tEdits, uri));
                        }
                    }
                }
            }
        } catch (IOException | WorkspaceDocumentException e) {
            throw new LSCodeActionProviderException("", e);
        }

        return new ArrayList<>();
    }

    private static List<TextEdit> getTypeGuardCodeActionEdits(LSContext context, String uri,
                                                              Reference referenceAtCursor,
                                                              BUnionType unionType)
            throws WorkspaceDocumentException, IOException {
        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        BLangNode bLangNode = referenceAtCursor.getbLangNode();
        Position startPos = new Position(bLangNode.pos.sLine - 1, bLangNode.pos.sCol - 1);
        Position endPosWithSemiColon = new Position(bLangNode.pos.eLine - 1, bLangNode.pos.eCol);
        Position endPos = new Position(bLangNode.pos.eLine - 1, bLangNode.pos.eCol - 1);
        Range newTextRange = new Range(startPos, endPosWithSemiColon);

        List<TextEdit> edits = new ArrayList<>();
        String spaces = StringUtils.repeat('\t', bLangNode.pos.sCol - 1);
        String padding = LINE_SEPARATOR + LINE_SEPARATOR + spaces;
        String content = CommandUtil.getContentOfRange(docManager, uri, new Range(startPos, endPos));
        // Remove last line feed
        while (content.endsWith(LINE_SEPARATOR)) {
            content = content.substring(0, content.length() - 1);
        }

        boolean hasError = unionType.getMemberTypes().stream().anyMatch(s -> s instanceof BErrorType);

        List<BType> members = new ArrayList<>((unionType).getMemberTypes());
        long errorTypesCount = unionType.getMemberTypes().stream().filter(t -> t instanceof BErrorType).count();
        if (members.size() == 1) {
            // Skip type guard
            return edits;
        }
        boolean transitiveBinaryUnion = unionType.getMemberTypes().size() - errorTypesCount == 1;
        if (transitiveBinaryUnion) {
            members.removeIf(s -> s instanceof BErrorType);
        }
        // Check is binary union type with error type
        if ((unionType.getMemberTypes().size() == 2 || transitiveBinaryUnion) && hasError) {
            String finalContent = content;
            members.forEach(bType -> {
                if (bType instanceof BNilType) {
                    // if (foo() is error) {...}
                    String newText = String.format("if (%s is error) {%s}", finalContent, padding);
                    edits.add(new TextEdit(newTextRange, newText));
                } else {
                    // if (foo() is int) {...} else {...}
                    String type = CommonUtil.getBTypeName(bType, context, true);
                    String newText = String.format("if (%s is %s) {%s} else {%s}", finalContent, type, padding,
                                                   padding);
                    edits.add(new TextEdit(newTextRange, newText));
                }
            });
        } else {
            CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
            Set<String> nameEntries = CommonUtil.getAllNameEntries(compilerContext);
            String varName = CommonUtil.generateVariableName(bLangNode, nameEntries);
            String typeDef = CommonUtil.getBTypeName(unionType, context, true);
            boolean addErrorTypeAtEnd;

            List<BType> tMembers = new ArrayList<>((unionType).getMemberTypes());
            if (errorTypesCount > 1) {
                tMembers.removeIf(s -> s instanceof BErrorType);
                addErrorTypeAtEnd = true;
            } else {
                addErrorTypeAtEnd = false;
            }
            List<String> memberTypes = new ArrayList<>();
            IntStream.range(0, tMembers.size())
                    .forEachOrdered(value -> {
                        BType bType = tMembers.get(value);
                        String bTypeName = CommonUtil.getBTypeName(bType, context, true);
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
