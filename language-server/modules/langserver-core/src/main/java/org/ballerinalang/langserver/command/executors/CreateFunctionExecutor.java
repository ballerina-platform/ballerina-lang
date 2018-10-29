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
import org.eclipse.lsp4j.services.LanguageClient;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.langserver.command.CommandUtil.applySingleTextEdit;
import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;

/**
 * Represents the command executor for creating a function.
 * 
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.command.LSCommandExecutor")
public class CreateFunctionExecutor implements LSCommandExecutor {

    private static final String COMMAND = "CREATE_FUNC";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String documentUri = null;
        String funcName = null;
        String returnType = null;
        String returnDefaultValue = null;
        String funcArgs = "";
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();

        for (Object arg : context.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            String argKey = ((LinkedTreeMap) arg).get(ARG_KEY).toString();
            String argVal = ((LinkedTreeMap) arg).get(ARG_VALUE).toString();
            switch (argKey) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    documentUri = argVal;
                    textDocumentIdentifier.setUri(documentUri);
                    context.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
                    break;
                case CommandConstants.ARG_KEY_FUNC_NAME:
                    funcName = argVal;
                    break;
                case CommandConstants.ARG_KEY_RETURN_TYPE:
                    returnType = argVal;
                    break;
                case CommandConstants.ARG_KEY_RETURN_DEFAULT_VAL:
                    returnDefaultValue = argVal;
                    break;
                case CommandConstants.ARG_KEY_FUNC_ARGS:
                    funcArgs = argVal;
                    break;
                default:
                    break;
            }
        }

        if (documentUri == null || funcName == null) {
            return new Object();
        }

        WorkspaceDocumentManager documentManager = context.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);

        Path filePath = Paths.get(URI.create(documentUri));
        Path compilationPath = getUntitledFilePath(filePath.toString()).orElse(filePath);
        String fileContent;
        try {
            fileContent = documentManager.getFileContent(compilationPath);
        } catch (WorkspaceDocumentException e) {
            throw new LSCommandExecutorException("Error executing create function Command: " + e.getMessage());
        }
        String[] contentComponents = fileContent.split("\\n|\\r\\n|\\r");
        int totalLines = contentComponents.length;
        int lastNewLineCharIndex = Math.max(fileContent.lastIndexOf('\n'), fileContent.lastIndexOf('\r'));
        int lastCharCol = fileContent.substring(lastNewLineCharIndex + 1).length();

        LSCompiler lsCompiler = context.get(ExecuteCommandKeys.LS_COMPILER_KEY);
        BLangPackage bLangPackage = lsCompiler.getBLangPackage(context, documentManager, false,
                LSCustomErrorStrategy.class, false).getRight();
        if (bLangPackage == null) {
            return new Object();
        }

        String editText = CommonUtil.FunctionGenerator.createFunction(funcName, funcArgs, returnType,
                returnDefaultValue);
        Range range = new Range(new Position(totalLines, lastCharCol + 1), new Position(totalLines + 3, lastCharCol));

        LanguageClient client = context.get(ExecuteCommandKeys.LANGUAGE_SERVER_KEY).getClient();
        return applySingleTextEdit(editText, range, textDocumentIdentifier, client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
