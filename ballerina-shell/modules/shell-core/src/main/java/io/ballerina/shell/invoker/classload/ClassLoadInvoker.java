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

import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.shell.Diagnostic;
import io.ballerina.shell.exceptions.InvokerException;
import io.ballerina.shell.invoker.Invoker;
import io.ballerina.shell.invoker.classload.context.ClassLoadContext;
import io.ballerina.shell.invoker.classload.context.StatementContext;
import io.ballerina.shell.invoker.classload.context.VariableContext;
import io.ballerina.shell.invoker.classload.visitors.ElevatedTypeTransformer;
import io.ballerina.shell.invoker.classload.visitors.TypeSignatureTransformer;
import io.ballerina.shell.rt.InvokerMemory;
import io.ballerina.shell.snippet.Snippet;
import io.ballerina.shell.snippet.types.ExecutableSnippet;
import io.ballerina.shell.snippet.types.ImportDeclarationSnippet;
import io.ballerina.shell.snippet.types.ModuleMemberDeclarationSnippet;
import io.ballerina.shell.snippet.types.VariableDeclarationSnippet;
import io.ballerina.shell.utils.StringUtils;
import io.ballerina.shell.utils.timeit.InvokerTimeIt;
import io.ballerina.shell.utils.timeit.TimedOperation;
import io.ballerina.tools.text.LinePosition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
import java.util.stream.Collectors;

/**
 * Executes the snippet given.
 * This invoker will save all the variable values in a static class and
 * load them into the generated class effectively managing any side-effects.
 *
 * @since 2.0.0
 */
public class ClassLoadInvoker extends Invoker implements ImportProcessor {
    // Context related information
    public static final String CONTEXT_EXPR_VAR_NAME = "expr";
    // Main class and method names to invoke
    public static final String MODULE_NOT_FOUND_CODE = "BCE2003";
    protected static final String MODULE_INIT_CLASS_NAME = "$_init";
    protected static final String MODULE_MAIN_METHOD_NAME = "main";
    protected static final String DOLLAR = "$";
    // Punctuations
    private static final String DECLARATION_TEMPLATE_FILE = "template.declaration.ftl";
    private static final String IMPORT_TEMPLATE_FILE = "template.import.ftl";
    private static final String EXECUTION_TEMPLATE_FILE = "template.execution.ftl";

    private static final AtomicInteger importIndex = new AtomicInteger(0);

    /**
     * Set of symbols that are known or seen at this point.
     */
    protected final Set<HashedSymbol> knownSymbols;
    /**
     * List of imports done.
     * These are imported to the read generated code as necessary.
     */
    protected final HashedImports imports;
    /**
     * List of module level declarations such as functions, classes, etc...
     * The snippets are saved as is.
     */
    protected final Map<String, String> moduleDclns;
    /**
     * List of global variables used in the code.
     * This is a map of variable name to its type.
     * The variable name must be a quoted identifier.
     */
    protected final Set<GlobalVariable> globalVars;
    /**
     * Flag to keep track of whether the invoker is initialized.
     */
    protected final AtomicBoolean initialized;
    /**
     * Id of the current invoker context.
     */
    protected final String contextId;

    /**
     * Stores all the newly found symbols in this iteration.
     * This is reset in each iteration and is persisted to `knownSymbols` at the end
     * of the current iteration. (If it were a success)
     */
    private final Set<HashedSymbol> newSymbols;
    /**
     * Stores all the newly found implicit imports.
     * Persisted at the end of iteration to `mustImportPrefixes`.
     */
    private final Set<String> newImplicitImports;

    /**
     * Creates a class load invoker from the given ballerina home.
     * Ballerina home should be tha path that contains repo directory.
     * It is expected that the runtime is added in the class path.
     */
    public ClassLoadInvoker() {
        this.initialized = new AtomicBoolean(false);
        this.contextId = UUID.randomUUID().toString();
        this.moduleDclns = new HashMap<>();
        this.globalVars = new HashSet<>();
        this.newSymbols = new HashSet<>();
        this.newImplicitImports = new HashSet<>();
        this.knownSymbols = new HashSet<>();
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
        ClassLoadContext emptyContext = new ClassLoadContext(contextId, imports.getImplicitImports());
        SingleFileProject project = getProject(emptyContext, DECLARATION_TEMPLATE_FILE);
        PackageCompilation compilation = compile(project);
        Collection<Symbol> symbols = visibleUnknownSymbols(project, compilation);
        symbols.stream().map(HashedSymbol::new).forEach(knownSymbols::add);
        JBallerinaBackend.from(compile(project), JvmTarget.JAVA_11);
        this.initialized.set(true);
    }

    @Override
    public void reset() {
        // Clear everything in memory
        // data wrt the memory context is also removed.
        this.moduleDclns.clear();
        this.globalVars.clear();
        InvokerMemory.forgetAll(contextId);
        this.knownSymbols.clear();
        this.initialized.set(false);
        this.imports.reset();
    }

    @Override
    public Optional<Object> execute(Snippet newSnippet) throws InvokerException {
        if (!this.initialized.get()) {
            throw new IllegalStateException("Invoker execution not initialized.");
        }

        newSymbols.clear();
        newImplicitImports.clear();

        // TODO: Fix the closure bug. Following will not work with isolated functions.
        // newSnippet.modify(new GlobalLoadModifier(globalVars));

        switch (newSnippet.getKind()) {
            case IMPORT_DECLARATION:
                // Only compilation to find import validity and exit.
                assert newSnippet instanceof ImportDeclarationSnippet;
                ImportDeclarationSnippet importDcln = (ImportDeclarationSnippet) newSnippet;
                String importPrefix = timedOperation("processing import", () -> processImport(importDcln));
                Objects.requireNonNull(importPrefix, "Import prefix identification failed.");
                addDiagnostic(Diagnostic.debug("Import prefix identified as: " + importPrefix));
                return Optional.empty();

            case MODULE_MEMBER_DECLARATION:
                // Only compilation to find dcln validity and exit.
                assert newSnippet instanceof ModuleMemberDeclarationSnippet;
                ModuleMemberDeclarationSnippet moduleDcln = (ModuleMemberDeclarationSnippet) newSnippet;
                Map.Entry<String, String> newModuleDcln = timedOperation("processing module dcln",
                        () -> processModuleDcln(moduleDcln));
                this.knownSymbols.addAll(this.newSymbols);
                this.newImplicitImports.forEach(imports::storeImplicitPrefix);
                this.moduleDclns.put(newModuleDcln.getKey(), newModuleDcln.getValue());
                addDiagnostic(Diagnostic.debug("Module dcln name: " + newModuleDcln.getKey()));
                addDiagnostic(Diagnostic.debug("Module dcln code: " + newModuleDcln.getValue()));
                addDiagnostic(Diagnostic.debug("Found new symbols: " + this.newSymbols));
                addDiagnostic(Diagnostic.debug("Implicit imports added: " + this.newImplicitImports));
                return Optional.empty();

            case VARIABLE_DECLARATION:
            case STATEMENT:
            case EXPRESSION:
                assert newSnippet instanceof ExecutableSnippet;
                Set<GlobalVariable> newVariables = new HashSet<>();
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
                        () -> executeProject(project, jBallerinaBackend));

                if (!isExecutionSuccessful) {
                    addDiagnostic(Diagnostic.error("Unhandled Runtime Error."));
                    throw new InvokerException();
                }

                // Save required data if execution was successful
                Object executionResult = InvokerMemory.recall(contextId, CONTEXT_EXPR_VAR_NAME);
                this.knownSymbols.addAll(this.newSymbols);
                this.newImplicitImports.forEach(imports::storeImplicitPrefix);
                if (newSnippet.isVariableDeclaration()) {
                    globalVars.addAll(newVariables);
                }
                addDiagnostic(Diagnostic.debug("Found new variables: " + newVariables));
                addDiagnostic(Diagnostic.debug("Found new symbols: " + this.newSymbols));
                addDiagnostic(Diagnostic.debug("Implicit imports added: " + this.newImplicitImports));
                return Optional.ofNullable(executionResult);

            default:
                addDiagnostic(Diagnostic.error("Unexpected snippet type."));
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public String processImplicitImport(String moduleName, String defaultPrefix) throws InvokerException {
        if (imports.moduleImported(moduleName)) {
            // If this module is already imported, use a previous prefix.
            return imports.prefix(moduleName);
        }

        // Try to find an available prefix
        String quotedPrefix = StringUtils.quoted(defaultPrefix);
        while (imports.containsPrefix(quotedPrefix)) {
            quotedPrefix = StringUtils.quoted("prefix" + importIndex.incrementAndGet());
        }

        // Check if import is successful.
        if (isImportStatementValid(String.format("import %s as %s;", moduleName, quotedPrefix))) {
            return imports.storeImport(quotedPrefix, moduleName);
        }
        return null;
    }

    /**
     * This is an import. A test import is done to check for errors.
     * It should not give 'module not found' error.
     * Only compilation is done to verify package resolution.
     *
     * @param importSnippet New import snippet string.
     * @return Whether import is a valid import.
     * @throws InvokerException If importing failed.
     */
    public String processImport(ImportDeclarationSnippet importSnippet) throws InvokerException {
        String moduleName = importSnippet.getImportedModule();
        String quotedPrefix = StringUtils.quoted(importSnippet.getPrefix());

        if (imports.moduleImported(moduleName) && imports.prefix(moduleName).equals(quotedPrefix)) {
            // Same module with same prefix. No need to check.
            // TODO: If identifier validity can be checked, no need to even import the statement.
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
    private Set<GlobalVariable> processVarDcln(VariableDeclarationSnippet newSnippet) throws InvokerException {
        // No matter the approach, compile. This will confirm that syntax is valid.
        ClassLoadContext varTypeInferContext = createVarTypeInferContext(newSnippet);
        SingleFileProject project = getProject(varTypeInferContext, DECLARATION_TEMPLATE_FILE);
        Collection<Symbol> symbols = visibleUnknownSymbols(project);

        Set<GlobalVariable> foundVariables = new HashSet<>();
        for (Symbol symbol : symbols) {
            HashedSymbol hashedSymbol = new HashedSymbol(symbol);
            // TODO: After name alternative is implemented use it.
            String variableName = symbol.name();

            boolean ignoreSymbol = knownSymbols.contains(hashedSymbol)
                    || GlobalVariable.isDefined(foundVariables, variableName)
                    || variableName.contains(DOLLAR);
            boolean acceptableSymbol = symbol instanceof VariableSymbol
                    || symbol instanceof FunctionSymbol;

            // Identify variable type
            if (!ignoreSymbol && acceptableSymbol) {
                TypeSymbol typeSymbol = (symbol instanceof VariableSymbol)
                        ? ((VariableSymbol) symbol).typeDescriptor()
                        : ((FunctionSymbol) symbol).typeDescriptor();

                ElevatedTypeTransformer elevatedTypeTransformer = new ElevatedTypeTransformer();
                ElevatedType elevatedType = elevatedTypeTransformer.transformType(typeSymbol);

                TypeSignatureTransformer signatureTransformer = new TypeSignatureTransformer(this);
                String variableType = signatureTransformer.transformType(typeSymbol);
                this.newImplicitImports.addAll(signatureTransformer.getImplicitImportPrefixes());

                foundVariables.add(new GlobalVariable(variableType, variableName, elevatedType));
                this.newSymbols.add(hashedSymbol);
            }
        }

        return foundVariables;
    }

    /**
     * Processes a variable declaration snippet.
     * Symbols are processed to know the new module dcln.
     * Only compilation is done.
     * TODO: Support enums.
     *
     * @param newSnippet New snippet to process.
     * @return The newly found type name and its declaration.
     * @throws InvokerException If module dcln is invalid.
     */
    private Map.Entry<String, String> processModuleDcln(ModuleMemberDeclarationSnippet newSnippet)
            throws InvokerException {
        // Add all required imports
        this.newImplicitImports.addAll(newSnippet.usedImports());

        ClassLoadContext varTypeInferContext = createModuleDclnNameInferContext(newSnippet);
        SingleFileProject project = getProject(varTypeInferContext, DECLARATION_TEMPLATE_FILE);
        Collection<Symbol> symbols = visibleUnknownSymbols(project);

        Optional<String> enumName = newSnippet.enumName();
        if (enumName.isPresent()) {
            // Enum. Has to process as such. The code will be as is.
            // Enums cannot be processed as normal because they are desugared in compilation.
            // All new symbols are added as known.
            symbols.stream().map(HashedSymbol::new).forEach(this.newSymbols::add);
            return Map.entry(enumName.get(), newSnippet.toString());
        } else {
            for (Symbol symbol : symbols) {
                if (!symbol.kind().equals(SymbolKind.MODULE)) {
                    this.newSymbols.add(new HashedSymbol(symbol));
                    // TODO: After name alternative is implemented use it.
                    return Map.entry(symbol.name(), newSnippet.toString());
                }
            }
        }

        addDiagnostic(Diagnostic.error("Invalid module level declaration: cannot be compiled."));
        throw new InvokerException();
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
     * @param newSnippet New snippet. Must be a var dcln.
     * @return Context with type information inferring code.
     */
    protected ClassLoadContext createVarTypeInferContext(VariableDeclarationSnippet newSnippet) {
        List<VariableContext> varDclns = globalVariableContexts();
        List<String> moduleDclnStrings = new ArrayList<>(moduleDclns.values());

        // Imports = snippet imports + module def imports
        Set<String> importStrings = getUsedImportStatements(newSnippet);
        importStrings.addAll(imports.getImplicitImports());
        String lastVarDcln = newSnippet.toString();

        return new ClassLoadContext(this.contextId, importStrings, moduleDclnStrings, varDclns, lastVarDcln);
    }

    /**
     * Creates a context which can be used to find declaration name.
     *
     * @param newSnippet New snippet. Must be a module member dcln.
     * @return Context to infer dcln name.
     */
    protected ClassLoadContext createModuleDclnNameInferContext(ModuleMemberDeclarationSnippet newSnippet) {
        List<VariableContext> varDclns = globalVariableContexts();
        List<String> moduleDclnStrings = new ArrayList<>(moduleDclns.values());
        moduleDclnStrings.add(newSnippet.toString());

        // Get all required imports
        Set<String> importStrings = getUsedImportStatements(newSnippet);
        importStrings.addAll(imports.getImplicitImports());

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
                                                      Set<GlobalVariable> newVariables) {
        List<VariableContext> variableDeclarations = globalVariableContexts();
        Set<String> importStrings = getUsedImportStatements(newSnippet);
        importStrings.addAll(imports.getImplicitImports());

        if (newSnippet.isVariableDeclaration()) {
            newVariables.stream().map(VariableContext::newVar)
                    .forEach(variableDeclarations::add);
            return new ClassLoadContext(this.contextId, importStrings, moduleDclns.values(),
                    variableDeclarations, newSnippet.toString(), null);
        } else {
            StatementContext lastStatement = new StatementContext(newSnippet);
            return new ClassLoadContext(this.contextId, importStrings, moduleDclns.values(),
                    variableDeclarations, null, lastStatement);
        }
    }

    /**
     * Global variables as required by contexts.
     *
     * @return Global variable declarations list.
     */
    private List<VariableContext> globalVariableContexts() {
        List<VariableContext> varDclns = new ArrayList<>();
        globalVars.stream().map(VariableContext::oldVar).forEach(varDclns::add);
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
        Template template = super.getTemplate(templateFile);
        try (StringWriter stringWriter = new StringWriter()) {
            template.process(context, stringWriter);
            return getProject(stringWriter.toString());
        } catch (TemplateException e) {
            addDiagnostic(Diagnostic.error("Template processing failed: " + e.getMessage()));
            throw new InvokerException(e);
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
     * Due to ballerina calling system.exit(), we need to disable these calls and
     * remove system error logs as well.
     *
     * @param project           Project to run.
     * @param jBallerinaBackend Backed to use.
     * @return Whether process execution was successful.
     * @throws InvokerException If execution failed.
     */
    protected boolean executeProject(Project project, JBallerinaBackend jBallerinaBackend) throws InvokerException {
        try {
            Module executableModule = project.currentPackage().getDefaultModule();
            JarResolver jarResolver = jBallerinaBackend.jarResolver();
            ClassLoader classLoader = jarResolver.getClassLoaderWithRequiredJarFilesForExecution();

            String initClassName = JarResolver.getQualifiedClassName(
                    executableModule.packageInstance().packageOrg().toString(),
                    executableModule.packageInstance().packageName().toString(),
                    executableModule.packageInstance().packageVersion().toString(),
                    MODULE_INIT_CLASS_NAME);
            Class<?> clazz = classLoader.loadClass(initClassName);

            Method method = clazz.getDeclaredMethod(MODULE_MAIN_METHOD_NAME, String[].class);
            int exitCode = invokeMethod(method);
            addDiagnostic(Diagnostic.debug("Exit code was " + exitCode));
            return exitCode == 0;
        } catch (ClassNotFoundException e) {
            addDiagnostic(Diagnostic.error("Main class not found: " + e.getMessage()));
            throw new InvokerException(e);
        } catch (NoSuchMethodException e) {
            addDiagnostic(Diagnostic.error("Main method not found: " + e.getMessage()));
            throw new InvokerException(e);
        } catch (IllegalAccessException e) {
            addDiagnostic(Diagnostic.error("Access for the method failed: " + e.getMessage()));
            throw new InvokerException(e);
        }
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
     * Gets the symbols that are visible to main method but are unknown (previously not seen).
     * Compilation is also done.
     *
     * @param project Project to get symbols.
     * @return All the visible symbols.
     */
    protected Collection<Symbol> visibleUnknownSymbols(Project project) throws InvokerException {
        PackageCompilation compilation = compile(project);
        return visibleUnknownSymbols(project, compilation);
    }

    /**
     * Gets the symbols that are visible to main method but are unknown (previously not seen).
     *
     * @param project     Project to get symbols.
     * @param compilation Compilation object.
     * @return All the visible symbols.
     */
    protected Collection<Symbol> visibleUnknownSymbols(Project project, PackageCompilation compilation) {
        // Get the document associated with project
        Module module = project.currentPackage().getDefaultModule();
        ModuleId moduleId = module.moduleId();
        Optional<DocumentId> documentId = module.documentIds().stream().findFirst();
        assert documentId.isPresent();
        Document document = module.document(documentId.get());

        // Find the position of cursor to find the symbols
        // Get the position of the main function, line start of the close brace (after anything in main body)
        ModulePartNode modulePartNode = document.syntaxTree().rootNode();
        NodeList<ModuleMemberDeclarationNode> declarationNodes = modulePartNode.members();
        ModuleMemberDeclarationNode declarationSnippet = declarationNodes.get(declarationNodes.size() - 1);
        assert declarationSnippet instanceof FunctionDefinitionNode;
        FunctionBodyNode bodyNode = ((FunctionDefinitionNode) declarationSnippet).functionBody();
        assert bodyNode instanceof FunctionBodyBlockNode;
        LinePosition cursorPos = ((FunctionBodyBlockNode) bodyNode).closeBraceToken().lineRange().startLine();

        return compilation.getSemanticModel(moduleId)
                .visibleSymbols(document, cursorPos).stream()
                .filter((s) -> !knownSymbols.contains(new HashedSymbol(s)))
                .collect(Collectors.toList());
    }

    /**
     * Return import strings used by this snippet.
     *
     * @param snippet Snippet to check.
     * @return List of imports.
     */
    protected Set<String> getUsedImportStatements(Snippet snippet) {
        Set<String> importStrings = new HashSet<>();
        snippet.usedImports().stream()
                .map(imports::getImport).filter(Objects::nonNull)
                .forEach(importStrings::add);
        // Process current snippet, module dclns and indirect imports
        return importStrings;
    }

    /**
     * Runs a method given. Returns the exit code from the execution.
     * Method should be a static method which returns an int.
     * Its signature should be, {@code static int name(String[] args)}.
     *
     * @param method Method to run (should be a static method).
     * @return Exit code of the method.
     * @throws IllegalAccessException If interrupted.
     */
    protected int invokeMethod(Method method) throws IllegalAccessException {
        String[] args = new String[0];

        // STDERR is completely ignored because Security Exceptions are thrown
        // So real errors will not be visible via STDERR.
        // Security manager is set to stop VM exits.

        PrintStream stdErr = System.err;
        NoExitVmSecManager secManager = new NoExitVmSecManager(System.getSecurityManager());
        try {
            System.setErr(new PrintStream(new ByteArrayOutputStream(), true, Charset.defaultCharset()));
            System.setSecurityManager(secManager);
            return (int) method.invoke(null, new Object[]{args});
        } catch (InvocationTargetException e) {
            return secManager.getExitCode();
        } finally {
            System.setSecurityManager(null);
            System.setErr(stdErr);
        }
    }

    // Available statements

    @Override
    public List<String> availableImports() {
        // Imports with prefixes
        List<String> importStrings = new ArrayList<>();
        for (String prefix : imports.prefixes()) {
            importStrings.add(String.format("(%s) %s", prefix, imports.getImport(prefix)));
        }
        return importStrings;
    }

    @Override
    public List<String> availableVariables() {
        // Available variables and values as string.
        List<String> varStrings = new ArrayList<>();
        for (GlobalVariable entry : globalVars) {
            Object obj = InvokerMemory.recall(contextId, entry.getVariableName());
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
        for (Map.Entry<String, String> entry : moduleDclns.entrySet()) {
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
