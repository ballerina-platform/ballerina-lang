/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers.imports;

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.eclipse.lsp4j.CodeActionKind;

import java.util.List;

/**
 * Code Action to remove all unused imports, except when there is a re-declared import statement.
 *
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class RemoveAllUnusedImportsCodeAction extends OptimizeImportsCodeAction {

    public static final String NAME = "Remove all unused imports";

    public RemoveAllUnusedImportsCodeAction() {
        super();
    }

    @Override
    protected List<ImportDeclarationNode> organizeFileImports(List<ImportDeclarationNode> fileImports) {
        return fileImports;
    }

    @Override
    protected String getCodeActionTitle() {
        return CommandConstants.REMOVE_ALL_UNUSED_IMPORTS;
    }

    @Override
    protected String getCodeActionKind() {
        return CodeActionKind.QuickFix;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
