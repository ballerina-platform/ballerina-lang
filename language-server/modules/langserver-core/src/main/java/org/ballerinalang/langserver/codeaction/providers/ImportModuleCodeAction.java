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
import org.ballerinalang.langserver.command.executors.ImportModuleExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.common.LSDocumentIdentifierImpl;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Code Action provider for importing a package.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ImportModuleCodeAction extends AbstractCodeActionProvider {
    private static final String UNDEFINED_FUNCTION = "undefined function";
    private static final String UNDEFINED_MODULE = "undefined module";
    private static final String UNRESOLVED_MODULE = "cannot resolve module";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                           List<Diagnostic> diagnostics) {
        WorkspaceDocumentManager documentManager = lsContext.get(CodeActionKeys.DOCUMENT_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(lsContext.get(CodeActionKeys.FILE_URI_KEY));
        LSDocumentIdentifier document = null;
        try {
            document = documentManager.getLSDocument(filePath.get());
        } catch (WorkspaceDocumentException e) {
            // ignore
        }
        List<CodeAction> actions = new ArrayList<>();

        if (document == null) {
            return actions;
        }
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.getMessage().startsWith(UNDEFINED_MODULE)) {
                List<CodeAction> codeActions = getModuleImportCommand(diagnostic, lsContext);
                actions.addAll(codeActions);
            }
        }
        return actions;
    }

    private static List<CodeAction> getModuleImportCommand(Diagnostic diagnostic, LSContext context) {
        String diagnosticMessage = diagnostic.getMessage();
        List<CodeAction> actions = new ArrayList<>();
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        CommandArgument uriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        String packageAlias = diagnosticMessage.substring(diagnosticMessage.indexOf("'") + 1,
                                                          diagnosticMessage.lastIndexOf("'"));
        LSDocumentIdentifier sourceDocument = new LSDocumentIdentifierImpl(uri);
        String sourceRoot = LSCompilerUtil.getProjectRoot(sourceDocument.getPath());
        sourceDocument.setProjectRootRoot(sourceRoot);
        List<BallerinaPackage> packagesList = new ArrayList<>();
        Stream.of(LSPackageLoader.getSdkPackages(), LSPackageLoader.getHomeRepoPackages())
                .forEach(packagesList::addAll);
        packagesList.stream()
                .filter(pkgEntry -> {
                    String fullPkgName = pkgEntry.getFullPackageNameAlias();
                    return fullPkgName.endsWith("." + packageAlias) || fullPkgName.endsWith("/" + packageAlias);
                })
                .forEach(pkgEntry -> {
                    String commandTitle = CommandConstants.IMPORT_MODULE_TITLE + " "
                            + pkgEntry.getFullPackageNameAlias();
                    CommandArgument pkgArgument = new CommandArgument(CommandConstants.ARG_KEY_MODULE_NAME,
                                                                      pkgEntry.getFullPackageNameAlias());
                    CodeAction action = new CodeAction(commandTitle);
                    action.setKind(CodeActionKind.QuickFix);
                    action.setCommand(new Command(commandTitle, ImportModuleExecutor.COMMAND,
                                                  new ArrayList<>(Arrays.asList(pkgArgument, uriArg))));
                    action.setDiagnostics(diagnostics);
                    actions.add(action);
                });
        return actions;
    }
}
