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

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.shell.snippet.types.ImportDeclarationSnippet;
import io.ballerina.shell.utils.StringUtils;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Imports that were stored to be able to search with the prefix.
 * The prefixes used will always be quoted identifiers.
 *
 * @since 2.0.0
 */
public class HashedImports {
    private static final String ANON_SOURCE = "'$";
    private static final Collection<String> ANON_IMPORT_PREFIXES = List.of("java");
    private static final String JAVA_IMPORT_SOURCE = "import ballerina/jballerina.java;";
    private static final ImportDeclarationSnippet JAVA_IMPORT;

    static {
        // Set the java import snippet
        TextDocument importText = TextDocuments.from(JAVA_IMPORT_SOURCE);
        SyntaxTree syntaxTree = SyntaxTree.from(importText);
        assert syntaxTree.rootNode() instanceof ModulePartNode;
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        ImportDeclarationNode importDeclaration = modulePartNode.imports().get(0);
        JAVA_IMPORT = new ImportDeclarationSnippet(importDeclaration);
    }

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
     * Import prefixes that are used in each module declaration/var declaration.
     * Key is the name of the module/var declaration. (quoted name)
     * Value is the prefixes used by that name. (quoted prefix)
     * All the implicit imports should be included under ANON_SOURCE name.
     */
    private final Map<String, Set<String>> usedPrefixes;

    public HashedImports() {
        this.imports = new HashMap<>();
        this.reverseImports = new HashMap<>();
        this.usedPrefixes = new HashMap<>();
        storeImport(JAVA_IMPORT);
        storeAnonImplicitPrefixes(ANON_IMPORT_PREFIXES);
    }

    /**
     * Clear the memory of previous imports and reset
     * to original state.
     */
    public void reset() {
        this.imports.clear();
        this.reverseImports.clear();
        this.usedPrefixes.clear();
        storeImport(JAVA_IMPORT);
        storeAnonImplicitPrefixes(ANON_IMPORT_PREFIXES);
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
     * Add prefixes to persisted list of imports.
     * The name will be linked with the import prefix.
     * eg: {@code a:P? p = ()} will have {@code p} as name and {@code a} as prefix.
     *
     * @param name     Usage source declaration name of the import.
     * @param prefixes Used prefixes.
     */
    public void storeImportUsages(String name, Collection<String> prefixes) {
        // Get the prefixes previously used by this name and add prefix this to it.
        Set<String> sourcePrefixes = this.usedPrefixes.getOrDefault(name, new HashSet<>());
        prefixes.stream().map(StringUtils::quoted).forEach(sourcePrefixes::add);
        this.usedPrefixes.put(StringUtils.quoted(name), sourcePrefixes);
    }

    /**
     * Add prefixes to persisted list of imports th
     * at originated without a source.
     * Will be added as an import from ANON_SOURCE.
     */
    public void storeAnonImplicitPrefixes(Collection<String> prefixes) {
        storeImportUsages(ANON_SOURCE, prefixes);
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
    public Set<String> getUsedImports() {
        return getUsedImports(List.of(ANON_SOURCE));
    }

    /**
     * All the import statements that were used by the given names.
     *
     * @return Set of import statements used in given names.
     */
    public Set<String> getUsedImports(Collection<String> names) {
        Set<String> allUsedImportPrefixes = new HashSet<>();
        names.stream()
                .map(this.usedPrefixes::get) // get used prefixes of names
                .filter(Objects::nonNull) // discard null lists
                .forEach(allUsedImportPrefixes::addAll);

        Set<String> importStrings = new HashSet<>();
        allUsedImportPrefixes.stream()
                .map(this::getImport) // get the import statement
                .filter(Objects::nonNull) // discard null imports
                .forEach(importStrings::add);

        return importStrings;
    }
}
