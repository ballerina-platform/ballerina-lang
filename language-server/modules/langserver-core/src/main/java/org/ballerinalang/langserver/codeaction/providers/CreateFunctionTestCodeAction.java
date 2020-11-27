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
import org.ballerinalang.langserver.command.executors.CreateTestExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Code Action for generating test case for function.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CreateFunctionTestCodeAction extends AbstractCodeActionProvider {
    public CreateFunctionTestCodeAction() {
        super(Collections.singletonList(CodeActionNodeType.FUNCTION));
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
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context) {
        String docUri = context.fileUri();
        List<CodeAction> actions = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        args.add(new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, docUri));
        Position position = context.cursorPosition();
        args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, "" + position.getLine()));
        args.add(new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN, "" + position.getCharacter()));

        CodeAction action = new CodeAction(CommandConstants.CREATE_TEST_FUNC_TITLE);
        action.setCommand(new Command(CommandConstants.CREATE_TEST_FUNC_TITLE, CreateTestExecutor.COMMAND, args));
        actions.add(action);
        return actions;
    }
}
