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
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.datamapper.config.ClientExtendedConfigImpl;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.ballerinalang.langserver.config.LSClientConfigHolder;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.TextEdit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


/**
 * Code Action provider for automatic data mapping.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider")
public class AIDataMapperCodeAction implements DiagnosticBasedCodeActionProvider {

    public static final String NAME = "AI Data Mapper";
    public static final String GENERATE_MAPPING_FUNCTION = "Generate mapping function";

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        List<CodeAction> actions = new ArrayList<>();
        if (diagnostic.message().toLowerCase(Locale.ROOT).contains(CommandConstants.INCOMPATIBLE_TYPES)) {
            getAIDataMapperCommand(diagnostic, positionDetails, context).map(actions::add);
        }
        return actions;
    }

    @Override
    public String getName() {
        return NAME;
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
            Optional<TypeSymbol> typeSymbol = positionDetails.diagnosticProperty(
                    DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX);
            if (typeSymbol.isEmpty()) {
                return Optional.empty();
            }
            TypeDescKind typeDescriptor = CommonUtil.getRawType(typeSymbol.get()).typeKind();

            if (typeDescriptor == TypeDescKind.UNION || typeDescriptor == TypeDescKind.RECORD ||
                    typeDescriptor == TypeDescKind.COMPILATION_ERROR) {
                List<TextEdit> fEdits = AIDataMapperCodeActionUtil.getInstance()
                        .getAIDataMapperCodeActionEdits(positionDetails, context, diagnostic);
                if (fEdits.isEmpty()) {
                    return Optional.empty();
                }
                CodeAction action = CodeActionUtil.createCodeAction(GENERATE_MAPPING_FUNCTION, fEdits,
                        context.fileUri(), CodeActionKind.QuickFix);
                return Optional.of(action);
            }
        } catch (IOException e) {
//             ignore
        }
        return Optional.empty();
    }
}
