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
package org.ballerinalang.langserver.codelenses.provider;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codelenses.CodeLensesProviderKeys;
import org.ballerinalang.langserver.codelenses.LSCodeLensesProvider;
import org.ballerinalang.langserver.codelenses.LSCodeLensesProviderException;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;

/**
 * Code lenses provider for adding code lenses for all imports.
 *
 * @since 0.990.3
 */
@JavaSPIService("org.ballerinalang.langserver.codelenses.LSCodeLensesProvider")
public class ImportBasedCodeLensesProvider implements LSCodeLensesProvider {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "imports.CodeLenses";
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
    public List<CodeLens> getLenses(LSContext context) throws LSCodeLensesProviderException {
        List<CodeLens> lenses = new ArrayList<>();
        BLangCompilationUnit cUnit = context.get(CodeLensesProviderKeys.COMPILATION_UNIT_KEY);
        for (TopLevelNode topLevelNode : cUnit.getTopLevelNodes()) {
            addImportLenses(lenses, topLevelNode);
        }
        return lenses;
    }

    private void addImportLenses(List<CodeLens> lenses, TopLevelNode topLevelNode) {
        if (topLevelNode instanceof BLangImportPackage
                && ((BLangImportPackage) topLevelNode).symbol != null
                && !Names.BUILTIN_ORG.equals(((BLangImportPackage) topLevelNode).symbol.pkgID.orgName)) {
            BLangImportPackage importPackage = (BLangImportPackage) topLevelNode;
            String importVersion = importPackage.symbol.pkgID.version.value;
            int line = importPackage.getPosition().getStartLine() - 1;
            int col = importPackage.getPosition().getStartColumn();
            Position pos = new Position(line, col);
            Command command = new Command("Current Version " + importVersion, null);
            CodeLens lens = new CodeLens(new Range(pos, pos), command, null);
            lenses.add(lens);
        }
    }
}
