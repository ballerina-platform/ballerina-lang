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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.*;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.*;
import io.ballerina.shell.exceptions.InvokerException;
import io.ballerina.shell.invoker.ShellSnippetsInvoker;
import io.ballerina.shell.invoker.classload.context.ClassLoadContext;
import io.ballerina.shell.invoker.classload.context.StatementContext;
import io.ballerina.shell.invoker.classload.context.VariableContext;
import io.ballerina.shell.rt.InvokerMemory;
import io.ballerina.shell.snippet.Snippet;
import io.ballerina.shell.snippet.types.ExecutableSnippet;
import io.ballerina.shell.snippet.types.ImportDeclarationSnippet;
import io.ballerina.shell.snippet.types.ModuleMemberDeclarationSnippet;
import io.ballerina.shell.snippet.types.StatementSnippet;
import io.ballerina.shell.snippet.types.VariableDeclarationSnippet;
import io.ballerina.shell.utils.Identifier;
import io.ballerina.shell.utils.QuotedImport;
import io.ballerina.shell.utils.StringUtils;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.tools.text.TextRange;

import java.io.PrintStream;
import java.util.ArrayList;
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
    // Templates
    private static final String DECLARATION_TEMPLATE_FILE = "template.declaration.mustache";
    private static final String EXECUTION_TEMPLATE_FILE = "template.execution.mustache";

    private static final String UPDATE_VARIABLE = "__updateVariables";

    /**
     * Set of identifiers that are known or seen at the initialization.
     * These can't be overridden.
     */
    private final Set<Identifier> initialIdentifiers;
    /**
     * List of imports done.
     * These are imported to the read generated code as necessary.
     */
    private final ImportsManager importsManager;
    /**
     * List of module level declarations such as functions, classes, etc...
     * The snippets are saved as is.
     */
    private final Map<Identifier, String> moduleDclns;
    /**
     * List of global variables used in the code.
     * This is a map of variable name to its type.
     * The variable name must be an identifier.
     */
    private final Map<Identifier, GlobalVariable> globalVars;
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
    private final Map<Identifier, Set<Identifier>> newImports;
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
        this.importsManager = new ImportsManager();
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
        ClassLoadContext emptyContext = new ClassLoadContext(contextId, importsManager.getUsedImports(List.of()));
        Project project = getProject(emptyContext, DECLARATION_TEMPLATE_FILE);
        PackageCompilation compilation = compile(project);
        // Remember all the visible var symbols
        // Also use this to cache ANY type symbol
        Identifier runFunctionName = new Identifier(MODULE_RUN_METHOD_NAME);
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
        this.importsManager.reset();
    }

    @Override
    public Optional<Object> execute(Collection<Snippet> newSnippets) throws InvokerException {
        if (!this.initialized.get()) {
            throw new IllegalStateException("Invoker execution not initialized.");
        }

        newImports.clear();

        // TODO: (#28036) Fix the closure bug.

        Map<VariableDeclarationSnippet, Set<Identifier>> variableDeclarations = new HashMap<>();
        Map<Identifier, ModuleMemberDeclarationSnippet> moduleDeclarations = new HashMap<>();
        List<ExecutableSnippet> executableSnippets = new ArrayList<>();
        List<Identifier> variableNames = new ArrayList<>();

        // Fill the required arrays/maps
        // Only compilation to find import validity.
        // All imports are done on-the-spot.
        // Others are processed later.
        for (Snippet newSnippet : newSnippets) {

            if (newSnippet instanceof ImportDeclarationSnippet) {
                processImport((ImportDeclarationSnippet) newSnippet);

            } else if (newSnippet instanceof VariableDeclarationSnippet) {
                VariableDeclarationSnippet varDclnSnippet = (VariableDeclarationSnippet) newSnippet;
                variableNames.addAll(varDclnSnippet.names());
                variableDeclarations.put(varDclnSnippet, varDclnSnippet.names());

            } else if (newSnippet instanceof ModuleMemberDeclarationSnippet) {
                ModuleMemberDeclarationSnippet moduleDclnSnippet = (ModuleMemberDeclarationSnippet) newSnippet;
                Identifier moduleDeclarationName = moduleDclnSnippet.name();
                moduleDeclarations.put(moduleDeclarationName, moduleDclnSnippet);
                Set<Identifier> usedPrefixes = newSnippet.usedImports().stream()
                        .map(Identifier::new).collect(Collectors.toSet());
                newImports.put(moduleDeclarationName, usedPrefixes);

            } else if (newSnippet instanceof ExecutableSnippet) {
                executableSnippets.add((ExecutableSnippet) newSnippet);

            } else {
                throw new UnsupportedOperationException("Unimplemented snippet category.");
            }
        }

        boolean noModuleDeclarations = moduleDeclarations.isEmpty();
        boolean noVariableDeclarations = variableDeclarations.isEmpty();
        boolean noExecutables = executableSnippets.isEmpty();

        // If there are only imports (no other snippets), just stop execution.
        if (noModuleDeclarations && noVariableDeclarations && noExecutables) {
            return Optional.empty();
        }

        // If there are no declarations/variables, we can simply execute.
        if (noModuleDeclarations && noVariableDeclarations) {
            ClassLoadContext execContext = createVariablesExecutionContext(List.of(), executableSnippets, Map.of());
            Project customProject = updateSyntaxTree(execContext);
            executeProjectNew(customProject);
            return Optional.ofNullable(InvokerMemory.recall(contextId, CONTEXT_EXPR_VAR_NAME));
        }

        // Compile declaration template if there were declarations
        ClassLoadContext context = createDeclarationContext(variableDeclarations.keySet(), variableNames,
                moduleDeclarations);
        Project project = getProject(context, DECLARATION_TEMPLATE_FILE);
        PackageCompilation compilation = compile(project);

        // Compilation was successful, so we can add the declarations
        // to the persisted list. Here everything is persisted.
        // Please note that this would be reversed if something went wrong in execution.

        // Find all the global variables that were defined
        // and Map all variable names with its global variable
        Collection<GlobalVariableSymbol> globalVariableSymbols = globalVariableSymbols(project, compilation);
        Map<Identifier, GlobalVariable> allNewVariables = new HashMap<>();
        for (VariableDeclarationSnippet snippet : variableDeclarations.keySet()) {
            Map<Identifier, GlobalVariable> newVariables = createGlobalVariables(
                    snippet.qualifiersAndMetadata(), snippet.names(), globalVariableSymbols,
                    snippet.isDeclaredWithVar());
            allNewVariables.putAll(newVariables);
        }
        // Persist all data
        globalVars.putAll(allNewVariables);
        newImports.forEach(importsManager::storeImportUsages);
        addDebugDiagnostic("Implicit imports added: " + newImports);
        addDebugDiagnostic("Found new variables: " + allNewVariables);
        for (Map.Entry<Identifier, ModuleMemberDeclarationSnippet> dcln : moduleDeclarations.entrySet()) {
            String moduleDclnCode = dcln.getValue().toString();
            moduleDclns.put(dcln.getKey(), moduleDclnCode);
            addDebugDiagnostic("Module dcln name: " + dcln.getKey());
            addDebugDiagnostic("Module dcln code: " + moduleDclnCode);
        }
        // No need to execute if none are variable declarations/executable snippets
        if (variableDeclarations.isEmpty() && executableSnippets.isEmpty()) {
            return Optional.empty();
        }

        // Recompile and Execute.
        try {
            ClassLoadContext execContext = createVariablesExecutionContext(
                    variableDeclarations.keySet(), executableSnippets, allNewVariables);
            Project customProject = updateSyntaxTree(execContext);
            executeProjectNew(customProject);
            return Optional.ofNullable(InvokerMemory.recall(contextId, CONTEXT_EXPR_VAR_NAME));
        } catch (InvokerException e) {
            // Execution failed... Reverse all by deleting declarations.
            Set<String> identifiersToDelete = new HashSet<>();
            allNewVariables.keySet().forEach(id -> identifiersToDelete.add(id.getName()));
            moduleDeclarations.keySet().forEach(id -> identifiersToDelete.add(id.getName()));
            delete(identifiersToDelete);
            throw e;
        }
    }

    @Override
    public void delete(Set<String> declarationNames) throws InvokerException {
        Map<Identifier, GlobalVariable> queuedGlobalVars = new HashMap<>();
        Map<Identifier, String> queuedModuleDclns = new HashMap<>();
        for (String declarationName : declarationNames) {
            Identifier identifier = new Identifier(declarationName);
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

    /* Process Snippets */

    /**
     * This is an import. A test import is done to check for errors.
     * It should not give 'module not found' error.
     * Only compilation is done to verify package resolution.
     *
     * @param importSnippet New import snippet string.
     * @throws InvokerException If importing failed.
     */
    private void processImport(ImportDeclarationSnippet importSnippet) throws InvokerException {
        QuotedImport quotedImport = importSnippet.getImportedModule();
        Identifier prefix = importSnippet.getPrefix();

        if (importsManager.moduleImported(quotedImport)
                && importsManager.prefix(quotedImport).equals(prefix)
                && importsManager.containsPrefix(prefix)) {
            // Same module with same prefix. No need to check.
            addDebugDiagnostic("Detected reimport: " + prefix);
            return;
        } else if (importsManager.containsPrefix(prefix)) {
            // Prefix is already used. (Not for the same module - checked above)
            addErrorDiagnostic("The import prefix was already used by another import.");
            throw new InvokerException();
        }

        compileImportStatement(importSnippet.toString());
        addDebugDiagnostic("Adding import: " + importSnippet);
        importsManager.storeImport(importSnippet);
    }

    /**
     * Processes current state by compiling. Throws an error if compilation fails.
     *
     * @throws InvokerException If state is invalid.
     */
    private void processCurrentState() throws InvokerException {
        Set<String> importStrings = getRequiredImportStatements();
        ClassLoadContext context = new ClassLoadContext(this.contextId, importStrings,
                moduleDclns.values(), globalVariableContexts().values(), null, null);
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
            Collection<Identifier> variableNames,
            Map<Identifier, ModuleMemberDeclarationSnippet> moduleDeclarations) {
        // Load current declarations
        Map<Identifier, VariableContext> oldVarDclns = globalVariableContexts();
        Map<Identifier, String> moduleDclnStringsMap = new HashMap<>(moduleDclns);

        StringJoiner lastVarDclns = new StringJoiner("\n");
        Set<String> importStrings = new HashSet<>();

        // Remove old declarations and add new declarations
        for (Map.Entry<Identifier, ModuleMemberDeclarationSnippet> dcln : moduleDeclarations.entrySet()) {
            importStrings.addAll(getRequiredImportStatements(dcln.getValue()));
            moduleDclnStringsMap.put(dcln.getKey(), dcln.getValue().toString());
        }
        // Remove old variable names and process new variable dclns
        variableNames.forEach(oldVarDclns::remove);
        for (VariableDeclarationSnippet varSnippet : variableDeclarations) {
            lastVarDclns.add(varSnippet.toString());
            importStrings.addAll(getRequiredImportStatements(varSnippet));
        }
        Collection<String> variableNameStrs = variableNames.stream()
                .map(Identifier::getName)
                .collect(Collectors.toSet());

        return new ClassLoadContext(this.contextId, importStrings, moduleDclnStringsMap.values(),
                oldVarDclns.values(), variableNameStrs, lastVarDclns.toString());
    }

    /**
     * Creates the context object to be passed to template.
     * This will process variable snippets.
     *
     * @param variableDeclarationSnippets New snippets from user.
     * @param executableSnippets          New executable snippets.
     * @param newVariables                Variables defined in new snippets.
     * @return Created context.
     */
    private ClassLoadContext createVariablesExecutionContext(
            Collection<VariableDeclarationSnippet> variableDeclarationSnippets,
            Collection<ExecutableSnippet> executableSnippets,
            Map<Identifier, GlobalVariable> newVariables) {

        Map<Identifier, VariableContext> varDclnsMap = globalVariableContexts();
        StringJoiner newVariableDclns = new StringJoiner("\n");
        Set<String> importStrings = new HashSet<>();

        for (VariableDeclarationSnippet snippet : variableDeclarationSnippets) {
            importStrings.addAll(getRequiredImportStatements(snippet));
            newVariableDclns.add(snippet.toString());
        }

        List<StatementContext> lastStatements = new ArrayList<>();
        for (ExecutableSnippet newSnippet : executableSnippets) {
            importStrings.addAll(getRequiredImportStatements(newSnippet));
            boolean isStatement = newSnippet instanceof StatementSnippet;
            lastStatements.add(new StatementContext(newSnippet.toString(), isStatement));
        }

        newVariables.forEach((k, v) -> varDclnsMap.put(k, VariableContext.newVar(v)));
        return new ClassLoadContext(this.contextId, importStrings, moduleDclns.values(),
                varDclnsMap.values(), null, newVariableDclns.toString(), lastStatements);
    }

    /**
     * Global variables as required by contexts.
     *
     * @return Global variable declarations map.
     */
    private Map<Identifier, VariableContext> globalVariableContexts() {
        Map<Identifier, VariableContext> varDclns = new HashMap<>();
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
     * @param globalVarSymbols      All global variable symbols.
     * @param isDeclaredWithVar     Declared with a var or not.
     * @return Exported found variable information (name and type)
     */
    private Map<Identifier, GlobalVariable> createGlobalVariables(
            String qualifiersAndMetadata,
            Set<Identifier> definedVariables,
            Collection<GlobalVariableSymbol> globalVarSymbols,
            boolean isDeclaredWithVar) {
        Map<Identifier, GlobalVariable> foundVariables = new HashMap<>();
        addDebugDiagnostic("Found variables: " + definedVariables);

        for (GlobalVariableSymbol globalVariableSymbol : globalVarSymbols) {
            Identifier variableName = globalVariableSymbol.getName();
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
            boolean isAssignableToAny = typeSymbol.subtypeOf(anyTypeSymbol);
            // Find the variable type
            Set<Identifier> requiredImports = new HashSet<>();
            String variableType = importsManager.extractImportsFromType(typeSymbol, requiredImports);
            this.newImports.put(new Identifier(variableName.getName()), requiredImports);
            GlobalVariable globalVariable = new GlobalVariable(variableType, isDeclaredWithVar,
                    variableName, isAssignableToAny, qualifiersAndMetadata);
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
                .map(Identifier::new)
                .map(importsManager::getImport).filter(Objects::nonNull)
                .forEach(importStrings::add);
        return importStrings;
    }

    /**
     * Return import strings used.
     *
     * @return List of imports.
     */
    private Set<String> getRequiredImportStatements() {
        Set<String> importStrings = new HashSet<>();
        importStrings.addAll(importsManager.getUsedImports(globalVars.keySet())); // Imports from vars
        importStrings.addAll(importsManager.getUsedImports(moduleDclns.keySet())); // Imports from module dclns
        return importStrings;
    }

    /* Available statements */

    @Override
    public List<String> availableImports() {
        // Imports with prefixes
        List<String> importStrings = new ArrayList<>();
        for (Identifier prefix : importsManager.prefixes()) {
            String importStatement = String.format("(%s) %s", prefix, importsManager.getImport(prefix));
            importStrings.add(importStatement);
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
        for (Map.Entry<Identifier, String> entry : moduleDclns.entrySet()) {
            String varString = String.format("(%s) %s", entry.getKey(),
                    StringUtils.shortenedString(entry.getValue()));
            moduleDclnStrings.add(varString);
        }
        return moduleDclnStrings;
    }

    /**
     * @return Error stream to print out error messages.
     */
    @Override
    protected PrintStream getErrorStream() {
        return System.err;
    }

    /**
     * @return Updated syntax tree.
     */
    public Project updateSyntaxTree(ClassLoadContext execContext) throws InvokerException {
        String balCode = getProjectDoc(execContext, EXECUTION_TEMPLATE_FILE);
        TextDocument document = TextDocuments.from(balCode);
        SyntaxTree syntaxTree = SyntaxTree.from(document);

        Project projectNew = getProject(execContext, EXECUTION_TEMPLATE_FILE);
        PackageCompilation compilationNew = compile(projectNew);
        ModuleId moduleId = projectNew.currentPackage().getDefaultModule().moduleId();
        SemanticModel semanticModel = compilationNew.getSemanticModel(moduleId);
        List<Symbol> symbols = semanticModel.moduleSymbols().stream().filter(s -> s instanceof VariableSymbol).
                collect(Collectors.toList());
        List<Symbol> functions = semanticModel.moduleSymbols().stream().filter(s -> s instanceof FunctionSymbol)
                .collect(Collectors.toList());
        Map<Symbol,List<Location>> refs = new HashMap<>();
        // use node list
        ArrayList<Node> nodeList = new ArrayList<>();
        for (Symbol symbol:symbols) {
            refs.put(symbol, semanticModel.references(symbol));
        }

        TextRange functionTextRange;
        TextRange functionPosition = null;
        for (Symbol function : functions) {
            if (function.nameEquals(UPDATE_VARIABLE)) {
                Optional<Location> location = function.getLocation();
                if (location.isPresent()) {
                    functionTextRange = location.get().textRange();
                    Node functionNode = ((ModulePartNode) syntaxTree.rootNode()).findNode(functionTextRange);
                    functionPosition = functionNode.textRange();
                } // else throw error
                break;
            }
        }

        for (Symbol symbol : symbols) {
            List<Location> locations = refs.get(symbol);
            for (int i = 1; i < locations.size(); i++) {
                TextRange symbolTextRange = locations.get(i).textRange();

                int startOffset = symbolTextRange.startOffset();
                int endOffset = symbolTextRange.endOffset();

                if (!((functionPosition.startOffset() < startOffset) && (endOffset < functionPosition.endOffset()))) {
                    nodeList.add(((ModulePartNode) syntaxTree.rootNode()).findNode(symbolTextRange));
                }
            }
        }

        NodeRewriter nodeRewriter = new NodeRewriter();
        for (Node node:nodeList) {
            List<Node> replacementNodes = nodeRewriter.accept(node);
            if (replacementNodes != null) {
                for (Node replacementNode: replacementNodes) {
                    if (replacementNode != null) {
                        syntaxTree = syntaxTree.replaceNode(node, replacementNode);
                    }
                }
            }
        }

        String newBalCode = syntaxTree.toSourceCode();
        Project customProject = getProject(newBalCode, true);
        return customProject;
    }
}
