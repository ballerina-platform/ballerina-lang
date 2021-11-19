/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import com.google.gson.JsonObject;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents the command executor for data mapper code action.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class AIDataMapperExecutor implements LSCommandExecutor {

    public static final String COMMAND = "GENERATE_MAPPING";

    /**
     * {@inheritDoc}
     *
     * @param context
     */
    @Override
    public Object execute(ExecuteCommandContext context) throws LSCommandExecutorException {
        String uri = null;
        Range range = null;
//        JsonArray parameters = null;
        JsonObject parameters = null;
        for (CommandArgument arg : context.getArguments()) {
            switch (arg.key()) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    uri = arg.valueAs(String.class);
                    break;
                case CommandConstants.ARG_KEY_NODE_RANGE:
                    range = arg.valueAs(Range.class);
                    break;
                case "ProcessedData":
                    parameters = arg.value();
                    break;
                default:
            }
        }
        LanguageClient client = context.getLanguageClient();
        Optional<Path> filePath = CommonUtil.getPathFromURI(uri);
        if (range == null || filePath.isEmpty() || parameters == null) {
            throw new UserErrorException("Invalid parameters received for the create mapping command!");
        }
        AIDataMapperExecutorUtil dataMapperExecutorUtil = new AIDataMapperExecutorUtil();
        try {
            List<TextEdit> fEdits = dataMapperExecutorUtil.generateMappingEdits(context, parameters,
                    filePath.get(), range);
            TextDocumentEdit textDocumentEdit = new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri,
                    null), fEdits);
            return CommandUtil.applyWorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)), client);
        } catch (IOException e) {
            // ignore
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
