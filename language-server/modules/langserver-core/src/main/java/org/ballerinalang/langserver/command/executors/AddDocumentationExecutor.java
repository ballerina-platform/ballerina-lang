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

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.docs.DocAttachmentInfo;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageClient;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import static org.ballerinalang.langserver.command.CommandUtil.applySingleTextEdit;
import static org.ballerinalang.langserver.command.docs.DocumentationGenerator.getDocumentationEditForNode;

/**
 * Command executor for adding single documentation.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class AddDocumentationExecutor implements LSCommandExecutor {

    public static final String COMMAND = "ADD_DOC";

    /**
     * {@inheritDoc}
     *
     * @param ctx
     */
    @Override
    public Object execute(ExecuteCommandContext ctx) throws LSCommandExecutorException {
        String documentUri = "";
        Range nodeRange = null;
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();
        for (CommandArgument arg : ctx.getArguments()) {
            String argKey = arg.key();
            switch (argKey) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    documentUri = arg.valueAs(String.class);
                    textDocumentIdentifier.setUri(documentUri);
                    break;
                case CommandConstants.ARG_KEY_NODE_RANGE:
                    nodeRange = arg.valueAs(Range.class);
                    break;
                default:
                    break;
            }
        }

        Optional<Path> filePath = CommonUtil.getPathFromURI(documentUri);
        if (filePath.isEmpty() || nodeRange == null) {
            return Collections.emptyList();
        }

        SyntaxTree syntaxTree = ctx.workspace().syntaxTree(filePath.get()).orElseThrow();
        NonTerminalNode node = CommonUtil.findNode(nodeRange, syntaxTree);
        if (node.kind() == SyntaxKind.MODULE_PART) {
            node = ((ModulePartNode) node).members().get(0);
        }
        Optional<DocAttachmentInfo> docAttachmentInfo = getDocumentationEditForNode(node, syntaxTree);
        if (docAttachmentInfo.isPresent()) {
            DocAttachmentInfo docs = docAttachmentInfo.get();
            Range range = new Range(docs.getDocStartPos(), docs.getDocStartPos());
            LanguageClient languageClient = ctx.getLanguageClient();
            return applySingleTextEdit(docs.getDocumentationString(), range, textDocumentIdentifier, languageClient);
        }

        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
