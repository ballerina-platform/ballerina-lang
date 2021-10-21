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

package io.ballerina.shell.parser.trials;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.shell.parser.TrialTreeParser;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.TextDocument;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Trial for testing for the correct syntax tree.
 *
 * @since 2.0.0
 */
public abstract class TreeParserTrial {
    protected final TrialTreeParser parentParser;

    protected TreeParserTrial(TrialTreeParser parentParser) {
        this.parentParser = parentParser;
    }

    /**
     * Tries to parse the source into a syntax tree.
     * Returns null if failed.
     *
     * @param source Input source statement.
     * @return Parsed syntax tree root node. Null if failed.
     * @throws ParserTrialFailedException If trial failed.
     */
    public abstract Collection<Node> parse(String source) throws ParserTrialFailedException;

    /**
     * Creates and checks for errors in the syntax tree.
     *
     * @param document Document to parse.
     * @return Created syntax tree.
     * @throws ParserTrialFailedException If tree contains errors.
     */
    protected SyntaxTree getSyntaxTree(TextDocument document) throws ParserTrialFailedException {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<SyntaxTree> future = executor.submit(() -> SyntaxTree.from(document));
        executor.shutdown();

        SyntaxTree tree;
        try {
            tree = future.get(getTimeOutDurationMs(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new ParserTrialFailedException("Tree parsing was interrupted.");
        } catch (ExecutionException e) {
            throw new ParserTrialFailedException("Executor failure because " + e.getCause().getMessage());
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ParserTrialFailedException("Tree parsing was timed out.");
        }

        for (Diagnostic diagnostic : tree.diagnostics()) {
            if (diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
                throw new ParserTrialFailedException(tree.textDocument(), diagnostic);
            }
        }
        return tree;
    }

    /**
     * Helper assertion to throw if condition is not satisfied.
     *
     * @param condition Condition to check.
     * @param message   Error message if failed.
     * @throws ParserTrialFailedException If condition is not satisfied.
     */
    protected void assertIf(boolean condition, String message) throws ParserTrialFailedException {
        if (!condition) {
            throw new ParserTrialFailedException(message);
        }
    }

    /**
     * Get the timeout set from the parent trial parser.
     *
     * @return The timeout for parsing the tree.
     */
    protected long getTimeOutDurationMs() {
        return parentParser.getTimeOutDurationMs();
    }
}
