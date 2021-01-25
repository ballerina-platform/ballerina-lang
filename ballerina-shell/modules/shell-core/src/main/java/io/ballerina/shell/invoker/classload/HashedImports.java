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

package io.ballerina.shell.invoker.classload;

import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.shell.snippet.types.ImportDeclarationSnippet;
import io.ballerina.shell.utils.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Imports that were stored to be able to search with the prefix.
 * The prefixes used will always be quoted identifiers.
 *
 * @since 2.0.0
 */
public class HashedImports {

    private static final ImportDeclarationSnippet JAVA_IMPORT = new ImportDeclarationSnippet(
            NodeFactory.createImportDeclarationNode(
                    NodeFactory.createToken(SyntaxKind.IMPORT_KEYWORD),
                    NodeFactory.createImportOrgNameNode(NodeFactory.createIdentifierToken("ballerina"),
                            NodeFactory.createToken(SyntaxKind.SLASH_TOKEN)),
                    NodeFactory.createSeparatedNodeList(
                            NodeFactory.createIdentifierToken("jballerina"),
                            NodeFactory.createToken(SyntaxKind.DOT_TOKEN),
                            NodeFactory.createIdentifierToken("java")
                    ),
                    null, NodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN)));

    /**
     * This is a map of import prefix to the import statement used.
     * Import prefix must be a quoted identifier.
     */
    private final HashMap<String, String> imports;
    /**
     * Reverse map to search the imported module.
     */
    private final HashMap<String, String> reverseImports;
    /**
     * Imports that should be done regardless of usage in the current snippet.
     * These are possibly the imports that are done previously
     * in module level declarations or variable declarations.
     * All prefixes are quoted identifiers.
     */
    private final Set<String> implicitImportPrefixes;

    public HashedImports() {
        this.imports = new HashMap<>();
        this.reverseImports = new HashMap<>();
        this.implicitImportPrefixes = new HashSet<>();
        storeImport(JAVA_IMPORT);
        storeImplicitPrefix(JAVA_IMPORT.getPrefix());
    }

    /**
     * Clear the memory of previous imports and reset
     * to original state.
     */
    public void reset() {
        this.imports.clear();
        this.reverseImports.clear();
        this.implicitImportPrefixes.clear();
        storeImport(JAVA_IMPORT);
        storeImplicitPrefix(JAVA_IMPORT.getPrefix());
    }

    /**
     * Get the import statement of the given prefix.
     *
     * @param prefix Prefix to search.
     * @return The import statement of the prefix.
     */
    public String getImport(String prefix) {
        prefix = StringUtils.quoted(prefix);
        String moduleName = this.imports.get(prefix);
        if (moduleName == null) {
            return null;
        }
        return String.format("import %s as %s;", moduleName, prefix);
    }

    /**
     * Whether the prefix was previously added.
     *
     * @param prefix Prefix to search.
     * @return If prefix was added.
     */
    public boolean containsPrefix(String prefix) {
        return this.imports.containsKey(StringUtils.quoted(prefix));
    }

    /**
     * Whether the module was imported before.
     * If yes, then this import does not need to be checked again.
     *
     * @param moduleName Module name to check in 'orgName/module' format.
     * @return If module was added.
     */
    public boolean moduleImported(String moduleName) {
        return this.reverseImports.containsKey(moduleName);
    }

    /**
     * Get the prefix this module name was imported as.
     *
     * @param moduleName Module name to check in 'orgName/module' format.
     * @return Prefix of the import.
     */
    public String prefix(String moduleName) {
        return this.reverseImports.get(moduleName);
    }

    /**
     * Add the prefix and import to the set of remembered imports.
     *
     * @param snippet Import snippet to add.
     * @return The prefix the import was added as.
     */
    public String storeImport(ImportDeclarationSnippet snippet) {
        String quotedPrefix = StringUtils.quoted(snippet.getPrefix());
        String importedModule = snippet.getImportedModule();
        return storeImport(quotedPrefix, importedModule);
    }

    /**
     * Add the prefix and import to the set of remembered imports.
     *
     * @param quotedPrefix Prefix of import.
     * @param moduleName   Module name to add.
     * @return The prefix the import was added as.
     */
    public String storeImport(String quotedPrefix, String moduleName) {
        this.imports.put(quotedPrefix, moduleName);
        this.reverseImports.put(moduleName, quotedPrefix);
        return quotedPrefix;
    }

    /**
     * Add a prefix to persisted list of imports.
     *
     * @param prefix Prefix to add.
     */
    public void storeImplicitPrefix(String prefix) {
        this.implicitImportPrefixes.add(StringUtils.quoted(prefix));
    }

    /**
     * All the prefixes that were added. Prefixes will be quoted.
     *
     * @return Set of prefixes.
     */
    public Set<String> prefixes() {
        return this.imports.keySet();
    }

    /**
     * All the implicit import statements that were remembered.
     *
     * @return Set of implicit import statements.
     */
    public Set<String> getImplicitImports() {
        Set<String> importStrings = new HashSet<>();
        this.implicitImportPrefixes.stream().map(this::getImport)
                .filter(Objects::nonNull).forEach(importStrings::add);
        return importStrings;
    }
}
