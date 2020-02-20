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

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.executors.ChangeAbstractTypeObjExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Code Action provider for change abstract type.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ChangeAbstractTypeObjCodeAction extends AbstractCodeActionProvider {
    private static final String NO_IMPL_FOUND_FOR_FUNCTION = "no implementation found for the function";
    private static final String ABSTRACT_OBJECT = "in abstract object";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> diagnosticsOfRange,
                                                    List<Diagnostic> allDiagnostics) {
        List<CodeAction> actions = new ArrayList<>();

        for (Diagnostic diagnostic : diagnosticsOfRange) {
            if (diagnostic.getMessage().contains(ABSTRACT_OBJECT)) {
                CodeAction codeAction = getChangeAbstractTypeCommand(diagnostic, lsContext);
                if (codeAction != null) {
                    actions.add(codeAction);
                }
            }
        }

        // Remove overlapping diagnostics of NO_IMPL_FOUND_FOR_FUNCTION
        Map<Range, Diagnostic> rangeToDiagnostics = new HashMap<>();
        diagnosticsOfRange.stream()
                .filter(diagnostic -> (diagnostic.getMessage().startsWith(NO_IMPL_FOUND_FOR_FUNCTION)))
                .forEach(diagnostic -> rangeToDiagnostics.put(diagnostic.getRange(), diagnostic));

        rangeToDiagnostics.values().forEach(diagnostic -> {
            CodeAction codeAction = getNoImplementationFoundCommand(diagnostic, lsContext);
            if (codeAction != null) {
                actions.add(codeAction);
            }
        });

        return actions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> allDiagnostics) {
        throw new UnsupportedOperationException("Not supported");
    }

    private static CodeAction getChangeAbstractTypeCommand(Diagnostic diagnostic, LSContext context) {
        String diagnosticMessage = diagnostic.getMessage();
        Position position = diagnostic.getRange().getStart();
        int line = position.getLine();
        int column = position.getCharacter();
        String uri = context.get(CodeActionKeys.FILE_URI_KEY);
        CommandArgument lineArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, "" + line);
        CommandArgument colArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN, "" + column);
        CommandArgument uriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        Matcher matcher = CommandConstants.FUNC_IN_ABSTRACT_OBJ_PATTERN.matcher(diagnosticMessage);
        if (matcher.find() && matcher.groupCount() > 1) {
            String objectName = matcher.group(2);
            int colonIndex = objectName.lastIndexOf(":");
            String simpleObjName = (colonIndex > -1) ? objectName.substring(colonIndex + 1) : objectName;
            List<Object> args = Arrays.asList(lineArg, colArg, uriArg);
            String commandTitle = String.format(CommandConstants.MAKE_OBJ_NON_ABSTRACT_TITLE, simpleObjName);

            CodeAction action = new CodeAction(commandTitle);
            action.setKind(CodeActionKind.QuickFix);
            action.setCommand(new Command(commandTitle, ChangeAbstractTypeObjExecutor.COMMAND, args));
            action.setDiagnostics(diagnostics);
            return action;
        }
        return null;
    }

    private static CodeAction getNoImplementationFoundCommand(Diagnostic diagnostic, LSContext context) {
        String diagnosticMessage = diagnostic.getMessage();
        Position position = diagnostic.getRange().getStart();
        int line = position.getLine();
        int column = position.getCharacter();
        String uri = context.get(CodeActionKeys.FILE_URI_KEY);
        CommandArgument lineArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, "" + line);
        CommandArgument colArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN, "" + column);
        CommandArgument uriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        Matcher matcher = CommandConstants.NO_IMPL_FOUND_FOR_FUNCTION_PATTERN.matcher(diagnosticMessage);
        if (matcher.find() && matcher.groupCount() > 1) {
            String objectName = matcher.group(2);
            int colonIndex = objectName.lastIndexOf(":");
            String simpleObjName = (colonIndex > -1) ? objectName.substring(colonIndex + 1) : objectName;
            List<Object> args = Arrays.asList(lineArg, colArg, uriArg);
            String commandTitle = String.format(CommandConstants.MAKE_OBJ_ABSTRACT_TITLE, simpleObjName);

            CodeAction action = new CodeAction(commandTitle);
            action.setKind(CodeActionKind.QuickFix);
            action.setCommand(new Command(commandTitle, ChangeAbstractTypeObjExecutor.COMMAND, args));
            action.setDiagnostics(diagnostics);
            return action;
        }
        return null;
    }
}
