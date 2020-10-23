/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import org.ballerinalang.langserver.codeaction.impl.ImplementFunctionsCodeAction;
import org.ballerinalang.langserver.codeaction.impl.MakeNonAbstractObjectCodeAction;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.LSCodeActionProviderException;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Code Action provider for implementing functions of an object.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class NoImplFoundCodeActionProvider extends AbstractCodeActionProvider {
    private static final String NO_IMPL_FOUND_FOR_FUNCTION = "no implementation found for the function";

    @Override
    public boolean isEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> diagnosticsOfRange,
                                                    List<Diagnostic> allDiagnostics) {
        List<CodeAction> actions = new ArrayList<>();
        BLangPackage bLangPackage = lsContext.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        if (bLangPackage == null) {
            return actions;
        }
        DiagBasedCodeAction implementFunctionsCodeAction = new ImplementFunctionsCodeAction();
        for (Diagnostic diagnostic : diagnosticsOfRange) {
            if (!(diagnostic.getMessage().startsWith(NO_IMPL_FOUND_FOR_FUNCTION))) {
                continue;
            }
            try {
                actions.addAll(implementFunctionsCodeAction.get(diagnostic, allDiagnostics, lsContext));
            } catch (LSCodeActionProviderException e) {
                // ignore
            }
        }

        // Remove overlapping diagnostics of NO_IMPL_FOUND_FOR_FUNCTION
        Map<Range, Diagnostic> rangeToDiagnostics = new HashMap<>();
        diagnosticsOfRange.stream()
                .filter(diagnostic -> (diagnostic.getMessage().startsWith(NO_IMPL_FOUND_FOR_FUNCTION)))
                .forEach(diagnostic -> rangeToDiagnostics.put(diagnostic.getRange(), diagnostic));

        DiagBasedCodeAction makeNonAbstractObjectCodeAction = new MakeNonAbstractObjectCodeAction();
        rangeToDiagnostics.values().forEach(diagnostic -> {
            try {
                makeNonAbstractObjectCodeAction.get(diagnostic, allDiagnostics, lsContext);
            } catch (LSCodeActionProviderException e) {
                // ignore
            }
        });

        return actions;
    }
}
