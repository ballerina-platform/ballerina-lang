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

package io.ballerina.shell.cli;

import io.ballerina.projects.PackageCompilation;
import io.ballerina.shell.Diagnostic;
import io.ballerina.shell.DiagnosticKind;
import io.ballerina.shell.Evaluator;
import io.ballerina.shell.ExceptionStatus;
import io.ballerina.shell.ShellCompilation;
import io.ballerina.shell.ShellReturnValue;
import io.ballerina.shell.cli.handlers.CommandHandler;
import io.ballerina.shell.cli.handlers.DeleteCommand;
import io.ballerina.shell.cli.handlers.ExitCommand;
import io.ballerina.shell.cli.handlers.FileCommand;
import io.ballerina.shell.cli.handlers.HelpCommand;
import io.ballerina.shell.cli.handlers.ResetStateCommand;
import io.ballerina.shell.cli.handlers.StringListCommand;
import io.ballerina.shell.cli.handlers.ToggleDebugCommand;
import io.ballerina.shell.cli.utils.FileUtils;
import io.ballerina.shell.exceptions.BallerinaShellException;
import io.ballerina.shell.utils.ModuleImporter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.ballerina.shell.cli.PropertiesLoader.COMMAND_DCLNS;
import static io.ballerina.shell.cli.PropertiesLoader.COMMAND_DEBUG;
import static io.ballerina.shell.cli.PropertiesLoader.COMMAND_DELETE;
import static io.ballerina.shell.cli.PropertiesLoader.COMMAND_EXIT;
import static io.ballerina.shell.cli.PropertiesLoader.COMMAND_FILE;
import static io.ballerina.shell.cli.PropertiesLoader.COMMAND_HELP;
import static io.ballerina.shell.cli.PropertiesLoader.COMMAND_IMPORTS;
import static io.ballerina.shell.cli.PropertiesLoader.COMMAND_RESET;
import static io.ballerina.shell.cli.PropertiesLoader.COMMAND_VARS;
import static io.ballerina.shell.cli.PropertiesLoader.HEADER_FILE;
import static io.ballerina.shell.cli.PropertiesLoader.REPL_PROMPT;

/**
 * REPL shell terminal executor. Launches the terminal.
 * Independent of third party libraries.
 *
 * @since 2.0.0
 */
public class BallerinaShell {
    protected final BShellConfiguration configuration;
    protected final TerminalAdapter terminal;
    protected final Evaluator evaluator;
    protected final CommandHandler commandHandler;
    protected boolean isRunning;

    public BallerinaShell(BShellConfiguration configuration, TerminalAdapter terminal) {
        this.configuration = configuration;
        this.terminal = terminal;
        this.isRunning = true;
        this.evaluator = configuration.getEvaluator();
        this.commandHandler = createCommandHandler();
    }

    /**
     * Runs the terminal application using the given config.
     */
    public void run() {
        String leftPrompt = terminal.color(PropertiesLoader.getProperty(REPL_PROMPT),
                TerminalAdapter.GREEN);
        terminal.println(FileUtils.readResource(PropertiesLoader.getProperty(HEADER_FILE)));

        Instant start = Instant.now();
        // Initialize. This must not fail.
        // If this fails, an error would be directly thrown.
        try {
            evaluator.initialize();
            // If a start file is given, run it in initialization
            Optional<String> startFile = configuration.getStartFile();
            if (startFile.isPresent()) {
                evaluator.evaluateDeclarationFile(startFile.get());
            }
        } catch (BallerinaShellException e) {
            terminal.println("\nShell Initialization Failed!!!");
            return;
        } finally {
            evaluator.diagnostics().forEach(this::outputDebugDiagnostic);
            outputDistinctErrorDiagnostics(evaluator.diagnostics());
            evaluator.resetDiagnostics();
        }

        Instant end = Instant.now();

        while (isRunning) {
            Collection<Diagnostic> currentStateDiagnostics;
            Duration previousDuration = Duration.between(start, end);
            String rightPrompt = String.format("took %s ms", previousDuration.toMillis());
            rightPrompt = terminal.color(rightPrompt, TerminalAdapter.BRIGHT);
            String result;
            try {
                String source = terminal.readLine(leftPrompt, rightPrompt).trim();
                start = Instant.now();
                if (!commandHandler.handle(source)) {
                    ShellCompilation shellCompilation = evaluator.getCompilation(source);
                    Optional<PackageCompilation> compilation = shellCompilation.getPackageCompilation();
                    if (ExceptionStatus.SUCCESS == shellCompilation.getExceptionStatus()) {
                        Optional<ShellReturnValue> shellReturnValue = evaluator.getValue(compilation);
                        if (shellReturnValue.isPresent() &&
                                shellReturnValue.get().getExceptionStatus() == ExceptionStatus.SUCCESS) {
                            result = shellReturnValue.get().getResult();
                            terminal.result(result);
                        } else if (shellReturnValue.isPresent() && shellReturnValue.get().getExceptionStatus() ==
                                ExceptionStatus.INVOKER_FAILED) {
                            if (isContainsUndefinedModules(evaluator.diagnostics())) {
                                currentStateDiagnostics = List.copyOf(evaluator.diagnostics());
                                evaluator.resetDiagnostics();
                                executeChanges(source, currentStateDiagnostics);
                            }
                        }
                    } else if (shellCompilation.getExceptionStatus() == ExceptionStatus.INVOKER_FAILED) {
                        if (isContainsUndefinedModules(evaluator.diagnostics())) {
                            currentStateDiagnostics = List.copyOf(evaluator.diagnostics());
                            evaluator.resetDiagnostics();
                            executeChanges(source, currentStateDiagnostics);
                        }
                    }
                }
            } catch (ShellExitException e) {
                terminal.info("Bye!!!");
                isRunning = false;
                break;
            } catch (Exception e) {
                if (!evaluator.hasErrors()) {
                    terminal.fatalError("Something went wrong: " + e.getMessage());
                }
            } finally {
                end = Instant.now();
                evaluator.diagnostics().forEach(this::outputDebugDiagnostic);
                outputDistinctErrorDiagnostics(evaluator.diagnostics());
                evaluator.resetDiagnostics();
                terminal.println("");
            }
        }
    }

    /**
     * Runs a file to load declarations.
     *
     * @param fileName File path relative to the cwd.
     */
    public void runFile(String fileName) {
        try {
            evaluator.evaluateDeclarationFile(fileName);
        } catch (BallerinaShellException e) {
            outputException(e);
        }
    }

    /**
     * Deletes a collection of declaration names from REPL.
     * All must be defined names.
     *
     * @param declarationNames Names to delete.
     */
    public void delete(List<String> declarationNames) {
        try {
            evaluator.delete(declarationNames);
        } catch (BallerinaShellException e) {
            outputException(e);
        }
    }

    /**
     * Output a debug diagnostic to the terminal.
     *
     * @param diagnostic Diagnostic to output.
     */
    protected void outputDebugDiagnostic(Diagnostic diagnostic) {
        DiagnosticKind diagnosticKind = diagnostic.getKind();
        if (diagnosticKind == DiagnosticKind.DEBUG) {
            if (configuration.isDebug()) {
                terminal.debug(diagnostic.toString());
            }
        }
    }

    /**
     * Output error diagnostics to the terminal.
     *
     * @param diagnostics list of available diagnostics.
     */
    protected void outputDistinctErrorDiagnostics(Collection<Diagnostic> diagnostics) {
        List<String> warnDiagnostics = new ArrayList<>();
        List<String> errorDiagnostics  = new ArrayList<>();
        for (Diagnostic diagnostic: diagnostics) {
            if (diagnostic.getKind() == DiagnosticKind.ERROR) {
                errorDiagnostics.add(diagnostic.toString());
            } else if (diagnostic.getKind() == DiagnosticKind.WARN) {
                warnDiagnostics.add(diagnostic.toString());
            }
        }

        warnDiagnostics.stream().distinct().forEach(terminal::warn);
        errorDiagnostics.stream().distinct().forEach(terminal::error);
    }

    /**
     * Outputs an exception to the terminal.
     *
     * @param e Exception to output.
     */
    protected void outputException(Exception e) {
        if (configuration.isDebug()) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            terminal.fatalError(stringWriter.toString());
        }
    }

    /**
     * Attaches commands to the handler which handles internal command.
     *
     * @return Command attached handler.
     */
    private CommandHandler createCommandHandler() {
        CommandHandler commandHandler = new CommandHandler();
        commandHandler.attach(PropertiesLoader.getProperty(COMMAND_EXIT), new ExitCommand(this));
        commandHandler.attach(PropertiesLoader.getProperty(COMMAND_HELP), new HelpCommand(this));
        commandHandler.attach(PropertiesLoader.getProperty(COMMAND_RESET), new ResetStateCommand(this));
        commandHandler.attach(PropertiesLoader.getProperty(COMMAND_DEBUG), new ToggleDebugCommand(this));
        commandHandler.attach(PropertiesLoader.getProperty(COMMAND_FILE), new FileCommand(this));
        commandHandler.attach(PropertiesLoader.getProperty(COMMAND_DELETE), new DeleteCommand(this));
        commandHandler.attach(PropertiesLoader.getProperty(COMMAND_VARS),
                new StringListCommand(this, evaluator::availableVariables));
        commandHandler.attach(PropertiesLoader.getProperty(COMMAND_IMPORTS),
                new StringListCommand(this, evaluator::availableImports));
        commandHandler.attach(PropertiesLoader.getProperty(COMMAND_DCLNS),
                new StringListCommand(this, evaluator::availableModuleDeclarations));
        return commandHandler;
    }

    public void outputInfo(String text) {
        this.terminal.info(text);
    }

    public void outputError(String text) {
        this.terminal.error(text);
    }

    public boolean toggleDebug() {
        this.configuration.toggleDebug();
        return this.configuration.isDebug();
    }

    public void reset() {
        try {
            this.evaluator.reset();
            this.evaluator.initialize();
        } catch (BallerinaShellException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add modules exists in the Ballerina Distribution repo.
     *
     * @param moduleImporter moduleImporter.
     * @param modules        available modules.
     */
    public void importModules(ModuleImporter moduleImporter, List<String> modules) {
        List<String> missingModules = new ArrayList<>();
        List<String> requiredModules = new ArrayList<>();
        terminal.info("\nFound following undefined module(s).");
        for (String module : modules) {
            module = module.replace("'", "");
            if (moduleImporter.isModuleInDistRepo(module)) {
                requiredModules.add(module);
            } else {
                missingModules.add(module);
            }
            terminal.info(module);
        }

        if (requiredModules.size() > 0) {
            terminal.info("\nFollowing undefined modules can be imported.");
            int moduleCount = 1;
            for (String module : requiredModules) {
                terminal.info(String.format("%d. %s", moduleCount, module));
                moduleCount += 1;
            }
            
            String answer = terminal.readOneLine("Do you want to import mentioned modules (yes/y) (no/n)? ");
            if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) {
                for (String module : requiredModules) {
                    String importSource = moduleImporter.getImportStatement(module);
                    terminal.info("\nAdding import: " + importSource);
                    try {
                        ShellCompilation shellCompilation = evaluator.getCompilation(importSource);
                        Optional<PackageCompilation> compilation = shellCompilation.getPackageCompilation();
                        evaluator.getValue(compilation);
                        terminal.info("Import added: " + importSource);
                    } catch (BallerinaShellException ex) {
                        terminal.error("Error occurred while adding imports.");
                        outputException(ex);
                    }
                }

                if (missingModules.size() > 0) {
                    terminal.error("\nFound following missing module(s).");
                    for (String missingModule : missingModules) {
                        terminal.error(missingModule);
                    }
                }
            } else {
                terminal.error("\nFound missing module(s).");
            }

        } else {
            terminal.error("\nFound missing module(s).");
        }
    }

    /**
     * Return found undefined modules.
     *
     * @param diagnostics collection of evaluator diagnostics.
     * @return distinct list of module errors.
     */
    public boolean addImports(Collection<Diagnostic> diagnostics) {
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.toString().contains("undefined module")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return found undefined modules.
     *
     * @param diagnostics collection of evaluator diagnostics.
     * @return distinct list of module errors.
     */
    public boolean isContainsUndefinedModules(Collection<Diagnostic> diagnostics) {
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.toString().contains("undefined module")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Execute with updated imports.
     *
     * @param source source.
     */
    private void executeChanges(String source, Collection<Diagnostic> diagnostics) {
        ModuleImporter moduleImporter = new ModuleImporter();
        List<String> modules = moduleImporter.undefinedModules(diagnostics);
        if (modules.size() > 0) {
            importModules(moduleImporter, modules);
            try {
                terminal.println("");
                ShellCompilation shellCompilation = evaluator.getCompilation(source);
                Optional<PackageCompilation> compilation = shellCompilation.getPackageCompilation();
                ShellReturnValue shellReturnValue = evaluator.getValue(compilation).get();
                String result = shellReturnValue.getResult();
                terminal.result(result);
            } catch (BallerinaShellException error) {
                terminal.error("\nCompilation aborted due to errors.");
            } catch (Exception e) {
                throw e;
            }
        }
    }
}
