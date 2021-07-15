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

import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.shell.snippet.types.ImportDeclarationSnippet;
import io.ballerina.shell.utils.QuotedIdentifier;
import io.ballerina.shell.utils.QuotedImport;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Imports that were stored to be able to search with the prefix.
 * The prefixes used will always be quoted identifiers.
 *
 * @since 2.0.0
 */
public class ImportsManager {
    private static final QuotedIdentifier ANON_SOURCE = new QuotedIdentifier("$");
    private static final Pattern FULLY_QUALIFIED_MODULE_ID_PATTERN =
            Pattern.compile("([\\w]+)/([\\w.]+):([^:]+):([\\w]+)[|]?");
    // Special imports
    private static final String CLONEABLE_TYPE_DEF = "ballerina/lang.value:0:Cloneable";
    private static final String JAVA_IMPORT_SOURCE = "import ballerina/jballerina.java;";
    private static final ImportDeclarationSnippet JAVA_IMPORT;

    // Special prefixes
    private static final String JAVA_PREFIX = "\'java";

    private final AtomicBoolean initialized;

    /**
     * A import prefix index to import implicit imports.
     * The newly done imports (if their default prefixes are not available)
     * will be imported as _0, _1, _2,.. format.
     */
    private static final AtomicInteger implicitImportPrefixIndex = new AtomicInteger(0);

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
    private final HashMap<QuotedIdentifier, QuotedImport> imports;
    /**
     * Hashmap to store user imports.
     */
    private final HashMap<QuotedIdentifier, QuotedImport> userImports;
    /**
     * Reverse map to search the imported module.
     */
    private final HashMap<QuotedImport, QuotedIdentifier> reverseImports;
    /**
     * Import prefixes that are used in each module declaration/var declaration.
     * Key is the name of the module/var declaration. (quoted name)
     * Value is the prefixes used by that name. (quoted prefix)
     * All the implicit imports should be included under ANON_SOURCE name.
     */
    private final Map<QuotedIdentifier, Set<QuotedIdentifier>> usedPrefixes;

    public ImportsManager() {
        this.initialized = new AtomicBoolean(false);
        this.imports = new HashMap<>();
        this.userImports = new HashMap<>();
        this.reverseImports = new HashMap<>();
        this.usedPrefixes = new HashMap<>();
        storeAnonImplicitPrefixes();
    }

    /**
     * Clear the memory of previous imports and reset
     * to original state.
     */
    public void reset() {
        this.imports.clear();
        this.reverseImports.clear();
        this.usedPrefixes.clear();
        this.userImports.clear();
        storeAnonImplicitPrefixes();
    }

    /**
     * Get the import statement of the given prefix.
     *
     * @param prefix Prefix to search.
     * @return The import statement of the prefix.
     */
    public String getImport(QuotedIdentifier prefix) {
        QuotedImport quotedImport = this.imports.get(prefix);
        if (quotedImport == null) {
            return null;
        }
        return String.format("import %s as %s;", quotedImport, prefix);
    }

    /**
     * Whether the prefix was previously added.
     *
     * @param prefix Prefix to search.
     * @return If prefix was added by the user.
     */
    public boolean containsPrefix(QuotedIdentifier prefix) {
        return this.userImports.containsKey(prefix);
    }

    /**
     * Whether the module was imported before.
     * If yes, then this import does not need to be checked again.
     *
     * @param moduleName Module name to check in 'orgName/module' format.
     * @return If module was added.
     */
    public boolean moduleImported(QuotedImport moduleName) {
        return this.reverseImports.containsKey(moduleName);
    }

    /**
     * Get the prefix this module name was imported as.
     *
     * @param moduleName Module name to check in 'orgName/module' format.
     * @return Prefix of the import.
     */
    public QuotedIdentifier prefix(QuotedImport moduleName) {
        return this.reverseImports.get(moduleName);
    }

    /**
     * Add a new import snippet to the set of remembered imports.
     *
     * @param snippet Import snippet to add.
     */
    public void storeImport(ImportDeclarationSnippet snippet) {
        QuotedIdentifier quotedPrefix = snippet.getPrefix();
        QuotedImport importedModule = snippet.getImportedModule();
        storeImport(quotedPrefix, importedModule);
    }

    /**
     * Add prefixes to persisted list of imports.
     * The name will be linked with the import prefix.
     *
     * @param name     Usage source declaration name of the import.
     * @param prefixes Used prefixes.
     */
    public void storeImportUsages(QuotedIdentifier name, Collection<QuotedIdentifier> prefixes) {
        // Get the prefixes previously used by this name and add prefix this to it.
        Set<QuotedIdentifier> sourcePrefixes = this.usedPrefixes.getOrDefault(name, new HashSet<>());
        sourcePrefixes.addAll(prefixes);
        this.usedPrefixes.put(name, sourcePrefixes);
    }

    /**
     * All the prefixes that were added by the user. Prefixes will be quoted.
     * These are essentially all the imports that are managed.
     *
     * @return Set of prefixes.
     */
    public Set<QuotedIdentifier> prefixes() {
        return this.userImports.keySet();
    }

    /**
     * All the import statements that were used by the given names.
     * This will also contain all the default imports. eg: ballerina/jballerina.java
     * Names should be quoted identifiers of module dclns and var dclns.
     *
     * @param names Names to search the used prefixes for.
     * @return Set of import statements used in given names.
     */
    public Set<String> getUsedImports(Collection<QuotedIdentifier> names) {
        // Start with all default imports
        Set<QuotedIdentifier> allUsedImportPrefixes = new HashSet<>(usedPrefixes.get(ANON_SOURCE));

        // Add the imports used by names
        names.stream()
                .map(this.usedPrefixes::get) // get used prefixes of names
                .filter(Objects::nonNull) // discard null lists
                .forEach(allUsedImportPrefixes::addAll);

        // Compose import strings
        Set<String> importStrings = new HashSet<>();
        allUsedImportPrefixes.stream()
                .map(this::getImport) // get the import statement
                .filter(Objects::nonNull) // discard null imports
                .forEach(importStrings::add);

        return importStrings;
    }

    /**
     * Converts a type symbol into its string counterpart.
     * Any imports that need to be done are calculated.
     * Eg: if the type was abc/z:TypeA then it will be converted as
     * 'import abc/z' will be added as an import.
     * We need to traverse all the sub-typed because any sub-type
     * may need to be imported.
     * Also, this will compute all the replacements that should be done.
     * In above case, abc/z:TypeA will be replaced with z:TypeA.
     *
     * @param typeSymbol Type to process.
     * @param imports    Imports set to store imports.
     * @return Type signature after parsing.
     */
    protected String extractImportsFromType(TypeSymbol typeSymbol, Set<QuotedIdentifier> imports) {
        String text = typeSymbol.signature();
        StringBuilder newText = new StringBuilder();
        Matcher matcher = FULLY_QUALIFIED_MODULE_ID_PATTERN.matcher(text);
        int nextStart = 0;
        // Matching Fully-Qualified-Module-IDs (eg.`abc/mod1:1.0.0`)
        // Purpose is to transform `int|abc/mod1:1.0.0:Person` into `int|mod1:Person` or `int|Person`
        // identifying the potential imports required.
        while (matcher.find()) {
            // Append up-to start of the match
            newText.append(text, nextStart, matcher.start(1));
            // Identify org name and module names
            String orgName = matcher.group(1);
            List<String> moduleNames = Arrays.asList(matcher.group(2).split("\\."));
            QuotedImport quotedImport = new QuotedImport(orgName, moduleNames);

            // Add the import required
            QuotedIdentifier quotedPrefix = addImport(quotedImport);
            imports.add(quotedPrefix);

            // Update next-start position
            newText.append(quotedPrefix.getName()).append(":");
            nextStart = matcher.end(3) + 1;
        }
        // Append the remaining
        if (nextStart != 0) {
            newText.append(text.substring(nextStart));
        }
        return newText.length() > 0 ? newText.toString() : text;
    }

    /**
     * Adds a import to this shell session.
     * This will import the module with an already imported prefix,
     * import with default name or using _I format.
     *
     * @param moduleName Module to import in 'orgName/module.name' format
     * @return Prefix imported.
     */
    private QuotedIdentifier addImport(QuotedImport moduleName) {
        // If this module is already imported, use a previous prefix.
        if (moduleImported(moduleName)) {
            return prefix(moduleName);
        }

        // Try to find an available prefix (starting from default prefix and iterate over _I imports)
        QuotedIdentifier quotedPrefix = moduleName.getDefaultPrefix();
        while (containsPrefix(quotedPrefix)) {
            quotedPrefix = new QuotedIdentifier("_" + implicitImportPrefixIndex.incrementAndGet());
        }

        storeImport(quotedPrefix, moduleName);
        return quotedPrefix;
    }

    /**
     * Add prefixes to persisted list of imports that originated without a source.
     * Will be added as an import from ANON_SOURCE.
     * The stored imports are essentially default imports.
     */
    private void storeAnonImplicitPrefixes() {
        storeImport(JAVA_IMPORT);
        storeImportUsages(ANON_SOURCE, List.of(JAVA_IMPORT.getPrefix()));
        this.initialized.set(true);
    }

    /**
     * Add the prefix and import to the set of remembered imports.
     *
     * @param quotedPrefix Prefix of import.
     * @param moduleName   Module name to add.
     */
    private void storeImport(QuotedIdentifier quotedPrefix, QuotedImport moduleName) {
        this.imports.put(quotedPrefix, moduleName);
        this.reverseImports.put(moduleName, quotedPrefix);
        if (!quotedPrefix.toString().equals(JAVA_PREFIX)) {
            this.userImports.put(quotedPrefix, moduleName);
        } else {
            if (this.initialized.get()) {
                this.userImports.put(quotedPrefix, moduleName);
            }
        }
    }
}
