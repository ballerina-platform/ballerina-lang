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

import com.google.gson.JsonObject;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;

import java.util.List;

import static org.ballerinalang.langserver.command.CommandUtil.applySingleTextEdit;

/**
 * Command executor for importing a package.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.command.LSCommandExecutor")
public class ImportModuleExecutor implements LSCommandExecutor {

    public static final String COMMAND = "IMPORT_MODULE";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String documentUri = null;
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();

        for (Object arg : context.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            if (((JsonObject) arg).get(ARG_KEY).getAsString().equals(CommandConstants.ARG_KEY_DOC_URI)) {
                documentUri = ((JsonObject) arg).get(ARG_VALUE).getAsString();
                textDocumentIdentifier.setUri(documentUri);
                context.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
            } else if (((JsonObject) arg).get(ARG_KEY).getAsString().equals(CommandConstants.ARG_KEY_MODULE_NAME)) {
                context.put(ExecuteCommandKeys.PKG_NAME_KEY, ((JsonObject) arg).get(ARG_VALUE).getAsString());
            }
        }

        WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        if (documentUri != null && context.get(ExecuteCommandKeys.PKG_NAME_KEY) != null) {
            try {
                LSModuleCompiler.getBLangPackage(context, documentManager, LSCustomErrorStrategy.class, false, false);
            } catch (CompilationFailedException e) {
                throw new LSCommandExecutorException("Couldn't compile the source", e);
            }
            String pkgName = context.get(ExecuteCommandKeys.PKG_NAME_KEY);

            // Filter the imports except the runtime import
            context.put(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY, CommonUtil.getCurrentFileImports(context));
            List<BLangImportPackage> imports = CommonUtil.getCurrentFileImports(context);

            Position start = new Position(0, 0);
            if (!imports.isEmpty()) {
                BLangImportPackage last = CommonUtil.getLastItem(imports);
                start = new Position(last.getPosition().getEndLine(), 0);
            }
            Range range = new Range(start, start);

            String importStatement = ItemResolverConstants.IMPORT + " " + pkgName
                    + CommonKeys.SEMI_COLON_SYMBOL_KEY + CommonUtil.LINE_SEPARATOR;

            return applySingleTextEdit(importStatement, range, textDocumentIdentifier,
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
