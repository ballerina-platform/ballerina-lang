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
package org.ballerinalang.langserver.codelenses.provider;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codelenses.CodeLensesProviderKeys;
import org.ballerinalang.langserver.codelenses.LSCodeLensesProvider;
import org.ballerinalang.langserver.codelenses.LSCodeLensesProviderException;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.compiler.LSContext;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Code lenses provider for code diagnostics.
 *
 * @since 0.990.3
 */
@JavaSPIService("org.ballerinalang.langserver.codelenses.LSCodeLensesProvider")
public class DiagnosticsBasedCodeLensesProvider implements LSCodeLensesProvider {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "pull.CodeLenses";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeLens> getLenses(LSContext context) throws LSCodeLensesProviderException {
        List<CodeLens> lenses = new ArrayList<>();
        BLangCompilationUnit cUnit = context.get(CodeLensesProviderKeys.COMPILATION_UNIT_KEY);
        String fileUri = context.get(CodeLensesProviderKeys.FILE_URI_KEY);
        List<org.ballerinalang.util.diagnostic.Diagnostic> diagnostics = context.get(
                CodeLensesProviderKeys.DIAGNOSTIC_KEY);
        String cUnitName = cUnit.pos.src.cUnitName;
        CommandUtil.CommandArgument docUriArg = new CommandUtil.CommandArgument(
                CommandConstants.ARG_KEY_DOC_URI, fileUri);
        for (org.ballerinalang.util.diagnostic.Diagnostic diagnostic : diagnostics) {
            if (!diagnostic.getPosition().getSource().getCompilationUnitName().equals(cUnitName)) {
                // Skip diagnostics for other files
                continue;
            }
            // Look for "cannot resolve module (*)" error
            Matcher matcher = CommandConstants.UNRESOLVED_MODULE_PATTERN.matcher(
                    diagnostic.getMessage().toLowerCase(Locale.ROOT)
            );
            if (matcher.find() && matcher.groupCount() > 0) {
                List<Object> args = new ArrayList<>();
                String pkgName = matcher.group(1);
                args.add(new CommandUtil.CommandArgument(CommandConstants.ARG_KEY_MODULE_NAME, pkgName));
                args.add(docUriArg);
                String commandTitle = CommandConstants.PULL_MOD_TITLE;

                int line = diagnostic.getPosition().getStartLine() - 1;
                int col = diagnostic.getPosition().getStartColumn();
                Position pos = new Position(line, col);
                Command command = new Command(commandTitle, CommandConstants.CMD_PULL_MODULE, args);
                CodeLens lens = new CodeLens(new Range(pos, pos), command, null);
                lenses.add(lens);
            }
        }
        return lenses;
    }
}
