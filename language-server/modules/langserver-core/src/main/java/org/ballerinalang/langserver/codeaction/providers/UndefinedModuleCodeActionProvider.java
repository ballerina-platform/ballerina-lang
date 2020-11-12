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
import org.ballerinalang.langserver.codeaction.impl.DiagBasedCodeAction;
import org.ballerinalang.langserver.codeaction.impl.ImportModuleCodeAction;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.LSCodeActionProviderException;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Code Action provider for importing a package.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class UndefinedModuleCodeActionProvider extends AbstractCodeActionProvider {
    private static final String UNDEFINED_FUNCTION = "undefined function";
    private static final String UNDEFINED_MODULE = "undefined module";
    private static final String UNRESOLVED_MODULE = "cannot resolve module";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> diagnosticsOfRange,
                                                    List<Diagnostic> allDiagnostics) {
        List<CodeAction> actions = new ArrayList<>();
        DiagBasedCodeAction importModuleCodeAction = new ImportModuleCodeAction();

        // Avoid duplicate import commands
        HashMap<String, Diagnostic> missingImports = new HashMap<>();
        for (Diagnostic diagnostic : diagnosticsOfRange) {
            missingImports.put(diagnostic.getMessage(), diagnostic);
        }
        for (Diagnostic diagnostic : missingImports.values()) {
            if (!(diagnostic.getMessage().startsWith(UNDEFINED_MODULE))) {
                continue;
            }
            try {
                actions.addAll(importModuleCodeAction.get(diagnostic, allDiagnostics, lsContext));
            } catch (LSCodeActionProviderException e) {
                // ignore
            }
        }
        return actions;
    }
}
