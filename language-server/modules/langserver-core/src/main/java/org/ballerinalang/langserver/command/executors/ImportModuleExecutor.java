/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.command.executors;

import com.google.gson.internal.LinkedTreeMap;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.command.LSCommandExecutor;
import org.ballerinalang.langserver.command.LSCommandExecutorException;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ballerinalang.langserver.command.CommandUtil.applySingleTextEdit;
import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;

/**
 * Command executor for importing a package.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.command.LSCommandExecutor")
public class ImportModuleExecutor implements LSCommandExecutor {

    private static final String COMMAND = "IMPORT_MODULE";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String documentUri = null;
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();

        for (Object arg : context.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            if (((LinkedTreeMap) arg).get(ARG_KEY).equals(CommandConstants.ARG_KEY_DOC_URI)) {
                documentUri = (String) ((LinkedTreeMap) arg).get(ARG_VALUE);
                textDocumentIdentifier.setUri(documentUri);
                context.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
            } else if (((LinkedTreeMap) arg).get(ARG_KEY).equals(CommandConstants.ARG_KEY_MODULE_NAME)) {
                context.put(ExecuteCommandKeys.PKG_NAME_KEY, (String) ((LinkedTreeMap) arg).get(ARG_VALUE));
            }
        }

        WorkspaceDocumentManager documentManager = context.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);
        if (documentUri != null && context.get(ExecuteCommandKeys.PKG_NAME_KEY) != null) {
            Path filePath = Paths.get(URI.create(documentUri));
            Path compilationPath = getUntitledFilePath(filePath.toString()).orElse(filePath);
            String fileContent;
            try {
                fileContent = documentManager.getFileContent(compilationPath);
            } catch (WorkspaceDocumentException e) {
                throw new LSCommandExecutorException("Error executing command Import module: " + e.getMessage());
            }
            String[] contentComponents = fileContent.split(CommonUtil.LINE_SEPARATOR_SPLIT);
            int totalLines = contentComponents.length;
            int lastNewLineCharIndex = Math.max(fileContent.lastIndexOf('\n'), fileContent.lastIndexOf('\r'));
            int lastCharCol = fileContent.substring(lastNewLineCharIndex + 1).length();
            LSCompiler lsCompiler = context.get(ExecuteCommandKeys.LS_COMPILER_KEY);
            BLangPackage bLangPackage = lsCompiler
                    .getBLangPackage(context, documentManager, false, LSCustomErrorStrategy.class, false)
                    .getRight();
            context.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY, bLangPackage.symbol.getName().getValue());
            String relativeSourcePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
            BLangPackage srcOwnerPkg = CommonUtil.getSourceOwnerBLangPackage(relativeSourcePath, bLangPackage);
            String pkgName = context.get(ExecuteCommandKeys.PKG_NAME_KEY);
            DiagnosticPos pos = null;

            // Filter the imports except the runtime import
            List<BLangImportPackage> imports = CommonUtil.getCurrentFileImports(srcOwnerPkg, context);

            if (!imports.isEmpty()) {
                BLangImportPackage lastImport = CommonUtil.getLastItem(imports);
                pos = lastImport.getPosition();
            }

            int endCol = pos == null ? -1 : pos.getEndColumn() - 1;
            int endLine = pos == null ? 0 : pos.getEndLine() - 1;

            String remainingTextToReplace;

            if (endCol != -1) {
                int contentLengthToReplaceStart = fileContent.substring(0,
                        fileContent.indexOf(contentComponents[endLine])).length() + endCol + 1;
                remainingTextToReplace = fileContent.substring(contentLengthToReplaceStart);
            } else {
                remainingTextToReplace = fileContent;
            }

            String editText = (pos != null ? CommonUtil.LINE_SEPARATOR : "") + "import " + pkgName + ";"
                    + (remainingTextToReplace.startsWith("\n") || remainingTextToReplace.startsWith("\r")
                    ? "" : CommonUtil.LINE_SEPARATOR) + remainingTextToReplace;
            Range range = new Range(new Position(endLine, endCol + 1), new Position(totalLines + 1, lastCharCol));

            return applySingleTextEdit(editText, range, textDocumentIdentifier,
                    context.get(ExecuteCommandKeys.LANGUAGE_SERVER_KEY).getClient());
        }

        return new Object();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
