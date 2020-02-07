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
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.eclipse.lsp4j.CodeAction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.langserver.command.CommandUtil.getDocGenerationCommand;

/**
 * Code Action provider for adding single documentation.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddDocumentationCodeAction extends AbstractCodeActionProvider {
    public AddDocumentationCodeAction() {
        super(Arrays.asList(CodeActionNodeType.FUNCTION,
                            CodeActionNodeType.OBJECT,
                            CodeActionNodeType.SERVICE,
                            CodeActionNodeType.RESOURCE,
                            CodeActionNodeType.RECORD,
                            CodeActionNodeType.OBJECT_FUNCTION));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                           List<org.eclipse.lsp4j.Diagnostic> diagnostics) {
        String docUri = lsContext.get(CodeActionKeys.FILE_URI_KEY);
        return Collections.singletonList(
                getDocGenerationCommand(nodeType.name(), docUri,
                                        lsContext.get(CodeActionKeys.POSITION_START_KEY).getLine()));
    }
}
