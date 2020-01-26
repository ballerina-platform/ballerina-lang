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
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.util.ArrayList;
import java.util.List;

/**
 * Code Action provider for change abstract type.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.codeaction.BallerinaCodeActionProvider")
public class ChangeAbstractTypeObjExecutorCodeAction extends BallerinaCodeActionProvider {
    private static final String NO_IMPL_FOUND_FOR_FUNCTION = "no implementation found for the function";
    private static final String ABSTRACT_OBJECT = "in abstract object";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<org.eclipse.lsp4j.CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                             List<org.eclipse.lsp4j.Diagnostic> diagnostics) {
        List<CodeAction> actions = new ArrayList<>();

        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.getMessage().contains(ABSTRACT_OBJECT)) {
                CodeAction codeAction = CommandUtil.getChangeAbstractTypeCommand(diagnostic, lsContext);
                if (codeAction != null) {
                    actions.add(codeAction);
                }
            } else if (diagnostic.getMessage().startsWith(NO_IMPL_FOUND_FOR_FUNCTION)) {
                CodeAction codeAction = CommandUtil.getNoImplementationFoundCommand(diagnostic, lsContext);
                if (codeAction != null) {
                    actions.add(codeAction);
                }
            }
        }

        return actions;
    }
}
