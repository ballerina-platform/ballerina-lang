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
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.config.LSClientConfigHolder;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


/**
 * Code Action provider for automatic data mapping.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AIDataMapperCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "AI Data Mapper";

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
                CodeAction action = new CodeAction("Generate mapping function");
                action.setKind(CodeActionKind.QuickFix);

                String uri = context.fileUri();
                AIDataMapperCodeActionUtil dataMapperUtil = AIDataMapperCodeActionUtil.getInstance();
                ArrayList outputArray = dataMapperUtil.getAIDataMapperCodeActionEdits(positionDetails, context,
                        diagnostic);
                if (outputArray.isEmpty()) {
                    return Optional.empty();
                }


                String commandTitle = "Generate mapping function";
                String diagnosticMessage = diagnostic.message();
                Range range = CommonUtil.toRange(diagnostic.location().lineRange());
                CommandArgument posArg = CommandArgument.from(CommandConstants.ARG_KEY_NODE_RANGE, range);
                CommandArgument uriArg = CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, uri);
                CommandArgument jsontest = CommandArgument.from("JsonVal", outputArray);
                List<Object> args = Arrays.asList(posArg, uriArg, jsontest);
                action.setCommand(new Command(commandTitle, AIDataMapperExecutor.COMMAND, args));


                action.setDiagnostics(new ArrayList<>());
                return Optional.of(action);
            }
        } catch (IOException e) {
//             ignore
        }
        return Optional.empty();
    }
}
