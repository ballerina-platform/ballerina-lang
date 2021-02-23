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
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.datamapper.config.ClientExtendedConfigImpl;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.config.LSClientConfigHolder;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
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
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        List<CodeAction> actions = new ArrayList<>();
        if (diagnostic.message().toLowerCase(Locale.ROOT).contains(CommandConstants.INCOMPATIBLE_TYPES)) {
            getAIDataMapperCommand(diagnostic, positionDetails, context).map(actions::add);
        }
        return actions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled(LanguageServerContext serverContext) {
        return LSClientConfigHolder.getInstance(serverContext)
                .getConfigAs(ClientExtendedConfigImpl.class).getDataMapper().isEnabled();
    }

    /**
     * Return data mapping code action.
     *
     * @param diagnostic      {@link Diagnostic}
     * @param positionDetails {@link DiagBasedPositionDetails}
     * @param context         {@link CodeActionContext}
     * @return data mapper code action
     */
    private static Optional<CodeAction> getAIDataMapperCommand(Diagnostic diagnostic,
                                                               DiagBasedPositionDetails positionDetails,
                                                               CodeActionContext context) {
        try {
            TypeDescKind typeDescriptor = CommonUtil.getRawType(positionDetails.matchedExprType()).typeKind();

            if (typeDescriptor == TypeDescKind.RECORD || typeDescriptor == TypeDescKind.COMPILATION_ERROR) {
                CodeAction action = new CodeAction("Generate mapping function");
                action.setKind(CodeActionKind.QuickFix);

                String uri = context.fileUri();
                List<TextEdit> fEdits = getAIDataMapperCodeActionEdits(positionDetails, context, diagnostic);
                if (fEdits.isEmpty()) {
                    return Optional.empty();
                }
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
