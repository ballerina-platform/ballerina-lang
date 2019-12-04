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
import org.ballerinalang.langserver.codeaction.BallerinaCodeActionProvider;
import org.ballerinalang.langserver.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.util.ArrayList;
import java.util.List;

/**
 * Code Action provider for pulling a package from central.
 *
 * @since 1.0.4
 */
@JavaSPIService("org.ballerinalang.langserver.codeaction.BallerinaCodeActionProvider")
public class PullModeExecutorCodeAction implements BallerinaCodeActionProvider {
    private static final String UNRESOLVED_MODULE = "cannot resolve module";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                           List<Diagnostic> diagnostics) {

        List<CodeAction> actions = new ArrayList<>();

        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.getMessage().startsWith(UNRESOLVED_MODULE)) {
                actions.add(CommandUtil.getUnresolvedPackageCommand(diagnostic, lsContext));
            }
        }
        return actions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNodeBased() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeActionNodeType> getCodeActionNodeTypes() {
        return null;
    }
}
