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

import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.shell.exceptions.InvokerException;
import io.ballerina.shell.invoker.ShellSnippetsInvoker;
import io.ballerina.shell.invoker.classload.context.ClassLoadContext;
import io.ballerina.shell.invoker.classload.context.StatementContext;
import io.ballerina.shell.invoker.classload.context.VariableContext;
import io.ballerina.shell.rt.InvokerMemory;
import io.ballerina.shell.snippet.Snippet;
import io.ballerina.shell.snippet.types.DeclarationSnippet;
import io.ballerina.shell.snippet.types.ExecutableSnippet;
import io.ballerina.shell.snippet.types.ImportDeclarationSnippet;
import io.ballerina.shell.snippet.types.ModuleMemberDeclarationSnippet;
import io.ballerina.shell.snippet.types.StatementSnippet;
import io.ballerina.shell.snippet.types.TopLevelDeclarationSnippet;
import io.ballerina.shell.snippet.types.VariableDeclarationSnippet;
import io.ballerina.shell.utils.QuotedIdentifier;
import io.ballerina.shell.utils.StringUtils;
import io.ballerina.shell.utils.timeit.InvokerTimeIt;
import io.ballerina.shell.utils.timeit.TimedOperation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Executes the snippet given.
 * This invoker will save all the variable values in a static class and
 * load them into the generated class effectively managing any side-effects.
 *
 * @since 2.0.0
 */
public class ClassLoadInvoker extends ShellSnippetsInvoker {
    // Context related information
    public static final String CONTEXT_EXPR_VAR_NAME = "__last__";
    // Punctuations
    private static final String DECLARATION_TEMPLATE_FILE = "template.declaration.mustache";
    private static final String EXECUTION_TEMPLATE_FILE = "template.execution.mustache";

    private static final AtomicInteger importIndex = new AtomicInteger(0);
    private static final Pattern FULLY_QUALIFIED_MODULE_ID_PATTERN = Pattern.compile("([\\w]+)/([\\w.]+):([\\d.]+):");

    /**
     * Set of identifiers that are known or seen at the initialization.
     * These can't be overridden.
     */
    private final Set<QuotedIdentifier> initialIdentifiers;
    /**
     * List of imports done.
     * These are imported to the read generated code as necessary.
     */
    private final HashedImports imports;
    /**
     * List of module level declarations such as functions, classes, etc...
     * The snippets are saved as is.
     */
    private final Map<QuotedIdentifier, String> moduleDclns;
    /**
     * List of global variables used in the code.
     * This is a map of variable name to its type.
     * The variable name must be a quoted identifier.
     */
    private final Map<QuotedIdentifier, GlobalVariable> globalVars;
    /**
     * Flag to keep track of whether the invoker is initialized.
     */
    private final AtomicBoolean initialized;
    /**
     * Id of the current invoker context.
     */
    private final String contextId;

    /**
     * Stores all the newly found implicit imports.
     * Persisted at the end of iteration to `imports`.
     * Key is the source snippet name (variable name/dcln name), value is the prefixes.
     */
    private final Map<QuotedIdentifier, Set<QuotedIdentifier>> newImports;
    /**
     * Type symbol of ANY. This is found at initialization.
     * This is used to check is a type symbol can be assigned to any.
     */
    private TypeSymbol anyTypeSymbol;

    /**
     * Creates a class load invoker from the given ballerina home.
     * Ballerina home should be tha path that contains repo directory.
     * It is expected that the runtime is added in the class path.
     */
    public ClassLoadInvoker() {
        this.initialized = new AtomicBoolean(false);
        this.contextId = UUID.randomUUID().toString();
        this.moduleDclns = new HashMap<>();
        this.globalVars = new HashMap<>();
        this.newImports = new HashMap<>();
        this.initialIdentifiers = new HashSet<>();
        this.imports = new HashedImports();
    }

    /**
     * Creates an empty context and loads the project.
     * This will allow compiler to cache necessary data so that
     * subsequent runs will be much more faster.
     * This must be called before calling execute.
     *
     * @throws InvokerException If initialization failed.
     */
    @Override
    public void initialize() throws InvokerException {
        ClassLoadContext emptyContext = new ClassLoadContext(contextId, imports.getUsedImports());
        Project project = getProject(emptyContext, DECLARATION_TEMPLATE_FILE);
        PackageCompilation compilation = compile(project);
        // Remember all the visible var symbols
        // Also use this to cache ANY type symbol
        QuotedIdentifier runFunctionName = new QuotedIdentifier(MODULE_RUN_METHOD_NAME);
        for (GlobalVariableSymbol symbol : globalVariableSymbols(project, compilation)) {
            initialIdentifiers.add(symbol.getName());
            if (symbol.getName().equals(runFunctionName)) {
                assert symbol.getTypeSymbol() instanceof FunctionTypeSymbol;
                FunctionTypeSymbol runFunctionType = (FunctionTypeSymbol) symbol.getTypeSymbol();
                anyTypeSymbol = runFunctionType.returnTypeDescriptor().orElseThrow();
            }
        }
        JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        this.initialized.set(true);
        addDebugDiagnostic("Added initial identifiers: " + initialIdentifiers);
    }

    @Override
    public void reset() {
        // Clear everything in memory
        // data wrt the memory context is also removed.
        this.moduleDclns.clear();
        this.globalVars.clear();
        InvokerMemory.forgetAll(contextId);
        this.initialIdentifiers.clear();
        this.initialized.set(false);
        this.imports.reset();
    }

    @Override
    public void delete(Set<String> declarationNames) throws InvokerException {
        Map<QuotedIdentifier, GlobalVariable> queuedGlobalVars = new HashMap<>();
        Map<QuotedIdentifier, String> queuedModuleDclns = new HashMap<>();
        for (String declarationName : declarationNames) {
            QuotedIdentifier identifier = new QuotedIdentifier(declarationName);
            if (globalVars.containsKey(identifier)) {
                queuedGlobalVars.put(identifier, globalVars.get(identifier));
            } else if (moduleDclns.containsKey(identifier)) {
                queuedModuleDclns.put(identifier, moduleDclns.get(identifier));
            } else {
                addErrorDiagnostic(declarationName + " is not defined.\n" +
                        "Please enter names of declarations that are already defined.");
                throw new InvokerException();
            }
        }

        try {
            queuedGlobalVars.keySet().forEach(globalVars::remove);
            queuedModuleDclns.keySet().forEach(moduleDclns::remove);
            processCurrentState();
        } catch (InvokerException e) {
            globalVars.putAll(queuedGlobalVars);
            moduleDclns.putAll(queuedModuleDclns);
            addErrorDiagnostic("Deleting declaration(s) failed.");
            throw e;
        }
    }

    @Override
    public Optional<Object> execute(Snippet newSnippet) throws InvokerException {
        if (!this.initialized.get()) {
            throw new IllegalStateException("Invoker execution not initialized.");
        }

        newImports.clear();

        // TODO: (#28036) Fix the closure bug.

        if (newSnippet instanceof ImportDeclarationSnippet) {
            // Only compilation to find import validity and exit.
            ImportDeclarationSnippet importDcln = (ImportDeclarationSnippet) newSnippet;
            QuotedIdentifier importPrefix = timedOperation("processing import", () -> processImport(importDcln));
            Objects.requireNonNull(importPrefix, "Import prefix identification failed.");
            addDebugDiagnostic("Import prefix identified as: " + importPrefix);
            return Optional.empty();

        } else if (newSnippet instanceof TopLevelDeclarationSnippet) {
            // Process the declarations
            processDeclarations(List.of((TopLevelDeclarationSnippet) newSnippet));
            return Optional.empty();

        } else if (newSnippet instanceof ExecutableSnippet) {
            // Compile and execute the real program. Also output the EXPR result.
            ClassLoadContext context = createStatementExecutionContext((ExecutableSnippet) newSnippet);
            timedOperation("statement execution", () -> executeProject(context, EXECUTION_TEMPLATE_FILE));
            Object executionResult = InvokerMemory.recall(contextId, CONTEXT_EXPR_VAR_NAME);
            addDebugDiagnostic("Implicit imports added: " + this.newImports);
            return Optional.ofNullable(executionResult);

        } else {
            addErrorDiagnostic("Unexpected snippet type");
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Object executeDeclarations(Collection<DeclarationSnippet> newSnippets) throws InvokerException {
        if (!this.initialized.get()) {
            throw new IllegalStateException("Invoker execution not initialized.");
        }

        // First filter out imports, run imports
        List<TopLevelDeclarationSnippet> declarationSnippets = new ArrayList<>();
        List<ImportDeclarationSnippet> importDeclarationSnippets = new ArrayList<>();
        for (DeclarationSnippet newSnippet : newSnippets) {
            if (newSnippet instanceof TopLevelDeclarationSnippet) {
                declarationSnippets.add((TopLevelDeclarationSnippet) newSnippet);
            } else if (newSnippet instanceof ImportDeclarationSnippet) {
                importDeclarationSnippets.add((ImportDeclarationSnippet) newSnippet);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        // Execute imports
        for (ImportDeclarationSnippet snippet : importDeclarationSnippets) {
            execute(snippet);
        }
        // Execute other snippets
        if (!declarationSnippets.isEmpty()) {
            processDeclarations(declarationSnippets);
        }
        return null;
    }

    /**
     * This is an import. A test import is done to check for errors.
     * It should not give 'module not found' error.
     * Only compilation is done to verify package resolution.
     *
     * @param importSnippet New import snippet string.
     * @return Imported prefix name.
     * @throws InvokerException If importing failed.
     */
    private QuotedIdentifier processImport(ImportDeclarationSnippet importSnippet) throws InvokerException {
        String moduleName = importSnippet.getImportedModule();
        QuotedIdentifier quotedPrefix = importSnippet.getPrefix();

        if (imports.moduleImported(moduleName) && imports.prefix(moduleName).equals(quotedPrefix)) {
            // Same module with same prefix. No need to check.
            return quotedPrefix;
        } else if (imports.containsPrefix(quotedPrefix)) {
            // Prefix is already used. (Not for the same module - checked above)
            addErrorDiagnostic("The import prefix was already used by another import.");
            throw new InvokerException();
        }

        compileImportStatement(importSnippet.toString());
        return imports.storeImport(importSnippet);
    }

    /**
     * Processes declarations (module dcln/var dclns).
     * Compiles once to check syntax and infer variable types.
     * Execution is only done if there is at least one variable dcln.
     *
     * @param declarationSnippets Declarations to process.
     * @throws InvokerException If compilation failed.
     */
    private void processDeclarations(Collection<TopLevelDeclarationSnippet> declarationSnippets)
            throws InvokerException {
        Map<VariableDeclarationSnippet, Set<QuotedIdentifier>> variableDeclarations = new HashMap<>();
        Map<QuotedIdentifier, VariableDeclarationSnippet.TypeInfo> variableTypes = new HashMap<>();
        List<QuotedIdentifier> variableNames = new ArrayList<>();
        Map<QuotedIdentifier, ModuleMemberDeclarationSnippet> moduleDeclarations = new HashMap<>();

        // Fill the required arrays/maps
        for (DeclarationSnippet declarationSnippet : declarationSnippets) {
            Set<QuotedIdentifier> usedPrefixes = new HashSet<>();
            declarationSnippet.usedImports().forEach(p -> usedPrefixes.add(new QuotedIdentifier(p)));

            if (declarationSnippet instanceof VariableDeclarationSnippet) {
                VariableDeclarationSnippet varDclnSnippet = (VariableDeclarationSnippet) declarationSnippet;
                variableNames.addAll(varDclnSnippet.names());
                variableTypes.putAll(varDclnSnippet.types());
                variableDeclarations.put(varDclnSnippet, varDclnSnippet.names());

            } else if (declarationSnippet instanceof ModuleMemberDeclarationSnippet) {
                ModuleMemberDeclarationSnippet moduleDclnSnippet = (ModuleMemberDeclarationSnippet) declarationSnippet;
                QuotedIdentifier moduleDeclarationName = moduleDclnSnippet.name();
                moduleDeclarations.put(moduleDeclarationName, moduleDclnSnippet);
                this.newImports.put(moduleDeclarationName, usedPrefixes);
            }
        }

        // Compile declaration template
        ClassLoadContext context = createDeclarationContext(variableDeclarations.keySet(), variableNames,
                moduleDeclarations);
        Project project = getProject(context, DECLARATION_TEMPLATE_FILE);
        PackageCompilation compilation = compile(project);

        // Only execute if there are variable declarations
        if (!variableNames.isEmpty()) {
            // Find all the global variables that were defined
            Collection<GlobalVariableSymbol> globalVariableSymbols = globalVariableSymbols(project, compilation);

            // Map all variable names with its global variable
            Map<QuotedIdentifier, GlobalVariable> allNewVariables = new HashMap<>();
            for (VariableDeclarationSnippet snippet : variableDeclarations.keySet()) {
                Map<QuotedIdentifier, GlobalVariable> newVariables = createGlobalVariables(
                        snippet.qualifiersAndMetadata(), snippet.names(), variableTypes, globalVariableSymbols);
                allNewVariables.putAll(newVariables);
            }

            // Compile and execute the real program.
            ClassLoadContext execContext = createVariablesExecutionContext(
                    variableDeclarations.keySet(), allNewVariables);
            executeProject(execContext, EXECUTION_TEMPLATE_FILE);
            globalVars.putAll(allNewVariables);
            newImports.forEach(imports::storeImportUsages);
            addDebugDiagnostic("Found new variables: " + allNewVariables);
        }

        // All was successful without error - save state
        this.newImports.forEach(imports::storeImportUsages);
        for (Map.Entry<QuotedIdentifier, ModuleMemberDeclarationSnippet> dcln : moduleDeclarations.entrySet()) {
            String moduleDclnCode = dcln.getValue().toString();
            this.moduleDclns.put(dcln.getKey(), moduleDclnCode);
            addDebugDiagnostic("Module dcln name: " + dcln.getKey());
            addDebugDiagnostic("Module dcln code: " + moduleDclnCode);
        }
        addDebugDiagnostic("Implicit imports added: " + this.newImports);
    }

    /**
     * Processes current state by compiling. Throws an error if compilation fails.
     *
     * @throws InvokerException If state is invalid.
     */
    private void processCurrentState() throws InvokerException {
        Set<String> importStrings = getRequiredImportStatements();
        ClassLoadContext context = new ClassLoadContext(this.contextId, importStrings,
                moduleDclns.values(), globalVariableContexts().values(), null);
        Project project = getProject(context, DECLARATION_TEMPLATE_FILE);
        compile(project);
    }

    /* Context Creation */

    /**
     * Creates a context which can be used to identify new variables and module level dclns.
     *
     * @param variableDeclarations New snippets. Must be a var dclns.
     * @param variableNames        Names of variables in variable declarations.
     * @param moduleDeclarations   Module declarations to load.
     * @return Context with type information inferring code.
     */
    private ClassLoadContext createDeclarationContext(
            Collection<VariableDeclarationSnippet> variableDeclarations,
            Collection<QuotedIdentifier> variableNames,
            Map<QuotedIdentifier, ModuleMemberDeclarationSnippet> moduleDeclarations) {
        // Load current declarations
        Map<QuotedIdentifier, VariableContext> oldVarDclns = globalVariableContexts();
        Map<QuotedIdentifier, String> moduleDclnStringsMap = new HashMap<>(moduleDclns);

        StringJoiner lastVarDclns = new StringJoiner("\n");
        Set<String> importStrings = new HashSet<>();

        // Remove old declarations and add new declarations
        for (Map.Entry<QuotedIdentifier, ModuleMemberDeclarationSnippet> dcln : moduleDeclarations.entrySet()) {
            importStrings.addAll(getRequiredImportStatements(dcln.getValue()));
            moduleDclnStringsMap.put(dcln.getKey(), dcln.getValue().toString());
        }
        // Remove old variable names and process new variable dclns
        variableNames.forEach(oldVarDclns::remove);
        for (VariableDeclarationSnippet varSnippet : variableDeclarations) {
            lastVarDclns.add(varSnippet.toString());
            importStrings.addAll(getRequiredImportStatements(varSnippet));
        }

        return new ClassLoadContext(this.contextId, importStrings, moduleDclnStringsMap.values(),
                oldVarDclns.values(), lastVarDclns.toString());
    }

    /**
     * Creates the context object to be passed to template.
     * Only executable snippets are processed.
     * So, only statements and expressions are processed.
     *
     * @param newSnippet New snippet from user.
     * @return Created context.
     */
    private ClassLoadContext createStatementExecutionContext(ExecutableSnippet newSnippet) {
        Map<QuotedIdentifier, VariableContext> varDclnsMap = globalVariableContexts();
        Set<String> importStrings = getRequiredImportStatements(newSnippet);

        boolean isStatement = newSnippet instanceof StatementSnippet;
        StatementContext lastStatement = new StatementContext(newSnippet.toString(), isStatement);
        return new ClassLoadContext(this.contextId, importStrings, moduleDclns.values(),
                varDclnsMap.values(), null, lastStatement);
    }

    /**
     * Creates the context object to be passed to template.
     * This will process variable snippets.
     *
     * @param newSnippets  New snippets from user.
     * @param newVariables Variables defined in new snippets.
     * @return Created context.
     */
    private ClassLoadContext createVariablesExecutionContext(Collection<VariableDeclarationSnippet> newSnippets,
                                                             Map<QuotedIdentifier, GlobalVariable> newVariables) {
        Map<QuotedIdentifier, VariableContext> varDclnsMap = globalVariableContexts();
        StringJoiner newVariableDclns = new StringJoiner("\n");
        Set<String> importStrings = new HashSet<>();

        for (VariableDeclarationSnippet snippet : newSnippets) {
            importStrings.addAll(getRequiredImportStatements(snippet));
            newVariableDclns.add(snippet.toString());
        }

        newVariables.forEach((k, v) -> varDclnsMap.put(k, VariableContext.newVar(v)));
        return new ClassLoadContext(this.contextId, importStrings, moduleDclns.values(),
                varDclnsMap.values(), newVariableDclns.toString(), null);
    }

    /**
     * Global variables as required by contexts.
     *
     * @return Global variable declarations map.
     */
    private Map<QuotedIdentifier, VariableContext> globalVariableContexts() {
        Map<QuotedIdentifier, VariableContext> varDclns = new HashMap<>();
        globalVars.forEach((k, v) -> varDclns.put(k, VariableContext.oldVar(v)));
        return varDclns;
    }

    /* Util functions */

    /**
     * Processes a variable declaration snippet.
     * We need to know all the variable types.
     * Here, all the global variables are filtered and the types of
     * new variables are found.
     *
     * @param qualifiersAndMetadata Variable snippet qualifiers.
     * @param definedVariables      Variables that were defined.
     * @param definedTypes          Types of the variables.
     *                              If an entry is missing from this, the signature will be used.
     * @param globalVarSymbols      All global variable symbols.
     * @return Exported found variable information (name and type)
     */
    private Map<QuotedIdentifier, GlobalVariable> createGlobalVariables(
            String qualifiersAndMetadata,
            Set<QuotedIdentifier> definedVariables,
            Map<QuotedIdentifier, VariableDeclarationSnippet.TypeInfo> definedTypes,
            Collection<GlobalVariableSymbol> globalVarSymbols) {
        Map<QuotedIdentifier, GlobalVariable> foundVariables = new HashMap<>();
        addDebugDiagnostic("Found variables: " + definedVariables);

        for (GlobalVariableSymbol globalVariableSymbol : globalVarSymbols) {
            QuotedIdentifier variableName = globalVariableSymbol.getName();
            TypeSymbol typeSymbol = globalVariableSymbol.getTypeSymbol();

            // Is a built-in variable/function
            if (initialIdentifiers.contains(variableName)) {
                continue;
            }
            // Is not a variable defined by the snippet in question
            if (!definedVariables.contains(variableName)) {
                continue;
            }

            // Determine the type information
            boolean isAssignableToAny = typeSymbol.assignableTo(anyTypeSymbol);
            // Find the variable type - use syntax tree if possible
            String variableType;
            if (definedTypes.containsKey(variableName)) {
                // We can use syntax tree, add required imports
                VariableDeclarationSnippet.TypeInfo typeInfo = definedTypes.get(variableName);
                variableType = typeInfo.getType();
                this.newImports.put(variableName, typeInfo.getImports());
            } else {
                Set<QuotedIdentifier> requiredImports = new HashSet<>();
                variableType = parseTypeSignature(typeSymbol, requiredImports);
                this.newImports.put(variableName, requiredImports);
            }

            // Create a global variable
            GlobalVariable globalVariable = new GlobalVariable(variableType, variableName,
                    isAssignableToAny, qualifiersAndMetadata);
            foundVariables.put(variableName, globalVariable);
        }

        return foundVariables;
    }

    /**
     * Gets the symbols that are visible globally.
     * Returns only function symbols and variable symbols.
     *
     * @param project     Project to get symbols.
     * @param compilation Compilation object.
     * @return All the visible symbols.
     */
    private Collection<GlobalVariableSymbol> globalVariableSymbols(Project project, PackageCompilation compilation) {
        // Get the document associated with project
        ModuleId moduleId = project.currentPackage().getDefaultModule().moduleId();
        return compilation.getSemanticModel(moduleId).moduleSymbols().stream()
                .filter(s -> s instanceof VariableSymbol || s instanceof FunctionSymbol)
                .map(GlobalVariableSymbol::fromSymbol)
                .collect(Collectors.toList());
    }

    /**
     * Return import strings used by this snippet.
     * All the global imports are also added.
     *
     * @param snippet Snippet to check.
     * @return List of imports.
     */
    private Set<String> getRequiredImportStatements(Snippet snippet) {
        Set<String> importStrings = getRequiredImportStatements();
        // Add all used imports in this snippet
        snippet.usedImports().stream()
                .map(QuotedIdentifier::new)
                .map(imports::getImport).filter(Objects::nonNull)
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
    protected String parseTypeSignature(TypeSymbol typeSymbol, Set<QuotedIdentifier> imports) {
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
            QuotedIdentifier orgName = new QuotedIdentifier(matcher.group(1));
            String[] moduleNames = Arrays
                    .stream(matcher.group(2).split("\\.")) // for each module name part
                    .map(StringUtils::quoted) // quote it
                    .toArray(String[]::new); // collect as a string array
            String moduleName = String.join(".", moduleNames);
            String moduleText = String.format("%s/%s", orgName, moduleName);

            // Add the import required
            QuotedIdentifier quotedPrefix = addImport(moduleText);
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
     * @param moduleText Module to import in 'orgName/module.name' format
     * @return Prefix imported.
     */
    protected QuotedIdentifier addImport(String moduleText) {
        // If this module is already imported, use a previous prefix.
        if (imports.moduleImported(moduleText)) {
            return imports.prefix(moduleText);
        }

        // Try to find an available prefix (starting from default prefix and iterate over _I imports)
        String defaultPrefix = moduleText.replaceAll(".*\\.", "");
        QuotedIdentifier quotedPrefix = new QuotedIdentifier(defaultPrefix);
        while (imports.containsPrefix(quotedPrefix)) {
            quotedPrefix = new QuotedIdentifier("_" + importIndex.incrementAndGet());
        }

        return imports.storeImport(quotedPrefix, moduleText);
    }

    /**
     * Return import strings used.
     *
     * @return List of imports.
     */
    private Set<String> getRequiredImportStatements() {
        Set<String> importStrings = new HashSet<>(imports.getUsedImports()); // Anon imports
        importStrings.addAll(imports.getUsedImports(globalVars.keySet())); // Imports from vars
        importStrings.addAll(imports.getUsedImports(moduleDclns.keySet())); // Imports from module dclns
        return importStrings;
    }

    /* Available statements */

    @Override
    public List<String> availableImports() {
        // Imports with prefixes
        List<String> importStrings = new ArrayList<>();
        for (QuotedIdentifier prefix : imports.prefixes()) {
            importStrings.add(String.format("(%s) %s", prefix, imports.getImport(prefix)));
        }
        return importStrings;
    }

    @Override
    public List<String> availableVariables() {
        // Available variables and values as string.
        List<String> varStrings = new ArrayList<>();
        for (GlobalVariable entry : globalVars.values()) {
            Object obj = InvokerMemory.recall(contextId, entry.getVariableName().getName());
            String objStr = StringUtils.getExpressionStringValue(obj);
            String value = StringUtils.shortenedString(objStr);
            String varString = String.format("(%s) %s %s = %s",
                    entry.getVariableName(), entry.getType(), entry.getVariableName(), value);
            varStrings.add(varString);
        }
        return varStrings;
    }

    @Override
    public List<String> availableModuleDeclarations() {
        // Module level dclns.
        List<String> moduleDclnStrings = new ArrayList<>();
        for (Map.Entry<QuotedIdentifier, String> entry : moduleDclns.entrySet()) {
            String varString = String.format("(%s) %s", entry.getKey(),
                    StringUtils.shortenedString(entry.getValue()));
            moduleDclnStrings.add(varString);
        }
        return moduleDclnStrings;
    }

    /**
     * @return Error stream to print out error messages.
     */
    protected PrintStream getErrorStream() {
        return System.err;
    }

    /**
     * Time the operation and add diagnostics to {@link ClassLoadInvoker}.
     *
     * @param category  Category to add diagnostics. Should be unique per operation.
     * @param operation Operation to perform.
     * @param <T>       operation return type.
     * @return Return value of operation.
     * @throws InvokerException If operation failed.
     */
    private <T> T timedOperation(String category, TimedOperation<T> operation) throws InvokerException {
        return InvokerTimeIt.timeIt(category, this, operation);
    }
}
