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
package org.ballerinalang.langserver.codelenses.providers;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codelenses.EndpointFindVisitor;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codelenses.CodeLensesProviderKeys;
import org.ballerinalang.langserver.commons.codelenses.LSCodeLensesProviderException;
import org.ballerinalang.langserver.compiler.config.LSClientConfigHolder;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;
import java.util.List;

/**
 * Code lenses provider for adding code lenses for all services.
 *
 * @since 0.990.3
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codelenses.spi.LSCodeLensesProvider")
public class EndpointsCodeLensesProvider extends AbstractCodeLensesProvider {
    public EndpointsCodeLensesProvider() {
        super("endpoints.CodeLenses");
        LSClientConfigHolder.getInstance().register((oldConfig, newConfig) -> {
            isEnabled = newConfig.getCodeLens().getEndpoints().isEnabled();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeLens> getLenses(LSContext context) throws LSCodeLensesProviderException {
        List<CodeLens> lenses = new ArrayList<>();
        BLangCompilationUnit cUnit = context.get(CodeLensesProviderKeys.COMPILATION_UNIT_KEY);
        addEndpointLenses(lenses, cUnit, context);
        return lenses;
    }

    private void addEndpointLenses(List<CodeLens> lenses, BLangCompilationUnit cUnit, LSContext context) {
        EndpointFindVisitor symbolFindVisitor = new EndpointFindVisitor();
        BLangPackage bLangPackage = context.get(CodeLensesProviderKeys.BLANG_PACKAGE_KEY);
        if (bLangPackage != null) {
            symbolFindVisitor.visit(cUnit);
            List<BLangNode> endpoints = symbolFindVisitor.getEndpoints();
            for (BLangNode node : endpoints) {
                Command endpointCmd = new Command("Endpoint", null);
                Position pos = new Position(node.pos.sLine - 1, node.pos.sCol);
                CodeLens endpointLens = new CodeLens(new Range(pos, pos), endpointCmd, null);
                lenses.add(endpointLens);
            }
        }
    }
}
