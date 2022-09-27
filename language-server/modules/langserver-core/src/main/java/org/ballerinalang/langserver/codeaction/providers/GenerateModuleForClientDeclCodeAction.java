/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.projects.PackageCompilation;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.command.executors.GenerateModuleForClientDeclExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action for generating modules for client declarations.
 *
 * @since 2201.3.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class GenerateModuleForClientDeclCodeAction implements DiagnosticBasedCodeActionProvider {

    private static final String NAME = "Generate Module Client Declaration";

    public static final String DIAGNOSTIC_CODE = "BCE4037";
    public static final String DIAGNOSTIC_CODE_EXCLUDE = "BCE5304";

    @Override
    public boolean validate(Diagnostic diagnostic,
                            DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return DIAGNOSTIC_CODE.equals(diagnostic.diagnosticInfo().code());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {

        Optional<PackageCompilation> packageCompilation = context.workspace()
                .waitAndGetPackageCompilation(context.filePath());
        //Ensure that the user's project is not stand-alone Ballerina file.
        if (packageCompilation.isEmpty() ||
                packageCompilation.get().diagnosticResult().diagnostics().stream()
                        .anyMatch(diag -> DIAGNOSTIC_CODE_EXCLUDE.equals(diag.diagnosticInfo().code()))) {
            return Collections.emptyList();
        }
        CommandArgument uriArg = CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, context.fileUri());
        List<Object> args = new ArrayList<>();
        args.add(uriArg);
        String commandTitle = CommandConstants.GENERATE_MODULE_FOR_CLIENT_DECLARATION_TITLE;
        Command command = new Command(commandTitle, GenerateModuleForClientDeclExecutor.COMMAND, args);
        CodeAction action = CodeActionUtil.createCodeAction(commandTitle, command, CodeActionKind.QuickFix);
        return Collections.singletonList(action);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
