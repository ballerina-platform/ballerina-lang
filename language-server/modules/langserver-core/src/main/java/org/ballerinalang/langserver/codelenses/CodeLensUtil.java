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
package org.ballerinalang.langserver.codelenses;

import org.ballerinalang.langserver.command.CommandUtil.CommandArgument;
import org.ballerinalang.langserver.command.executors.MessageExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.compiler.CollectDiagnosticListener;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR_SPLIT;

/**
 * Provides code lenses related common functionalities.
 *
 * @since 0.984.0
 */
public class CodeLensUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeLensUtil.class);

    /**
     * Compile and get code lenses.
     *
     * @param fileUri         file uri
     * @param documentManager Document Manager
     * @return a list of code lenses
     * @throws LSCompilerException thrown when compilation error occurs
     */
    public static List<CodeLens> compileAndGetCodeLenses(String fileUri, WorkspaceDocumentManager documentManager) 
            throws LSCompilerException {
        List<CodeLens> lenses = new ArrayList<>();
        LSServiceOperationContext codeLensContext = new LSServiceOperationContext();
        codeLensContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
        BLangPackage bLangPackage = LSModuleCompiler.getBLangPackage(codeLensContext, documentManager, true,
                                                               LSCustomErrorStrategy.class, false);
        // Source compilation has no errors, continue
        Optional<BLangCompilationUnit> documentCUnit = bLangPackage.getCompilationUnits().stream()
                .filter(cUnit -> (fileUri.endsWith(cUnit.getName())))
                .findFirst();

        CompilerContext compilerContext = codeLensContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        final List<org.ballerinalang.util.diagnostic.Diagnostic> diagnostics = new ArrayList<>();
        if (compilerContext.get(DiagnosticListener.class) instanceof CollectDiagnosticListener) {
            CollectDiagnosticListener listener =
                    (CollectDiagnosticListener) compilerContext.get(DiagnosticListener.class);
            diagnostics.addAll(listener.getDiagnostics());
            listener.clearAll();
        }

        codeLensContext.put(CodeLensesProviderKeys.BLANG_PACKAGE_KEY, bLangPackage);
        codeLensContext.put(CodeLensesProviderKeys.FILE_URI_KEY, fileUri);
        codeLensContext.put(CodeLensesProviderKeys.DIAGNOSTIC_KEY, diagnostics);

        documentCUnit.ifPresent(cUnit -> {
            codeLensContext.put(CodeLensesProviderKeys.COMPILATION_UNIT_KEY, cUnit);

            List<LSCodeLensesProvider> providers = LSCodeLensesProviderFactory.getInstance().getProviders();
            for (LSCodeLensesProvider provider : providers) {
                try {
                    lenses.addAll(provider.getLenses(codeLensContext));
                } catch (LSCodeLensesProviderException e) {
                    LOGGER.error("Error while retrieving lenses from: " + provider.getName());
                }
            }
        });
        return lenses;
    }

    /**
     * Updates cached code lenses.
     *
     * @param lenses           list of cached code lenses
     * @param contentChangeEvents a list of content change events
     * @throws WorkspaceDocumentException when updating code lenses fails
     */
    public static void updateCachedCodeLenses(List<CodeLens> lenses,
                                              List<TextDocumentContentChangeEvent> contentChangeEvents)
            throws WorkspaceDocumentException {
        int cLine = contentChangeEvents.get(contentChangeEvents.size() - 1).getRange().getEnd().getLine();
        for (TextDocumentContentChangeEvent changeEvent : contentChangeEvents) {
            Range changesRange = changeEvent.getRange();
            int newTextLines = (" " + changeEvent.getText() + " ").split(LINE_SEPARATOR_SPLIT).length;
            int affectedLines = (changesRange.getEnd().getLine() - changesRange.getStart().getLine());
            int textGap = (newTextLines - 1) - affectedLines;

            // Remove affected code lenses
            for (int i = 0; i < lenses.size(); i++) {
                CodeLens codeLens = lenses.get(i);
                Range lensRange = codeLens.getRange();
                boolean isWithingModRange = changesRange.getStart().getLine() <= lensRange.getStart().getLine() &&
                        changesRange.getEnd().getLine() >= lensRange.getEnd().getLine();
                if (isWithingModRange) {
                    lenses.remove(codeLens);
                    i--;
                } else {
                    // Padding position-below lenses
                    int sLine = codeLens.getRange().getStart().getLine();
                    if (textGap != 0 && sLine > cLine) {
                        codeLens.getRange().getStart().setLine(sLine + textGap);
                        codeLens.getRange().getEnd().setLine(sLine + textGap);
                    }
                }

                // Override the command of the code lens
                CommandArgument typeArg = new CommandArgument(CommandConstants.ARG_KEY_MESSAGE_TYPE,
                                                              String.valueOf(MessageType.Error.getValue()));
                CommandArgument msgArg = new CommandArgument(CommandConstants.ARG_KEY_MESSAGE,
                                                             "Please fix compilation errors first!");
                codeLens.setCommand(new Command(codeLens.getCommand().getTitle(), MessageExecutor.COMMAND,
                                                new ArrayList<>(Arrays.asList(typeArg, msgArg))));
            }
        }
    }
}
