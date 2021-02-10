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

import com.github.mustachejava.Mustache;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.shell.Diagnostic;
import io.ballerina.shell.exceptions.InvokerException;
import io.ballerina.shell.exceptions.InvokerPanicException;
import io.ballerina.shell.invoker.Invoker;
import io.ballerina.shell.invoker.classload.context.ClassLoadContext;
import io.ballerina.shell.invoker.classload.context.StatementContext;
import io.ballerina.shell.invoker.classload.context.VariableContext;
import io.ballerina.shell.rt.InvokerMemory;
import io.ballerina.shell.snippet.Snippet;
import io.ballerina.shell.snippet.types.DeclarationSnippet;
import io.ballerina.shell.snippet.types.ExecutableSnippet;
import io.ballerina.shell.snippet.types.ImportDeclarationSnippet;
import io.ballerina.shell.snippet.types.ModuleMemberDeclarationSnippet;
import io.ballerina.shell.snippet.types.VariableDeclarationSnippet;
import io.ballerina.shell.utils.StringUtils;
import io.ballerina.shell.utils.timeit.InvokerTimeIt;
import io.ballerina.shell.utils.timeit.TimedOperation;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
public class ClassLoadInvoker extends Invoker {
    // Context related information
    public static final String CONTEXT_EXPR_VAR_NAME = "__last__";
    // Main class and method names to invoke
    public static final String MODULE_NOT_FOUND_CODE = "BCE2003";
    protected static final String MODULE_INIT_CLASS_NAME = "$_init";
    protected static final String MODULE_INIT_METHOD_NAME = "$moduleInit";
    protected static final String MODULE_RUN_METHOD_NAME = "run";
    protected static final String DOLLAR = "$";
    // Punctuations
    private static final String DECLARATION_TEMPLATE_FILE = "template.declaration.mustache";
    private static final String IMPORT_TEMPLATE_FILE = "template.import.mustache";
    private static final String EXECUTION_TEMPLATE_FILE = "template.execution.mustache";

    private static final AtomicInteger importIndex = new AtomicInteger(0);
    private static final AtomicInteger unnamedModuleNameIndex = new AtomicInteger(0);
    private static final Pattern FULLY_QUALIFIED_MODULE_ID_PATTERN = Pattern.compile("([\\w]+)/([\\w.]+):([\\d.]+):");

    private static TypeSymbol anyTypeSymbol;


    /**
     * Set of identifiers that are known or seen at the initialization.
     * These can't be overridden.
     */
    protected final Set<QuotedIdentifier> initialIdentifiers;
    /**
     * List of imports done.
     * These are imported to the read generated code as necessary.
     */
    protected final HashedImports imports;
    /**
     * List of module level declarations such as functions, classes, etc...
     * The snippets are saved as is.
     */
    protected final Map<QuotedIdentifier, String> moduleDclns;
    /**
     * List of global variables used in the code.
     * This is a map of variable name to its type.
     * The variable name must be a quoted identifier.
     */
    protected final Map<QuotedIdentifier, GlobalVariable> globalVars;
    /**
     * Flag to keep track of whether the invoker is initialized.
     */
    protected final AtomicBoolean initialized;
    /**
     * Id of the current invoker context.
     */
    protected final String contextId;
    /**
     * Stores all the newly found implicit imports.
     * Persisted at the end of iteration to `mustImportPrefixes`.
     * Key is the source snippet name (variable name/dcln name), value is the prefix.
     */
    private final Map<QuotedIdentifier, Set<QuotedIdentifier>> newImports;
    /**
     * Scheduler used to run the init and main methods
     * of the generated classes.
     */
    private final Scheduler scheduler;

    /**
     * Creates a class load invoker from the given ballerina home.
     * Ballerina home should be tha path that contains repo directory.
     * It is expected that the runtime is added in the class path.
     */
    public ClassLoadInvoker() {
        this.scheduler = new Scheduler(false);
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
        SingleFileProject project = getProject(emptyContext, DECLARATION_TEMPLATE_FILE);
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
        addDiagnostic(Diagnostic.debug("Added initial identifiers: " + initialIdentifiers));
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
                addDiagnostic(Diagnostic.error(declarationName + " is not defined.\n" +
                        "Please enter names of declarations that are already defined."));
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
        }
    }

    @Override
    public Optional<Object> execute(Snippet newSnippet) throws InvokerException {
        if (!this.initialized.get()) {
            throw new IllegalStateException("Invoker execution not initialized.");
        }

        newImports.clear();

        // TODO: Fix the closure bug. Following will not work with isolated functions.
        // newSnippet.modify(new GlobalLoadModifier(globalVars));

        switch (newSnippet.getKind()) {
            case IMPORT_DECLARATION:
                // Only compilation to find import validity and exit.
                assert newSnippet instanceof ImportDeclarationSnippet;
                ImportDeclarationSnippet importDcln = (ImportDeclarationSnippet) newSnippet;
                QuotedIdentifier importPrefix = timedOperation("processing import", () -> processImport(importDcln));
                Objects.requireNonNull(importPrefix, "Import prefix identification failed.");
                addDiagnostic(Diagnostic.debug("Import prefix identified as: " + importPrefix));
                return Optional.empty();

            case MODULE_MEMBER_DECLARATION:
                // Only compilation to find dcln validity and exit.
                assert newSnippet instanceof ModuleMemberDeclarationSnippet;
                ModuleMemberDeclarationSnippet moduleDcln = (ModuleMemberDeclarationSnippet) newSnippet;
                Map.Entry<QuotedIdentifier, String> newModuleDcln = timedOperation("processing module dcln",
                        () -> processModuleDcln(moduleDcln));
                this.newImports.forEach(imports::storeImportUsages);
                this.moduleDclns.put(newModuleDcln.getKey(), newModuleDcln.getValue());
                addDiagnostic(Diagnostic.debug("Module dcln name: " + newModuleDcln.getKey()));
                addDiagnostic(Diagnostic.debug("Module dcln code: " + newModuleDcln.getValue()));
                addDiagnostic(Diagnostic.debug("Implicit imports added: " + this.newImports));
                return Optional.empty();

            case VARIABLE_DECLARATION:
            case STATEMENT:
            case EXPRESSION:
                assert newSnippet instanceof ExecutableSnippet;
                Map<QuotedIdentifier, GlobalVariable> newVariables = new HashMap<>();
                if (newSnippet.isVariableDeclaration()) {
                    assert newSnippet instanceof VariableDeclarationSnippet;
                    VariableDeclarationSnippet varDcln = (VariableDeclarationSnippet) newSnippet;
                    newVariables = timedOperation("processing var dcln", () -> processVarDcln(varDcln));
                }

                // Compile and execute the real program.
                ClassLoadContext context = createExecutionContext((ExecutableSnippet) newSnippet, newVariables);
                SingleFileProject project = timedOperation("building project",
                        () -> getProject(context, EXECUTION_TEMPLATE_FILE));
                PackageCompilation compilation = timedOperation("compilation",
                        () -> compile(project));
                JBallerinaBackend jBallerinaBackend = timedOperation("backend fetch",
                        () -> JBallerinaBackend.from(compilation, JvmTarget.JAVA_11));
                boolean isExecutionSuccessful = timedOperation("project execution",
                        () -> executeProject(jBallerinaBackend));

                if (!isExecutionSuccessful) {
                    addDiagnostic(Diagnostic.error("Unhandled Runtime Error."));
                    throw new InvokerException();
                }

                // Save required data if execution was successful
                Object executionResult = InvokerMemory.recall(contextId, CONTEXT_EXPR_VAR_NAME);
                if (newSnippet.isVariableDeclaration()) {
                    globalVars.putAll(newVariables);
                    newImports.forEach(imports::storeImportUsages);
                }
                addDiagnostic(Diagnostic.debug("Found new variables: " + newVariables));
                addDiagnostic(Diagnostic.debug("Implicit imports added: " + this.newImports));
                return Optional.ofNullable(executionResult);

            default:
                addDiagnostic(Diagnostic.error("Unexpected snippet type."));
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public Object executeDeclarations(Collection<DeclarationSnippet> newSnippets) throws InvokerException {
        // TODO: Implement a more better way to run all snippets at once
        for (DeclarationSnippet newSnippet : newSnippets) {
            execute(newSnippet);
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
    public QuotedIdentifier processImport(ImportDeclarationSnippet importSnippet) throws InvokerException {
        String moduleName = importSnippet.getImportedModule();
        QuotedIdentifier quotedPrefix = new QuotedIdentifier(importSnippet.getPrefix());

        if (imports.moduleImported(moduleName) && imports.prefix(moduleName).equals(quotedPrefix)) {
            // Same module with same prefix. No need to check.
            return quotedPrefix;
        } else if (imports.containsPrefix(quotedPrefix)) {
            // Prefix is already used. (Not for the same module - checked above)
            addDiagnostic(Diagnostic.error("The import prefix was already used by another import."));
            throw new InvokerException();
        }

        if (isImportStatementValid(importSnippet.toString())) {
            return imports.storeImport(importSnippet);
        }
        throw new InvokerException();
    }

    /**
     * Processes a variable declaration snippet.
     * We need to know all the variable types.
     * Some types (var) are determined at compile time.
     * So we have to compile once and know the names and types of variables.
     * Only compilation is done.
     *
     * @param newSnippet New variable declaration snippet.
     * @return Exported found variable information (name and type)
     * @throws InvokerException If type/name inferring failed.
     */
    private Map<QuotedIdentifier, GlobalVariable> processVarDcln(VariableDeclarationSnippet newSnippet)
            throws InvokerException {
        Set<QuotedIdentifier> definedVariables = newSnippet.names().stream()
                .map(QuotedIdentifier::new).collect(Collectors.toSet());

        // No matter the approach, compile. This will confirm that syntax is valid.
        ClassLoadContext varTypeInferContext = createVarTypeInferContext(newSnippet, definedVariables);
        SingleFileProject project = getProject(varTypeInferContext, DECLARATION_TEMPLATE_FILE);
        PackageCompilation compilation = compile(project);
        Collection<GlobalVariableSymbol> globalVariableSymbols = globalVariableSymbols(project, compilation);

        String qualifiersAndMetadata = newSnippet.qualifiersAndMetadata();
        Map<QuotedIdentifier, GlobalVariable> foundVariables = new HashMap<>();
        addDiagnostic(Diagnostic.debug("Found variables: " + definedVariables));

        for (GlobalVariableSymbol globalVariableSymbol : globalVariableSymbols) {
            QuotedIdentifier variableName = globalVariableSymbol.getName();
            TypeSymbol typeSymbol = globalVariableSymbol.getTypeSymbol();
            if (initialIdentifiers.contains(variableName)) {
                continue;
            }
            if (!(definedVariables.contains(variableName))) {
                continue;
            }

            boolean isAssignableToAny = typeSymbol.assignableTo(anyTypeSymbol);

            Set<QuotedIdentifier> requiredImports = new HashSet<>();
            String variableType = parseTypeSignature(typeSymbol, requiredImports);
            this.newImports.put(variableName, requiredImports);

            GlobalVariable globalVariable = new GlobalVariable(variableType, variableName,
                    isAssignableToAny, qualifiersAndMetadata);
            foundVariables.put(variableName, globalVariable);
        }

        return foundVariables;
    }

    /**
     * Processes a variable declaration snippet. Only compilation is done.
     * TODO: Support enums.
     *
     * @param newSnippet New snippet to process.
     * @return The newly found type name and its declaration.
     * @throws InvokerException If module dcln is invalid.
     */
    private Map.Entry<QuotedIdentifier, String> processModuleDcln(ModuleMemberDeclarationSnippet newSnippet)
            throws InvokerException {
        Optional<String> moduleDeclarationNameOp = newSnippet.name();
        QuotedIdentifier moduleDeclarationName = moduleDeclarationNameOp.map(QuotedIdentifier::new)
                .orElseGet(() -> new QuotedIdentifier(DOLLAR + unnamedModuleNameIndex.getAndIncrement()));

        Set<QuotedIdentifier> usedPrefixes = new HashSet<>();
        newSnippet.usedImports().forEach(p -> usedPrefixes.add(new QuotedIdentifier(p)));
        this.newImports.put(moduleDeclarationName, usedPrefixes);

        ClassLoadContext varTypeInferContext = createModuleDclnNameInferContext(moduleDeclarationName, newSnippet);
        SingleFileProject project = getProject(varTypeInferContext, DECLARATION_TEMPLATE_FILE);
        compile(project);
        return Map.entry(moduleDeclarationName, newSnippet.toString());
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

    /**
     * Creates a context which can be used to check import validation.
     *
     * @param importString Import declaration snippet string.
     * @return Context with import checking code.
     */
    protected ClassLoadContext createImportInferContext(String importString) {
        return new ClassLoadContext(this.contextId, List.of(importString));
    }

    /**
     * Creates a context which can be used to identify new variables.
     *
     * @param newSnippet       New snippet. Must be a var dcln.
     * @param newVariableNames Variable names in newSnippet.
     * @return Context with type information inferring code.
     */
    protected ClassLoadContext createVarTypeInferContext(VariableDeclarationSnippet newSnippet,
                                                         Collection<QuotedIdentifier> newVariableNames) {
        Map<QuotedIdentifier, VariableContext> varDclnsMap = globalVariableContexts();
        newVariableNames.forEach(varDclnsMap::remove);
        Collection<VariableContext> varDclns = varDclnsMap.values();

        List<String> moduleDclnStrings = new ArrayList<>(moduleDclns.values());
        Set<String> importStrings = getRequiredImportStatements(newSnippet);
        String lastVarDclnString = newSnippet.toString();

        return new ClassLoadContext(this.contextId, importStrings, moduleDclnStrings, varDclns, lastVarDclnString);
    }

    /**
     * Creates a context which can be used to find declaration name.
     *
     * @param newSnippet New snippet. Must be a module member dcln.
     * @return Context to infer dcln name.
     */
    protected ClassLoadContext createModuleDclnNameInferContext(QuotedIdentifier moduleDeclarationName,
                                                                ModuleMemberDeclarationSnippet newSnippet) {
        Collection<VariableContext> varDclns = globalVariableContexts().values();
        Map<QuotedIdentifier, String> moduleDclnStringsMap = new HashMap<>(moduleDclns);
        moduleDclnStringsMap.put(moduleDeclarationName, newSnippet.toString());

        ArrayList<String> moduleDclnStrings = new ArrayList<>(moduleDclnStringsMap.values());
        Set<String> importStrings = getRequiredImportStatements(newSnippet);

        return new ClassLoadContext(this.contextId, importStrings, moduleDclnStrings, varDclns, null);
    }

    /**
     * Creates the context object to be passed to template.
     * The new snippets are not added here. Instead they are added to copies.
     * Only executable snippets are processed.
     *
     * @param newSnippet   New snippet from user.
     * @param newVariables Newly defined variables. Must be set if snippet is a var dcln.
     * @return Created context.
     */
    protected ClassLoadContext createExecutionContext(ExecutableSnippet newSnippet,
                                                      Map<QuotedIdentifier, GlobalVariable> newVariables) {
        Map<QuotedIdentifier, VariableContext> varDclnsMap = globalVariableContexts();
        Set<String> importStrings = getRequiredImportStatements(newSnippet);

        if (newSnippet.isVariableDeclaration()) {
            newVariables.forEach((k, v) -> varDclnsMap.put(k, VariableContext.newVar(v)));
            return new ClassLoadContext(this.contextId, importStrings, moduleDclns.values(),
                    varDclnsMap.values(), newSnippet.toString(), null);
        } else {
            StatementContext lastStatement = new StatementContext(newSnippet);
            return new ClassLoadContext(this.contextId, importStrings, moduleDclns.values(),
                    varDclnsMap.values(), null, lastStatement);
        }
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

    /**
     * Get the project with the context data.
     *
     * @param context      Context to create the ballerina file.
     * @param templateFile Template file to load.
     * @return Created ballerina project.
     * @throws InvokerException If file writing failed.
     */
    protected SingleFileProject getProject(Object context, String templateFile) throws InvokerException {
        Mustache template = super.getTemplate(templateFile);
        try (StringWriter stringWriter = new StringWriter()) {
            template.execute(stringWriter, context);
            return getProject(stringWriter.toString());
        } catch (IOException e) {
            addDiagnostic(Diagnostic.error("File generation failed: " + e.getMessage()));
            throw new InvokerException(e);
        }
    }

    /**
     * Get the project with the context data.
     *
     * @param source Source to use for generating project.
     * @return Created ballerina project.
     * @throws InvokerException If file writing failed.
     */
    protected SingleFileProject getProject(String source) throws InvokerException {
        try {
            File mainBal = writeToFile(source);
            BuildOptions buildOptions = new BuildOptionsBuilder().offline(true).build();
            return SingleFileProject.load(mainBal.toPath(), buildOptions);
        } catch (IOException e) {
            addDiagnostic(Diagnostic.error("File writing failed: " + e.getMessage()));
            throw new InvokerException(e);
        }
    }

    /**
     * Executes a compiled project.
     * It is expected that the project had no compiler errors.
     * The process is run and the stdout is collected and printed.
     *
     * @param jBallerinaBackend Backed to use.
     * @return Whether process execution was successful.
     * @throws InvokerException If execution failed.
     */
    protected boolean executeProject(JBallerinaBackend jBallerinaBackend) throws InvokerException {
        PrintStream errorStream = getErrorStream();
        try {
            JarResolver jarResolver = jBallerinaBackend.jarResolver();
            ClassLoader classLoader = jarResolver.getClassLoaderWithRequiredJarFilesForExecution();
            // First initialize the module
            invokeMethod(classLoader, MODULE_INIT_CLASS_NAME, MODULE_INIT_METHOD_NAME);
            // Then call main method
            Object failError = invokeMethod(classLoader, getMethodClassName(), MODULE_RUN_METHOD_NAME);
            if (failError != null) {
                errorStream.println("Fail: " + failError);
            }
            return true;
        } catch (InvokerPanicException panicError) {
            Throwable panicCause = panicError.getCause();
            errorStream.println("Panic: " + panicCause.getMessage());
            addDiagnostic(Diagnostic.error("Unhandled Runtime Error."));
            throw panicError;
        }
    }

    /**
     * Invokes a method that is in the given class.
     * The method must be a static method accepting only one parameter, a {@link Strand}.
     *
     * @param classLoader Class loader to find the class.
     * @param className   Class name with the method.
     * @param methodName  Method name to invoke.
     * @throws InvokerException If invocation failed.
     */
    protected Object invokeMethod(ClassLoader classLoader, String className, String methodName)
            throws InvokerException {
        try {
            // Get class and method references
            Class<?> clazz = classLoader.loadClass(className);
            Method method = clazz.getDeclaredMethod(methodName, Strand.class);
            Function<Object[], Object> methodInvocation = createInvokerCallback(method);

            // Schedule and run the function and return if result is valid
            BFuture out = scheduler.schedule(new Object[1], methodInvocation, null, null, null,
                    PredefinedTypes.TYPE_ANY, null, null);
            scheduler.start();

            Object result = out.getResult();
            Throwable panic = out.getPanic();
            if (panic != null) {
                // Unexpected runtime error
                throw new InvokerPanicException(panic);
            }
            if (result instanceof Throwable) {
                // Function returned error (panic)
                throw new InvokerPanicException((Throwable) result);
            }
            return result;
        } catch (ClassNotFoundException e) {
            addDiagnostic(Diagnostic.error(className + " class not found: " + e.getMessage()));
            throw new InvokerException(e);
        } catch (NoSuchMethodException e) {
            addDiagnostic(Diagnostic.error(methodName + " method not found: " + e.getMessage()));
            throw new InvokerException(e);
        } catch (RuntimeException e) {
            addDiagnostic(Diagnostic.error("Unexpected error: " + e.getMessage()));
            throw new InvokerException(e);
        }
    }

    /**
     * Creates a callback to the method to directly call it with given params.
     *
     * @param method Method to create invocation.
     * @return Created callback.
     */
    private Function<Object[], Object> createInvokerCallback(Method method) {
        return (params) -> {
            try {
                return method.invoke(null, params);
            } catch (InvocationTargetException e) {
                return e.getTargetException();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error while invoking function.", e);
            }
        };
    }

    /**
     * Tries to import using the given statement.
     *
     * @param importStatement Import statement to use.
     * @return Whether import is valid.
     * @throws InvokerException If import file writing failed.
     */
    private boolean isImportStatementValid(String importStatement) throws InvokerException {
        ClassLoadContext importCheckingContext = createImportInferContext(importStatement);
        SingleFileProject project = getProject(importCheckingContext, IMPORT_TEMPLATE_FILE);
        PackageCompilation compilation = project.currentPackage().getCompilation();

        // Detect if import is valid.
        for (io.ballerina.tools.diagnostics.Diagnostic diagnostic : compilation.diagnosticResult().diagnostics()) {
            if (diagnostic.diagnosticInfo().code().equals(MODULE_NOT_FOUND_CODE)) {
                addDiagnostic(Diagnostic.error("Import resolution failed. Module not found."));
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the symbols that are visible globally.
     * Returns only function symbols and variable symbols.
     *
     * @param project     Project to get symbols.
     * @param compilation Compilation object.
     * @return All the visible symbols.
     */
    protected Collection<GlobalVariableSymbol> globalVariableSymbols(Project project, PackageCompilation compilation) {
        // Get the document associated with project
        ModuleId moduleId = project.currentPackage().getDefaultModule().moduleId();
        return compilation.getSemanticModel(moduleId).moduleSymbols().stream()
                .filter(s -> s instanceof VariableSymbol || s instanceof FunctionSymbol)
                .map(GlobalVariableSymbol::fromSymbol)
                .collect(Collectors.toList());
    }

    /**
     * Return import strings used by this snippet.
     *
     * @param snippet Snippet to check.
     * @return List of imports.
     */
    protected Set<String> getRequiredImportStatements(Snippet snippet) {
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
    protected Set<String> getRequiredImportStatements() {
        Set<String> importStrings = new HashSet<>(imports.getUsedImports()); // Anon imports
        importStrings.addAll(imports.getUsedImports(globalVars.keySet())); // Imports from vars
        importStrings.addAll(imports.getUsedImports(moduleDclns.keySet())); // Imports from module dclns
        return importStrings;
    }

    // Available statements

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
            String value = StringUtils.shortenedString(obj);
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
