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

package io.ballerina.shell.invoker;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.Module;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.shell.Diagnostic;
import io.ballerina.shell.DiagnosticReporter;
import io.ballerina.shell.exceptions.InvokerException;
import io.ballerina.shell.snippet.Snippet;
import io.ballerina.shell.utils.StringUtils;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;


/**
 * Invoker that invokes a command to evaluate a list of snippets.
 * <p>
 * State of an invoker persists all the information required.
 * {@code reset} function will clear the invoker state.
 * <p>
 * Context of an invoker is the context that will be used to
 * fill the template. This should be a logic-less as much as possible.
 * Invoker and its context may be tightly coupled.
 *
 * @since 2.0.0
 */
public abstract class ShellSnippetsInvoker extends DiagnosticReporter {
    public static final String MODULE_NOT_FOUND_CODE = "BCE2003";
    // TODO: (#28662) After configurables can be supported, change this to that file location
    private static final Path CONFIG_PATH = Paths.get(System.getProperty("user.dir"), "Config.toml");
    /* Constants related to execution */
    private static final String MODULE_INIT_CLASS_NAME = "$_init";
    private static final String CONFIGURE_INIT_CLASS_NAME = "$ConfigurationMapper";
    private static final String MODULE_INIT_METHOD_NAME = "$moduleInit";
    private static final String CONFIGURE_INIT_METHOD_NAME = "$configureInit";
    private static final String MODULE_MAIN_METHOD_NAME = "main";
    /* Constants related to temp files */
    private static final String TEMP_FILE_PREFIX = "main-";
    private static final String TEMP_FILE_SUFFIX = ".bal";
    private static final String NON_ACCESSIBLE_TYPE_CODE = "BCE2037";

    /**
     * Scheduler used to run the init and main methods
     * of the generated classes.
     */
    private final Scheduler scheduler;
    /**
     * File object that is used to create projects and write.
     * Depending on USE_TEMP_FILE flag, this may be either a file in cwd
     * or a temp file.
     */
    private File bufferFile;

    protected ShellSnippetsInvoker() {
        this.scheduler = new Scheduler(false);
    }

    /**
     * Initializes the invoker. This can be used to load required files
     * and create caches. Calling this is not a requirement.
     * <p>
     * Runs so that a demo file is loaded and compiled
     * so the required caches will be ready once the user gives input.
     * Any error is an indication of a failure in template of base compilation.
     * Throw if that happens.
     *
     * @throws InvokerException If initialization failed.
     */
    public abstract void initialize() throws InvokerException;

    /**
     * Reset executor state so that the execution can be start over.
     */
    public abstract void reset();

    /**
     * Executes a snippet and returns the output lines.
     * Snippets parameter should only include newly added snippets.
     * Old snippets should be managed as necessary by the implementation.
     *
     * @param newSnippet New snippet to execute.
     * @return Execution output result.
     */
    public abstract Optional<Object> execute(Snippet newSnippet) throws InvokerException;

    /**
     * Returns available imports in the module.
     *
     * @return Available imports as a list of string.
     */
    public abstract List<String> availableImports();

    /**
     * Returns available variables in the module.
     *
     * @return Available variables as a list of string.
     */
    public abstract List<String> availableVariables();

    /**
     * Returns available declarations in the module.
     *
     * @return Available declarations as a list of string.
     */
    public abstract List<String> availableModuleDeclarations();

    /* Template methods */

    /**
     * Helper method that creates the template reference.
     *
     * @param templateName Name of the template.
     * @return Created template
     */
    protected Mustache getTemplate(String templateName) {
        MustacheFactory mf = new DefaultMustacheFactory();
        return mf.compile(templateName);
    }

    /* Project creation methods */

    /**
     * Get the project with the context data.
     *
     * @param context      Context to create the ballerina file.
     * @param templateFile Template file to load.
     * @return Created ballerina project.
     * @throws InvokerException If file writing failed.
     */
    protected SingleFileProject getProject(Object context, String templateFile) throws InvokerException {
        Mustache template = getTemplate(templateFile);
        try (StringWriter stringWriter = new StringWriter()) {
            template.execute(stringWriter, context);
            return getProject(stringWriter.toString());
        } catch (IOException e) {
            addErrorDiagnostic("File generation failed: " + e.getMessage());
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
            addErrorDiagnostic("File writing failed: " + e.getMessage());
            throw new InvokerException(e);
        }
    }

    /* Compilation methods */

    /**
     * Helper method to compile a project and report any errors.
     * No code generation is done.
     *
     * @param project Project to compile.
     * @return Compilation data.
     * @throws InvokerException If compilation failed.
     */
    protected PackageCompilation compile(Project project) throws InvokerException {
        try {
            Module module = project.currentPackage().getDefaultModule();
            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            DiagnosticResult diagnosticResult = packageCompilation.diagnosticResult();

            for (io.ballerina.tools.diagnostics.Diagnostic diagnostic : diagnosticResult.diagnostics()) {
                DiagnosticSeverity severity = diagnostic.diagnosticInfo().severity();
                if (severity == DiagnosticSeverity.ERROR) {
                    addErrorDiagnostic(highlightedDiagnostic(module, diagnostic));
                    addErrorDiagnostic("Compilation aborted due to errors.");
                    throw new InvokerException();
                } else if (severity == DiagnosticSeverity.WARNING) {
                    addDiagnostic(Diagnostic.warn(highlightedDiagnostic(module, diagnostic)));
                } else {
                    addDiagnostic(Diagnostic.debug(diagnostic.message()));
                }
            }

            return packageCompilation;
        } catch (InvokerException e) {
            throw e;
        } catch (Exception e) {
            addErrorDiagnostic("Something went wrong: " + e);
            throw new InvokerException(e);
        }
    }


    /**
     * Tries to import using the given statement.
     *
     * @param importStatement Import statement to use.
     * @throws InvokerException If import cannot be resolved.
     */
    protected void compileImportStatement(String importStatement) throws InvokerException {
        SingleFileProject project = getProject(importStatement);
        PackageCompilation compilation = project.currentPackage().getCompilation();

        // Detect if import is valid.
        for (io.ballerina.tools.diagnostics.Diagnostic diagnostic : compilation.diagnosticResult().diagnostics()) {
            if (diagnostic.diagnosticInfo().code().equals(MODULE_NOT_FOUND_CODE)) {
                addErrorDiagnostic("Import resolution failed. Module not found.");
                throw new InvokerException();
            }
        }
    }

    /* Execution methods */

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
        if (bufferFile == null) {
            throw new UnsupportedOperationException("Buffer file must be set before execution");
        }

        // Main method class name is file name without extension
        String fileName = bufferFile.getName();
        String mainMethodClassName = fileName.substring(0, fileName.length() - TEMP_FILE_SUFFIX.length());

        JarResolver jarResolver = jBallerinaBackend.jarResolver();
        ClassLoader classLoader = jarResolver.getClassLoaderWithRequiredJarFilesForExecution();
        // First run configure initialization
        invokeMethodDirectly(classLoader, CONFIGURE_INIT_CLASS_NAME, CONFIGURE_INIT_METHOD_NAME,
                new Class[]{Path.class}, new Object[]{CONFIG_PATH});
        // Initialize the module
        invokeScheduledMethod(classLoader, MODULE_INIT_CLASS_NAME, MODULE_INIT_METHOD_NAME);
        // Then call main method
        Object result = invokeScheduledMethod(classLoader, mainMethodClassName, MODULE_MAIN_METHOD_NAME);
        addDiagnostic(Diagnostic.debug("Result: " + result));
        return result == null;
    }

    /* Invocation methods */

    /**
     * Invokes a method that is in the given class.
     * The method must be a static method accepting only one parameter, a {@link Strand}.
     *
     * @param classLoader Class loader to find the class.
     * @param className   Class name with the method.
     * @param methodName  Method name to invoke.
     * @return The result of the invocation.
     * @throws InvokerException If invocation failed.
     */
    protected Object invokeScheduledMethod(ClassLoader classLoader, String className, String methodName)
            throws InvokerException {
        try {
            addDiagnostic(Diagnostic.debug(String.format("Running %s.%s on schedule", className, methodName)));

            // Get class and method references
            Class<?> clazz = classLoader.loadClass(className);
            Method method = clazz.getDeclaredMethod(methodName, Strand.class);
            Function<Object[], Object> methodInvocation = createInvokerCallback(method);

            // Schedule and run the function and return if result is valid
            BFuture out = scheduler.schedule(new Object[1], methodInvocation, null, null, null,
                    PredefinedTypes.TYPE_ERROR, null, null);
            scheduler.start();

            Object result = out.getResult();
            Throwable panic = out.getPanic();
            addDiagnostic(Diagnostic.debug("Panic: " + panic));
            addDiagnostic(Diagnostic.debug("Result: " + result));

            if (panic != null) {
                throw new InvokerException(panic);
            }
            return result;
        } catch (ClassNotFoundException e) {
            addErrorDiagnostic(className + " class not found: " + e.getMessage());
            throw new InvokerException(e);
        } catch (NoSuchMethodException e) {
            addErrorDiagnostic(methodName + " method not found: " + e.getMessage());
            throw new InvokerException(e);
        }
    }

    /**
     * Invokes a method that is in the given class.
     * This is directly invoked without scheduling.
     * The method must be a static method accepting the given parameters.
     *
     * @param classLoader Class loader to find the class.
     * @param className   Class name with the method.
     * @param methodName  Method name to invoke.
     * @param argTypes    Types of arguments.
     * @param args        Arguments to provide.
     * @return The result of the invocation.
     * @throws InvokerException If invocation failed.
     */
    protected Object invokeMethodDirectly(ClassLoader classLoader, String className, String methodName,
                                          Class<?>[] argTypes, Object[] args) throws InvokerException {
        try {
            // Get class and method references
            addDiagnostic(Diagnostic.debug(String.format("Running %s.%s directly", className, methodName)));
            Class<?> clazz = classLoader.loadClass(className);
            Method method = clazz.getDeclaredMethod(methodName, argTypes);
            Object result = method.invoke(null, args);
            addDiagnostic(Diagnostic.debug("Result: " + result));
            return result;
        } catch (ClassNotFoundException e) {
            addErrorDiagnostic(className + " class not found: " + e.getMessage());
            throw new InvokerException(e);
        } catch (NoSuchMethodException e) {
            addErrorDiagnostic(methodName + " method not found: " + e.getMessage());
            throw new InvokerException(e);
        } catch (IllegalAccessException e) {
            addErrorDiagnostic(methodName + " illegal access: " + e.getMessage());
            throw new InvokerException(e);
        } catch (InvocationTargetException e) {
            addErrorDiagnostic(methodName + " exception at target: " + e.getTargetException());
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

    /* Util methods */

    /**
     * Highlight and show the error position.
     *
     * @param module     Module object. This should be a single document module.
     * @param diagnostic Diagnostic to show.
     * @return The string with position highlighted.
     */
    private String highlightedDiagnostic(Module module, io.ballerina.tools.diagnostics.Diagnostic diagnostic) {
        // Get the source code
        Optional<DocumentId> documentId = module.documentIds().stream().findFirst();
        assert documentId.isPresent();
        Document document = module.document(documentId.get());
        if (diagnostic.diagnosticInfo().code().equals(NON_ACCESSIBLE_TYPE_CODE)) {
            return "Error: " + diagnostic.message() + "\n" +
                    "The initializer returns a non-accessible symbol. " +
                    "This is currently not supported in REPL. " +
                    "Please explicitly state the type.";
        }
        return StringUtils.highlightDiagnostic(document.textDocument(), diagnostic);
    }

    /**
     * Helper method to write a string source to a file.
     *
     * @param source Content to write to the file.
     * @return The created temp file.
     * @throws IOException If writing was unsuccessful.
     */
    protected File writeToFile(String source) throws IOException {
        File createdFile = getBufferFile();
        try (FileWriter fileWriter = new FileWriter(createdFile, Charset.defaultCharset())) {
            fileWriter.write(source);
        }
        return createdFile;
    }

    /**
     * Get the file that would be used as the buffer for loading project.
     *
     * @return File to use.
     * @throws IOException If file open failed.
     */
    private File getBufferFile() throws IOException {
        if (this.bufferFile == null) {
            this.bufferFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
            this.bufferFile.deleteOnExit();
        }
        return this.bufferFile;
    }
}
