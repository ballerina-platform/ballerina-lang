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
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.internal.BalRuntime;
import io.ballerina.runtime.internal.scheduling.ClassloaderRuntime;
import io.ballerina.shell.DiagnosticReporter;
import io.ballerina.shell.exceptions.InvokerException;
import io.ballerina.shell.exceptions.InvokerPanicException;
import io.ballerina.shell.invoker.classload.context.ClassLoadContext;
import io.ballerina.shell.snippet.Snippet;
import io.ballerina.shell.utils.StringUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    /* Constants related to execution */
    protected static final String MODULE_RUN_METHOD_NAME = "__run";

    protected static final String MODULE_STATEMENT_METHOD_NAME = "__stmt";
    private static final String MODULE_INIT_CLASS_NAME = "$_init";
    private static final String CONFIGURE_INIT_CLASS_NAME = "$configurationMapper";
    private static final String MODULE_INIT_METHOD_NAME = "$moduleInit";
    private static final String MODULE_START_METHOD_NAME = "$moduleStart";
    private static final String CONFIGURE_INIT_METHOD_NAME = "$configureInit";
    /* Constants related to temp files */
    private static final String TEMP_FILE_PREFIX = "main-";
    private static final String TEMP_FILE_SUFFIX = ".bal";
    /* Error type codes */
    private static final String MODULE_NOT_FOUND_CODE = "BCE2003";
    /**
     * File object that is used to create projects and write.
     * Depending on USE_TEMP_FILE flag, this may be either a file in cwd
     * or a temp file.
     */
    private File bufferFile;

    protected ShellSnippetsInvoker() {

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
     * Executes snippets and returns the compilation.
     * Snippets parameter should only include newly added snippets.
     * Old snippets should be managed as necessary by the implementation.
     *
     * @param newSnippets New snippets to execute.
     * @return compilation.
     */
    public abstract PackageCompilation getCompilation(Collection<Snippet> newSnippets) throws InvokerException;

    /**
     * Executes snippets and returns the output lines.
     *
     * @param compilation compilation.
     * @return Execution output result.
     */
    public abstract Optional<Object> execute(Optional<PackageCompilation> compilation) throws InvokerException;

    /**
     * Deletes a collection of names from the evaluator state.
     * If any of the names did not exist, this will throw an error.
     * A compilation will be done to make sure that no new errors are there.
     */
    public abstract void delete(Set<String> declarationNames) throws InvokerException;

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
     * Returns available variables in the module.
     *
     * @return Available variables as a list of AvailableVariable objects
     * with name, type and value.
     */
    public abstract List<AvailableVariable> availableVariablesAsObjects();

    /**
     * Returns available declarations in the module.
     *
     * @return Available declarations as a list of string.
     */
    public abstract List<String> availableModuleDeclarations();

    /**
     * Clears out the module declarations and variables definitions
     * from the previous execution.
     * Whether last invocation was successful or not this method needs to be
     * called as for a new execution because stored values for newDefinedVariableNames
     * and newModuleDeclnNames both are for the previous execution
     */
    public abstract void clearPreviousVariablesAndModuleDclnsNames();

    /**
     * Returns new variables in the module.
     *
     * @return New variables defined by the last executed code
     * snippet as a list of string.
     */
    public abstract List<String> newVariableNames();

    /**
     * Returns new declarations in the module.
     *
     * @return new declarations declared by the last executed code
     * snippet as a list of string.
     */
    public abstract List<String> newModuleDeclarations();

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
    protected Project getProject(Object context, String templateFile) throws InvokerException {
        Mustache template = getTemplate(templateFile);
        try (StringWriter stringWriter = new StringWriter()) {
            template.execute(stringWriter, context);
            return getProject(stringWriter.toString(), true);
        } catch (IOException e) {
            addErrorDiagnostic("File generation failed: " + e.getMessage());
            throw new InvokerException(e);
        }
    }

    /**
     * Get the project with the context data.
     *
     * @param source    Source to use for generating project.
     * @param isOffline Whether to use offline flag for build options.
     * @return Created ballerina project.
     * @throws InvokerException If file writing failed.
     */
    protected Project getProject(String source, boolean isOffline) throws InvokerException {
        try {
            File mainBal = writeToFile(source);
            BuildOptions buildOptions = BuildOptions.builder()
                    .setOffline(isOffline)
                    .targetDir(ProjectUtils.getTemporaryTargetPath())
                    .build();
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
        boolean containErrors = false;
        try {
            Module module = project.currentPackage().getDefaultModule();
            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            DiagnosticResult diagnosticResult = packageCompilation.diagnosticResult();

            for (Diagnostic diagnostic : diagnosticResult.diagnostics()) {
                DiagnosticSeverity severity = diagnostic.diagnosticInfo().severity();
                if (severity == DiagnosticSeverity.ERROR) {
                    containErrors = true;
                    addErrorDiagnostic(highlightedDiagnostic(module, diagnostic));
                } else if (severity == DiagnosticSeverity.WARNING) {
                    addWarnDiagnostic(highlightedDiagnostic(module, diagnostic));
                } else {
                    addDebugDiagnostic(diagnostic.message());
                }
            }

            if (containErrors) {
                addErrorDiagnostic("Compilation aborted due to errors.");
                throw new InvokerException();
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
        // First try to compile import offline.
        // If the module is not found, then try to change to online and compile.
        PackageCompilation offlineCompilation = getProject(importStatement, true)
                .currentPackage().getCompilation();

        if (containsModuleNotFoundError(offlineCompilation)) {
            PackageCompilation onlineCompilation = getProject(importStatement, false)
                    .currentPackage().getCompilation();
            if (containsModuleNotFoundError(onlineCompilation)) {
                addErrorDiagnostic("Import resolution failed. Module not found.");
                throw new InvokerException();
            }
        }
    }

    /**
     * @return Whether the compilation contains MODULE_NOT_FOUND error.
     */
    private boolean containsModuleNotFoundError(PackageCompilation compilation) {
        for (Diagnostic diagnostic : compilation.diagnosticResult().diagnostics()) {
            if (diagnostic.diagnosticInfo().code().equals(MODULE_NOT_FOUND_CODE)) {
                return true;
            }
        }
        return false;
    }

    /* Execution methods */

    /**
     * Executes a context in given template.
     *
     * @param context      Context to use.
     * @param templateName Template to evaluate.
     * @throws InvokerException If execution/compilation failed.
     */
    protected void executeProject(ClassLoadContext context, String templateName) throws InvokerException {
        Project project = getProject(context, templateName);
        PackageCompilation compilation = compile(project);
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_21);
        Package pkg = project.currentPackage();
        io.ballerina.runtime.api.Module module = new io.ballerina.runtime.api.Module(pkg
                .packageOrg().value(), pkg.packageName().value(), pkg.packageVersion().toString());
        executeProject(jBallerinaBackend, module);
    }

    /**
     * Executes a compiled project.
     * It is expected that the project had no compiler errors.
     * The process is run and the stdout is collected and printed.
     *
     * @param jBallerinaBackend Backed to use.
     * @param module           Module to execute.
     */
    protected void executeProject(JBallerinaBackend jBallerinaBackend, io.ballerina.runtime.api.Module module)
            throws InvokerPanicException {
        if (bufferFile == null) {
            throw new UnsupportedOperationException("Buffer file must be set before execution");
        }

        PrintStream errorStream = getErrorStream();
        try {
            JarResolver jarResolver = jBallerinaBackend.jarResolver();
            ClassLoader classLoader = jarResolver.getClassLoaderWithRequiredJarFilesForExecution();
            Object failErrorMessage = this.callRun(classLoader, module);
            if (failErrorMessage != null) {
                errorStream.println("fail: " + failErrorMessage);
            }
        } catch (Throwable panicError) {
            List<String> stacktrace = Arrays.stream(panicError.getCause().getStackTrace())
                    .filter(element -> !(element.toString().contains(MODULE_STATEMENT_METHOD_NAME) ||
                                        element.toString().contains(MODULE_RUN_METHOD_NAME)))
                    .toList()
                    .stream().map(element -> "at " + element.getMethodName() + "()").toList();
            errorStream.println("panic: " + StringUtils.getErrorStringValue(panicError.getCause()));
            stacktrace.forEach(errorStream::println);
            addErrorDiagnostic("Execution aborted due to unhandled runtime error.");
            throw panicError;
        }
    }

    /* Invocation methods */

    /**
     * Invokes a run function.
     *
     * @param classLoader Class loader to find the class.
     */
    protected Object callRun(ClassLoader classLoader, io.ballerina.runtime.api.Module module)
            throws InvokerPanicException {
        BalRuntime runtime = new ClassloaderRuntime(module, classLoader);
        Object result;
        try {
            // Initialize the module
            result = runtime.init();
            if (result instanceof Throwable throwable) {
                throw new InvokerPanicException(throwable);
            }
            // Start the module
            result = runtime.start();
            if (result instanceof Throwable throwable) {
                throw new InvokerPanicException(throwable);
            }
            // Then call run method
            result = runtime.callFunction(module, MODULE_RUN_METHOD_NAME, null);
            if (result instanceof Throwable throwable) {
                throw new InvokerPanicException(throwable);
            }
        } catch (Throwable throwable) {
            Throwable cause = throwable.getCause();
            if (cause instanceof BError bError) {
                throw new InvokerPanicException(bError);
            }
            throw new InvokerPanicException(throwable);
        } finally {
            try {
                runtime.stop();
            } catch (BError ignored) {
                // stop errors are ignored
            }
        }
        return result;
    }

    /* Util methods */

    /**
     * Highlight and show the error position.
     *
     * @param module     Module object. This should be a single document module.
     * @param diagnostic Diagnostic to show.
     * @return The string with position highlighted.
     */
    private String highlightedDiagnostic(Module module, Diagnostic diagnostic) {
        Optional<DocumentId> documentId = module.documentIds().stream().findFirst();
        Document document = module.document(documentId.orElseThrow());
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
    public File getBufferFile() throws IOException {
        if (this.bufferFile == null) {
            this.bufferFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
            addDebugDiagnostic("Using temp file: " + bufferFile.getAbsolutePath());
            this.bufferFile.deleteOnExit();
        }
        return this.bufferFile;
    }

    /**
     * @return Error stream to print out error messages.
     */
    protected PrintStream getErrorStream() {
        return System.err;
    }
}
