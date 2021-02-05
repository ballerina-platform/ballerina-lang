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

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.shell.Diagnostic;
import io.ballerina.shell.DiagnosticReporter;
import io.ballerina.shell.exceptions.InvokerException;
import io.ballerina.shell.snippet.Snippet;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

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
public abstract class Invoker extends DiagnosticReporter {
    private static final String NON_ACCESSIBLE_TYPE_CODE = "BCE2037";
    private static final boolean USE_TEMP_FILE = true;

    /**
     * File object that is used to create projects and write.
     * Depending on USE_TEMP_FILE flag, this may be either a file in cwd
     * or a temp file.
     */
    private File bufferFile;

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

    /**
     * Helper method that creates the template reference.
     *
     * @param templateName Name of the template.
     * @return Created template
     * @throws InvokerException If reading template failed.
     */
    protected Template getTemplate(String templateName) throws InvokerException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
        cfg.setClassForTemplateLoading(getClass(), "/");
        cfg.setDefaultEncoding("UTF-8");
        try {
            return cfg.getTemplate(templateName);
        } catch (IOException e) {
            addDiagnostic(Diagnostic.error("Template file read failed: " + e.getMessage()));
            throw new InvokerException(e);
        }
    }

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
                    addDiagnostic(Diagnostic.error(highlightedDiagnostic(module, diagnostic)));
                    addDiagnostic(Diagnostic.error("Compilation aborted because of errors."));
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
            addDiagnostic(Diagnostic.error("Something went wrong: " + e));
            throw new InvokerException(e);
        } catch (Error e) {
            addDiagnostic(Diagnostic.error("Something severely went wrong: " + e));
            throw new InvokerException(e);
        }
    }

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
        return Diagnostic.highlightDiagnostic(document.textDocument(), diagnostic);
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
            this.bufferFile = USE_TEMP_FILE
                    ? File.createTempFile("main-", ".bal")
                    : new File("main.bal");
            this.bufferFile.deleteOnExit();
            addDiagnostic(Diagnostic.debug("Ballerina source file used as buffer: " + this.bufferFile));
        }
        return this.bufferFile;
    }
}
