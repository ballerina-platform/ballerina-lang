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

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.command.executors.AddDocumentationExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.NodeBasedPositionDetails;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.langserver.command.docs.DocumentationGenerator.hasDocs;

/**
 * Code Action for adding single documentation.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddDocumentationCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Add Documentation";

    public AddDocumentationCodeAction() {
        super(Arrays.asList(CodeActionNodeType.FUNCTION,
                CodeActionNodeType.OBJECT,
                CodeActionNodeType.CLASS,
                CodeActionNodeType.SERVICE,
                CodeActionNodeType.RESOURCE,
                CodeActionNodeType.RECORD,
                CodeActionNodeType.OBJECT_FUNCTION,
                CodeActionNodeType.CLASS_FUNCTION));
    }

    @Override
    public int priority() {
        return 999;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context,
                                                    NodeBasedPositionDetails posDetails) {
        String docUri = context.fileUri();
        NonTerminalNode matchedNode = posDetails.matchedTopLevelNode();
        
        if (hasDocs(matchedNode)) {
            return Collections.emptyList();
        }

        CommandArgument docUriArg = CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, docUri);
        CommandArgument lineStart = CommandArgument.from(CommandConstants.ARG_KEY_NODE_RANGE,
                                                         CommonUtil.toRange(matchedNode.lineRange()));
        List<Object> args = new ArrayList<>(Arrays.asList(docUriArg, lineStart));

        CodeAction action = new CodeAction(CommandConstants.ADD_DOCUMENTATION_TITLE);
        Command command = new Command(CommandConstants.ADD_DOCUMENTATION_TITLE, AddDocumentationExecutor.COMMAND, args);
        action.setCommand(command);
        return Collections.singletonList(action);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
