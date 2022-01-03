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

import io.ballerina.projects.PackageCompilation;
import io.ballerina.shell.exceptions.BallerinaShellException;
import io.ballerina.shell.invoker.ShellSnippetsInvoker;
import io.ballerina.shell.parser.TreeParser;
import io.ballerina.shell.preprocessor.Preprocessor;
import io.ballerina.shell.snippet.factory.SnippetFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Main shell entry point.
 * Creates a virtual shell which will accept input from
 * a terminal and evaluate each expression.
 *
 * @since 2.0.0
 */
public abstract class Evaluator extends DiagnosticReporter {
    protected final Preprocessor preprocessor;
    protected final TreeParser treeParser;
    protected final SnippetFactory snippetFactory;
    protected final ShellSnippetsInvoker invoker;

    protected Evaluator(Preprocessor preprocessor, TreeParser treeParser,
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
    public abstract void initialize() throws BallerinaShellException;

    /**
     * Base evaluation function which evaluates an input line.
     * <p>
     * An input line may contain one or more statements separated by semicolons.
     * The result will be written via the {@code ShellResultController}.
     * <p>
     * If the execution failed, an error will be thrown instead.
     *
     * @param source Input line from user.
     * @return String output from the evaluator. This will be the last output.
     */
    public abstract String evaluate(String source) throws BallerinaShellException;

    /**
     * Base evaluation function which returns compilation.
     * <p>
     * An input line may contain one or more statements separated by semicolons.
     * The result will be written via the {@code ShellResultController}.
     * <p>
     * If the execution failed, an error will be thrown instead.
     *
     * @param source Input line from user.
     * @return compilation.
     */
    public abstract ShellCompilation getCompilation(String source);

    /**
     * Base evaluation function.
     * <p>
     * An input line may contain one or more statements separated by semicolons.
     * The result will be written via the {@code ShellResultController}.
     * <p>
     * If the execution failed, an error will be thrown instead.
     *
     * @param compilation compilation.
     * @return String output from the evaluator. This will be the last output.
     */
    public abstract Optional<ShellReturnValue> getValue(Optional<PackageCompilation> compilation)
            throws BallerinaShellException;

    /**
     * Evaluate a ballerina file as if it was entered to the shell.
     * This file should only include declarations.
     * Some functions, (eg: main) are skipped.
     * All other top level declarations will be processed.
     * Whole file will run as a one whole snippet.
     * Main function will be skipped.
     *
     * @param filePath Path of file to execute.
     */
    public abstract void evaluateDeclarationFile(String filePath) throws BallerinaShellException;

    /**
     * Deletes a collection of names from the evaluator state.
     * If any of the names did not exist, this will throw an error.
     * A compilation will be done to make sure that no new errors are there.
     * This cannot be undone.
     *
     * @param declarationNames Names to delete.
     * @throws BallerinaShellException if deletion failed.
     */
    public abstract void delete(Collection<String> declarationNames) throws BallerinaShellException;

    /**
     * Reset evaluator so that the execution can be start over.
     */
    public abstract void reset();

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
}
