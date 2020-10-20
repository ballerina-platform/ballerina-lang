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
package org.ballerinalang.langserver.codeaction.impl;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.api.types.UnionTypeDescriptor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.langserver.codeaction.impl.DiagBasedCodeAction.getPossibleTypes;
import static org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider.createQuickFixCodeAction;

/**
 * Code Action for error type handle.
 *
 * @since 2.0.0
 */
public class ErrorTypeCodeAction implements DiagBasedCodeAction {
    private final Symbol scopedSymbol;
    private final BallerinaTypeDescriptor typeDescriptor;

    public ErrorTypeCodeAction(BallerinaTypeDescriptor typeDescriptor, Symbol scopedSymbol) {
        this.typeDescriptor = typeDescriptor;
        this.scopedSymbol = scopedSymbol;
    }

    @Override
    public List<CodeAction> get(Diagnostic diagnostic, List<Diagnostic> allDiagnostics, LSContext context) {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        Position pos = diagnostic.getRange().getStart();
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        List<TextEdit> importEdits = new ArrayList<>();
        List<String> types = getPossibleTypes(context, typeDescriptor, importEdits, compilerContext);

        for (String type : types) {
            if (type.endsWith("|error")) {
                String name = (this.scopedSymbol != null) ? this.scopedSymbol.name() : this.typeDescriptor.signature();
                String varName = CommonUtil.generateVariableName(name, CommonUtil.getAllNameEntries(compilerContext));
                return addErrorTypeBasedCodeActions(uri, scopedSymbol, typeDescriptor, pos, importEdits, type, varName);
            }
        }
        return Collections.emptyList();
    }


    private static List<CodeAction> addErrorTypeBasedCodeActions(String uri, Symbol symbol,
                                                                 BallerinaTypeDescriptor typeDescriptor,
                                                                 Position position, List<TextEdit> importEdits,
                                                                 String type,
                                                                 String name) {
        List<CodeAction> actions = new ArrayList<>();
        // add code action for `check`
        if (symbol != null) {
            boolean hasError = false;
            if (typeDescriptor.kind() == TypeDescKind.ERROR) {
                hasError = true;
            } else if (typeDescriptor.kind() == TypeDescKind.UNION) {
                UnionTypeDescriptor unionType = (UnionTypeDescriptor) typeDescriptor;
                hasError = unionType.memberTypeDescriptors().stream().anyMatch(s -> s.kind() == TypeDescKind.ERROR);
            }
            if (hasError) {
                String panicCmd = String.format(CommandConstants.CREATE_VARIABLE_TITLE + " with '%s'", "Check");
                String edit = type.replace("|error", "") + " " + name + " = check ";
                List<TextEdit> edits = new ArrayList<>();
                Position insertPos = new Position(position.getLine(), position.getCharacter());
                edits.add(new TextEdit(new Range(insertPos, insertPos), edit));
                edits.addAll(importEdits);
                actions.add(createQuickFixCodeAction(panicCmd, edits, uri));
            }
        }
        // add code action for `checkpanic`
        String panicCmd = String.format(CommandConstants.CREATE_VARIABLE_TITLE + " with '%s'", "CheckPanic");
        String edit = type.replace("|error", "") + " " + name + " = checkpanic ";
        List<TextEdit> edits = new ArrayList<>();
        Position insertPos = new Position(position.getLine(), position.getCharacter());
        edits.add(new TextEdit(new Range(insertPos, insertPos), edit));
        edits.addAll(importEdits);
        actions.add(createQuickFixCodeAction(panicCmd, edits, uri));
        return actions;
    }

}
