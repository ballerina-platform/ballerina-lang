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

package io.ballerina.shell;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.shell.exceptions.BallerinaShellException;
import io.ballerina.shell.invoker.ShellSnippetsInvoker;
import io.ballerina.shell.parser.TreeParser;
import io.ballerina.shell.preprocessor.Preprocessor;
import io.ballerina.shell.snippet.Snippet;
import io.ballerina.shell.snippet.factory.SnippetFactory;
import io.ballerina.shell.snippet.types.DeclarationSnippet;
import io.ballerina.shell.utils.StringUtils;
import io.ballerina.shell.utils.timeit.TimeIt;
import io.ballerina.shell.utils.timeit.TimedOperation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Main shell entry point.
 * Creates an virtual shell which will accept input from
 * a terminal and evaluate each expression.
 *
 * @since 2.0.0
 */
public class Evaluator extends DiagnosticReporter {
    private final Preprocessor preprocessor;
    private final TreeParser treeParser;
    private final SnippetFactory snippetFactory;
    private final ShellSnippetsInvoker invoker;

    public Evaluator(Preprocessor preprocessor, TreeParser treeParser,
                     SnippetFactory snippetFactory, ShellSnippetsInvoker invoker) {
        this.preprocessor = preprocessor;
        this.treeParser = treeParser;
        this.snippetFactory = snippetFactory;
        this.invoker = invoker;
    }

    /**
     * Initialized the required components.
     * Calling this first is not required.
     * However if called, this will make subsequent runs faster.
     *
     * @throws BallerinaShellException If initialization failed.
     */
    public void initialize() throws BallerinaShellException {
        try {
            invoker.initialize();
        } catch (BallerinaShellException e) {
            addAllDiagnostics(invoker.diagnostics());
            throw e;
        }
    }

    /**
     * Base evaluation function which evaluates an input line.
     * <p>
     * An input line may contain one or more statements separated by semicolons.
     * The result will be written via the {@code ShellResultController}.
     * Each stage of the evaluator will be notified wia the controller.
     * <p>
     * If the execution failed, an error will be thrown instead.
     * If there was an error in one of the statements in the line,
     * then this will stop execution without evaluating later lines.
     *
     * @param source Input line from user.
     * @return String output from the evaluator. This will be the last output.
     */
    public String evaluate(String source) throws BallerinaShellException {
        String response = null;
        try {
            Collection<String> statements = timedOperation("preprocessor", () -> preprocessor.process(source));
            for (String statement : statements) {
                Node rootNode = timedOperation("tree parser", () -> treeParser.parse(statement));
                Snippet snippet = timedOperation("snippet factory", () -> snippetFactory.createSnippet(rootNode));
                Optional<Object> invokerOut = timedOperation("invoker", () -> invoker.execute(snippet));

                if (invokerOut.isPresent()) {
                    response = StringUtils.getExpressionStringValue(invokerOut.get());
                }
            }
            return response;
        } finally {
            addAllDiagnostics(preprocessor.diagnostics());
            addAllDiagnostics(treeParser.diagnostics());
            addAllDiagnostics(snippetFactory.diagnostics());
            addAllDiagnostics(invoker.diagnostics());
            preprocessor.resetDiagnostics();
            treeParser.resetDiagnostics();
            snippetFactory.resetDiagnostics();
            invoker.resetDiagnostics();
        }
    }

    /**
     * Executes a ballerina file as if it was entered to the shell.
     * Some functions, (eg: main) are skipped.
     * All other top level declarations will be processed.
     * Whole file will run as a one whole snippet.
     * Main function will be skipped.
     *
     * @param file File to execute.
     */
    public void executeFile(File file) throws BallerinaShellException {
        try {
            addDebugDiagnostic("Loading file: " + file.getAbsolutePath());
            String statements = timedOperation("preprocessor", () -> preprocessor.processFile(file));
            Collection<Node> nodes = timedOperation("tree parser", () -> treeParser.parseDeclarations(statements));
            List<DeclarationSnippet> declarationSnippets = new ArrayList<>();
            for (Node node : nodes) {
                declarationSnippets.add(snippetFactory.createDeclarationSnippet(node));
            }
            timedOperation("invoker", () -> invoker.executeDeclarations(declarationSnippets));
        } finally {
            addAllDiagnostics(preprocessor.diagnostics());
            addAllDiagnostics(treeParser.diagnostics());
            addAllDiagnostics(snippetFactory.diagnostics());
            addAllDiagnostics(invoker.diagnostics());
            preprocessor.resetDiagnostics();
            treeParser.resetDiagnostics();
            snippetFactory.resetDiagnostics();
            invoker.resetDiagnostics();
        }
    }

    /**
     * Deletes a collection of names from the evaluator state.
     * If any of the names did not exist, this will throw an error.
     * A compilation will be done to make sure that no new errors are there.
     * This cannot be undone.
     *
     * @param declarationNames Names to delete.
     * @throws BallerinaShellException if deletion failed.
     */
    public void delete(Collection<String> declarationNames) throws BallerinaShellException {

        try {
            invoker.delete(new HashSet<>(declarationNames));
        } finally {
            addAllDiagnostics(invoker.diagnostics());
            invoker.resetDiagnostics();
        }
    }

    /**
     * Reset evaluator so that the execution can be start over.
     */
    public void reset() {
        preprocessor.resetDiagnostics();
        treeParser.resetDiagnostics();
        snippetFactory.resetDiagnostics();
        invoker.resetDiagnostics();
        this.resetDiagnostics();
        invoker.reset();
    }

    public List<String> availableImports() {
        return invoker.availableImports();
    }

    public List<String> availableVariables() {
        return invoker.availableVariables();
    }

    public List<String> availableModuleDeclarations() {
        return invoker.availableModuleDeclarations();
    }

    public Preprocessor getPreprocessor() {
        return preprocessor;
    }

    public TreeParser getTreeParser() {
        return treeParser;
    }

    public SnippetFactory getSnippetFactory() {
        return snippetFactory;
    }

    public ShellSnippetsInvoker getInvoker() {
        return invoker;
    }

    /**
     * Time the operation and add diagnostics to {@link Evaluator}.
     *
     * @param category  Category to add diagnostics. Should be unique per operation.
     * @param operation Operation to perform.
     * @param <T>       operation return type.
     * @return Return value of operation.
     * @throws BallerinaShellException If operation failed.
     */
    private <T> T timedOperation(String category, TimedOperation<T> operation) throws BallerinaShellException {
        return TimeIt.timeIt(category, this, operation);
    }
}
