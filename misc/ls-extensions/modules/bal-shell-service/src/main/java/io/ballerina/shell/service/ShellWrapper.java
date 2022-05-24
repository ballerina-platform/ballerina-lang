/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.shell.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.shell.Evaluator;
import io.ballerina.shell.ExceptionStatus;
import io.ballerina.shell.NotebookReturnValue;
import io.ballerina.shell.ShellCompilation;
import io.ballerina.shell.cli.BShellConfiguration;
import io.ballerina.shell.exceptions.BallerinaShellException;
import io.ballerina.shell.exceptions.InvokerException;
import io.ballerina.shell.exceptions.InvokerPanicException;
import io.ballerina.shell.exceptions.SnippetException;
import io.ballerina.shell.exceptions.TreeParserException;
import io.ballerina.shell.invoker.AvailableVariable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Wrapper for Ballerina Shell.
 *
 * @since 2201.1.1
 */
public class ShellWrapper {
    private final BShellConfiguration configuration;
    private Evaluator evaluator;
    private File tempFile;
    private static final String TEMP_FILE_PREFIX = "temp-";
    private static final String TEMP_FILE_SUFFIX = ".bal";

    private static class InstanceHolder {
        private static final ShellWrapper instance = new ShellWrapper();
    }

    private ShellWrapper() {
        this.configuration = new BShellConfiguration.Builder().build();
        this.initializeEvaluator();
    }

    public static ShellWrapper getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * Evaluate and returns result for a given source snippet.
     *
     * @param source evaluated value
     * @return  result after the execution
     */
    public BalShellGetResultResponse getResult(String source) {
        BalShellGetResultResponse output = new BalShellGetResultResponse();
        PrintStream original = System.out;
        ConsoleOutCollector consoleOutCollector = new ConsoleOutCollector();
        System.setOut(new PrintStream(consoleOutCollector, false, Charset.defaultCharset()));
        try {
            ShellCompilation shellCompilation = evaluator.getCompilation(source);
            // continue the execution if the compilation is done successfully
            // info related to errors required for the Ballerina notebook in compilation
            // will include in diagnostics
            if (shellCompilation.getExceptionStatus() == ExceptionStatus.SUCCESS) {
                Optional<PackageCompilation> compilation = shellCompilation.getPackageCompilation();
                Optional<NotebookReturnValue> notebookReturnValue = evaluator.getValueAsObject(compilation);
                if (notebookReturnValue.isPresent() &&
                        notebookReturnValue.get().getExceptionStatus() == ExceptionStatus.SUCCESS) {
                    Object out = notebookReturnValue.get().getResult();
                    output.setValue(out);
                } else if (notebookReturnValue.isPresent() &&
                        notebookReturnValue.get().getExceptionStatus() == ExceptionStatus.INVOKER_FAILED) {
                    throw new InvokerException();
                }
            // for other exception statuses throw errors accordingly
            } else if (shellCompilation.getExceptionStatus() == ExceptionStatus.SNIPPET_FAILED) {
                throw new SnippetException();
            } else if (shellCompilation.getExceptionStatus() == ExceptionStatus.TREE_PARSER_FAILED) {
                throw new TreeParserException();
            } else {
                throw new InvokerException();
            }
        } catch (InvokerPanicException error) {
            output.addError("panic: " + error.getMessage());
        } catch (Exception error) { // handling unidentified runtime errors and invoker exceptions
            output.addError(error.getMessage());
        } finally {
            output.setConsoleOut(consoleOutCollector.getLines());
            output.addOutputDiagnostics(evaluator.diagnostics());
            output.setMetaInfo(
                    evaluator.newVariableNames(),
                    evaluator.newModuleDeclarations()
            );
            evaluator.resetDiagnostics();
            evaluator.clearPreviousVariablesAndModuleDclnsNames();
            System.setOut(original);
        }
        return output;
    }

    /**
     * Get data associated with temp file created by shell.
     *
     * @return temp file uri as a strings and its content
     */
    public ShellFileSourceResponse getShellFileSource() {
        String fileContent;
        try {
            fileContent = Files.readString(Paths.get(evaluator.getBufferFileUri()), Charset.defaultCharset()).trim();
            File tempFile = writeToFile(fileContent);
            return new ShellFileSourceResponse(tempFile, fileContent);
        } catch (IOException ignored) {
        }
        return new ShellFileSourceResponse();
    }

    /**
     * Get values and types of user defined variables.
     *
     * @return list of variables with its name, type and current value
     */
    public List<Map<String, String>> getAvailableVariables() {
        List<Map<String, String>> availableVarsMap = new ArrayList<>();
        for (AvailableVariable availableVar: evaluator.availableVariablesAsObjects()) {
            ObjectMapper oMapper = new ObjectMapper();
            Map<String, String> varMap = oMapper.convertValue(availableVar, Map.class);
            availableVarsMap.add(varMap);
        }
        return availableVarsMap;
    }

    /**
     * Deletes defined values from the shell state.
     *
     * returns true when successfully completed else false
     */
    public boolean deleteDeclarations(String varToDelete) {
        try {
            evaluator.delete(List.of(varToDelete));
        } catch (BallerinaShellException e) {
            evaluator.resetDiagnostics();
            return false;
        }
        return true;
    }

    /**
     * Reset evaluator so that the execution can be start over.
     *
     * returns true when completed
     */
    public boolean restart() {
        this.evaluator.reset();
        this.initializeEvaluator();
        return true;
    }

    private File writeToFile(String source) throws IOException {
        File tempFile = getTempBufferFile();
        try (FileWriter fileWriter = new FileWriter(tempFile, Charset.defaultCharset())) {
            fileWriter.write(source);
        }
        return tempFile;
    }

    private File getTempBufferFile() throws IOException {
        if (this.tempFile == null) {
            this.tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
            this.tempFile.deleteOnExit();
        }
        return this.tempFile;
    }

    private void initializeEvaluator() {
        this.evaluator = this.configuration.getEvaluator();
        try {
            this.evaluator.initialize();
        } catch (BallerinaShellException ignored) {
        } finally {
            this.evaluator.resetDiagnostics();
        }
    }
}

