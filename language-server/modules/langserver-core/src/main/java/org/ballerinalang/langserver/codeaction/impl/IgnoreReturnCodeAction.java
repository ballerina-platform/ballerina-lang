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

import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.api.types.UnionTypeDescriptor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider.createQuickFixCodeAction;

/**
 * Code Action for ignore variable assignment.
 *
 * @since 2.0.0
 */
public class IgnoreReturnCodeAction implements DiagBasedCodeAction {
    private final BallerinaTypeDescriptor typeDescriptor;

    public IgnoreReturnCodeAction(BallerinaTypeDescriptor typeDescriptor) {
        this.typeDescriptor = typeDescriptor;
    }

    @Override
    public List<CodeAction> get(Diagnostic diagnostic, List<Diagnostic> allDiagnostics, LSContext context) {
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        Position pos = diagnostic.getRange().getStart();
        boolean hasError = false;
        if (typeDescriptor.kind() == TypeDescKind.ERROR) {
            hasError = true;
        } else if (typeDescriptor.kind() == TypeDescKind.UNION) {
            UnionTypeDescriptor unionType = (UnionTypeDescriptor) typeDescriptor;
            hasError = unionType.memberTypeDescriptors().stream().anyMatch(s -> s.kind() == TypeDescKind.ERROR);
        }
        // Add ignore return value code action
        if (!hasError) {
            String commandTitle = CommandConstants.IGNORE_RETURN_TITLE;
            return Collections.singletonList(
                    createQuickFixCodeAction(commandTitle, getIgnoreCodeActionEdits(pos), uri));
        }
        return Collections.emptyList();
    }

    private static List<TextEdit> getIgnoreCodeActionEdits(Position position) {
        String editText = "_ = ";
        List<TextEdit> edits = new ArrayList<>();
        edits.add(new TextEdit(new Range(position, position), editText));
        return edits;
    }
}
