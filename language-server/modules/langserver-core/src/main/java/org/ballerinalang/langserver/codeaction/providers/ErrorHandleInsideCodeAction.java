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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

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
public class ErrorHandleInsideCodeAction extends CreateVariableCodeAction {
    /**
     * {@inheritDoc}
     */
    @Override
    public int priority() {
        return 997;
    }

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    CodeActionContext context) {
        String diagnosticMsg = diagnostic.getMessage().toLowerCase(Locale.ROOT);
        if (!(diagnosticMsg.contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED))) {
            return Collections.emptyList();
        }

        Symbol matchedSymbol = context.positionDetails().matchedSymbol();
        TypeSymbol typeDescriptor = context.positionDetails().matchedExprType();
        String uri = context.fileUri();
        if (typeDescriptor == null || typeDescriptor.typeKind() != TypeDescKind.UNION) {
            return Collections.emptyList();
        }
        UnionTypeSymbol unionType = (UnionTypeSymbol) typeDescriptor;
        boolean isRemoteInvocation = matchedSymbol instanceof Qualifiable &&
                ((Qualifiable) matchedSymbol).qualifiers().contains(Qualifier.REMOTE);
        if (isRemoteInvocation) {
            return Collections.emptyList();
        }

        CreateVariableOut createVarTextEdits = getCreateVariableTextEdits(diagnostic, context);

        // Add type guard code action
        String commandTitle = String.format(CommandConstants.TYPE_GUARD_TITLE, matchedSymbol.name());
        List<TextEdit> edits = getTypeGuardCodeActionEdits(createVarTextEdits.name, diagnostic, unionType);
        if (edits.isEmpty()) {
            return Collections.emptyList();
        }

        edits.add(createVarTextEdits.edits.get(0));
        edits.addAll(createVarTextEdits.imports);
        return Collections.singletonList(createQuickFixCodeAction(commandTitle, edits, uri));
    }

    private static List<TextEdit> getTypeGuardCodeActionEdits(String varName, Diagnostic diagnostic,
                                                              UnionTypeSymbol unionType) {
        Position startPos = diagnostic.getRange().getEnd();

        Range newTextRange = new Range(startPos, startPos);

        List<TextEdit> edits = new ArrayList<>();
        String spaces = StringUtils.repeat(' ', diagnostic.getRange().getStart().getCharacter());
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
                    String newText = String.format("%s%sif (%s is error) {%s}%s", LINE_SEPARATOR, spaces,
                            varName,
                            padding, LINE_SEPARATOR);
                    edits.add(new TextEdit(newTextRange, newText));
                } else {
                    // if (foo() is int) {...} else {...}
                    String type = bType.signature();
                    String newText = String.format("%s%sif (%s is %s) {%s} else {%s}%s", LINE_SEPARATOR, spaces,
                            varName, type, padding, padding, LINE_SEPARATOR);
                    edits.add(new TextEdit(newTextRange, newText));
                }
            });
        } else if (hasError) {
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

            String newText = spaces + IntStream.range(0, memberTypes.size() - 1)
                    .mapToObj(value -> String.format("%sif (%s is %s) {%s}", LINE_SEPARATOR, varName,
                            memberTypes.get(value), padding))
                    .collect(Collectors.joining(" else "));
            newText += String.format(" else {%s}%s", padding, LINE_SEPARATOR);
            edits.add(new TextEdit(newTextRange, newText));
        }
        return edits;
    }
}
