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
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.util.Arrays;
import java.util.List;

/**
 * Code Action provider for create variable command.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.codeaction.BallerinaCodeActionProvider")
public class CreateTestExecutorCodeAction extends BallerinaCodeActionProvider {
    public CreateTestExecutorCodeAction() {
        super(Arrays.asList(CodeActionNodeType.FUNCTION,
                            CodeActionNodeType.OBJECT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                           List<Diagnostic> diagnostics) {
        try {
            return CommandUtil.getTestGenerationCommand(nodeType.name(),
                                                        lsContext.get(DocumentServiceKeys.FILE_URI_KEY),
                                                        lsContext);
        } catch (CompilationFailedException e) {
            // ignore
        }
        return null;
    }
}
