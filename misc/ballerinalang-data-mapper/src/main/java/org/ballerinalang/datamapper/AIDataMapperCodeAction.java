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
package org.ballerinalang.datamapper;

import io.ballerina.compiler.api.symbols.TypeDescKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.datamapper.config.LSClientExtendedConfig;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.config.LSClientConfigHolder;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.ballerinalang.datamapper.AIDataMapperCodeActionUtil.getAIDataMapperCodeActionEdits;

/**
 * Code Action provider for automatic data mapping.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AIDataMapperCodeAction extends AbstractCodeActionProvider {
    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    CodeActionContext context) {
        List<CodeAction> actions = new ArrayList<>();
        if (diagnostic.getMessage().toLowerCase(Locale.ROOT).contains(CommandConstants.INCOMPATIBLE_TYPES)) {
            getAIDataMapperCommand(diagnostic, context).map(actions::add);
        }
        return actions;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return LSClientConfigHolder.getInstance().getConfigAs(LSClientExtendedConfig.class).getDataMapper().isEnabled();
    }

    /**
     * Return data mapping code action.
     *
     * @param diagnostic {@link Diagnostic}
     * @param context    {@link LSContext}
     * @return data mapper code action
     */
    private static Optional<CodeAction> getAIDataMapperCommand(Diagnostic diagnostic,
                                                               CodeActionContext context) {
        try {
            if (CommonUtil.getRawType(context.positionDetails().matchedExprType()).typeKind() == TypeDescKind.RECORD) {
                CodeAction action = new CodeAction("Generate mapping function");
                action.setKind(CodeActionKind.QuickFix);

                String uri = context.fileUri();
                List<TextEdit> fEdits = getAIDataMapperCodeActionEdits(context, diagnostic);
                action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                        new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), fEdits)))));
                action.setDiagnostics(new ArrayList<>());
                return Optional.of(action);
            }
        } catch (IOException e) {
//             ignore
        }
        return Optional.empty();
    }
}
