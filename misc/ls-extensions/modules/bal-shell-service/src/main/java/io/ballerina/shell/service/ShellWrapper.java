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

import io.ballerina.projects.PackageCompilation;
import io.ballerina.shell.Evaluator;
import io.ballerina.shell.ExceptionStatus;
import io.ballerina.shell.ShellCompilation;
import io.ballerina.shell.cli.BShellConfiguration;
import io.ballerina.shell.exceptions.BallerinaShellException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ShellWrapper {
    private BShellConfiguration configuration;
    private Evaluator evaluator;
    private File tempFile;
    private static ShellWrapper instance;
    private static final String TEMP_FILE_PREFIX = "temp-";
    private static final String TEMP_FILE_SUFFIX = ".bal";

    private ShellWrapper(){
        this.configuration = new BShellConfiguration.Builder().build();
        this.initializeEvaluator();
    }

    public static ShellWrapper getInstance(){
        if (instance == null){
            synchronized (ShellWrapper.class){
                if (instance == null){
                    instance = new ShellWrapper();
                }
            }
        }
        return instance;
    }

    public BalShellResponse getResult(String source){
        BalShellResponse output = new BalShellResponse();
        PrintStream original = System.out;
        ConsoleOutCollector consoleOutCollector = new ConsoleOutCollector();
        System.setOut(new PrintStream(consoleOutCollector));
        try {
            ShellCompilation shellCompilation = evaluator.getCompilation(source);
            Optional<PackageCompilation> compilation = shellCompilation.getPackageCompilation();
            if (ExceptionStatus.SUCCESS == shellCompilation.getExceptionStatus()) {
                Object out = evaluator.getValueAsObject(compilation).get();
                List<String> consoleOut = consoleOutCollector.getLines();
                output.setValue(out, consoleOut);
            }
        } catch (Exception error) {
            List<String> consoleOut = consoleOutCollector.getLines();
            output.setValue(null, consoleOut);
            if (!(error instanceof NoSuchElementException)){
                output.addError(error.getMessage());
            }
        } finally {
            output.addOutputDiagnostics(evaluator.diagnostics(), configuration.isDebug());
            evaluator.resetDiagnostics();
            System.setOut(original);
        }
        return output;
    }

    public ShellFileSourceResponse getShellFileSource(){
        String fileContent;
        try {
            fileContent = new String(Files.readAllBytes(Paths.get(evaluator.getBufferFileUri()))).trim();
            File tempFile = writeToFile(fileContent);
            return new ShellFileSourceResponse(tempFile, fileContent);
        } catch (IOException ignored) {
        }
        return null;
    }

    public List<Map<String, String>> getAvailableVariables(){
        return evaluator.availableVariablesAsMap();
    }

    public boolean restart(){
        evaluator.reset();
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
        } catch (BallerinaShellException error) {
            error.printStackTrace();
        } finally {
            this.evaluator.resetDiagnostics();
        }
    }
}

