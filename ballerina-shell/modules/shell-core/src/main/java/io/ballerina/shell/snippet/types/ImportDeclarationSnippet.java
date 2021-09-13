/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.snippet.types;

import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.shell.snippet.SnippetSubKind;
import io.ballerina.shell.utils.Identifier;
import io.ballerina.shell.utils.QuotedImport;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Snippet that represent a import statement.
 *
 * @since 2.0.0
 */
public class ImportDeclarationSnippet extends AbstractSnippet<ImportDeclarationNode> implements DeclarationSnippet {
    public ImportDeclarationSnippet(ImportDeclarationNode rootNode) {
        super(SnippetSubKind.IMPORT_DECLARATION, rootNode);
    }

    /**
     * Finds the import alias used for the import statement.
     * Eg: alias of 'import abc/pqr' is 'pqr'
     * This is prefix or the last module name.
     *
     * @return Alias of this import.
     */
    public Identifier getPrefix() {
        String importPrefix = rootNode.prefix().isPresent()
                ? rootNode.prefix().get().prefix().text()
                : rootNode.moduleName().get(rootNode.moduleName().size() - 1).text();
        return new Identifier(importPrefix);
    }

    /**
     * Finds the import expression or the imported module.
     * This will follow `orgName/module1.module2` format.
     *
     * @return Imported module expression.
     */
    public QuotedImport getImportedModule() {
        List<String> moduleNames = rootNode.moduleName().stream().map(IdentifierToken::text)
                .collect(Collectors.toList());
        if (rootNode.orgName().isPresent()) {
            String orgName = rootNode.orgName().get().orgName().text();
            return new QuotedImport(orgName, moduleNames);
        }
        return new QuotedImport(moduleNames);
    }
}
