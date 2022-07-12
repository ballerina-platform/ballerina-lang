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
package org.ballerinalang.langserver.codeaction.providers.docs;

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.command.executors.AddAllDocumentationExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.NodeBasedPositionDetails;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Code Action provider for adding all documentation for top level items.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddAllDocumentationCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Add All Documentation";

    public AddAllDocumentationCodeAction() {
        super(Arrays.asList(CodeActionNodeType.FUNCTION,
                CodeActionNodeType.OBJECT,
                CodeActionNodeType.CLASS,
                CodeActionNodeType.SERVICE,
                CodeActionNodeType.RESOURCE,
                CodeActionNodeType.RECORD,
                CodeActionNodeType.OBJECT_FUNCTION,
                CodeActionNodeType.ANNOTATION,
                CodeActionNodeType.CLASS_FUNCTION));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context,
                                                    NodeBasedPositionDetails posDetails) {
        // We don't show 'Document All' for nodes other than top level nodes
        if (posDetails.matchedDocumentableNode().isEmpty()
                || posDetails.matchedDocumentableNode().get().parent().kind() != SyntaxKind.MODULE_PART) {
            return Collections.emptyList();
        }

        String docUri = context.fileUri();
        CommandArgument docUriArg = CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, docUri);
        List<Object> args = new ArrayList<>(Collections.singletonList(docUriArg));

        CodeAction action = new CodeAction(CommandConstants.ADD_ALL_DOC_TITLE);
        action.setCommand(new Command(CommandConstants.ADD_ALL_DOC_TITLE, AddAllDocumentationExecutor.COMMAND, args));
        action.setKind(CodeActionKind.Source);
        return Collections.singletonList(action);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
