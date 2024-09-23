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

package io.ballerina.syntaxapicallsgen.parser;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.syntaxapicallsgen.SyntaxApiCallsGenException;
import io.ballerina.syntaxapicallsgen.config.SyntaxApiCallsGenConfig;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.TextDocument;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * In this stage the correct syntax tree is identified.
 * The root node of the syntax tree must be the corresponding
 * type for the statement.
 * For an example, for a import declaration,
 * the tree that is parsed should have
 * {@code ImportDeclarationNode} as the root node.
 *
 * @since 2.0.0
 */
public abstract class SyntaxApiCallsGenParser {
    private static final Set<String> SILENCED_ERRORS = Set.of("BCE0517");
    private final long timeoutMs;

    protected SyntaxApiCallsGenParser(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    /**
     * Creates a parser depending on the configuration given.
     */
    public static SyntaxApiCallsGenParser fromConfig(SyntaxApiCallsGenConfig config) {
        long timeoutMs = config.parserTimeout();
        return switch (config.parser()) {
            case EXPRESSION -> new ExpressionParser(timeoutMs);
            case STATEMENT -> new StatementParser(timeoutMs);
            default -> new ModuleParser(timeoutMs);
        };
    }

    /**
     * Highlight and show the error position.
     *
     * @param textDocument Text document to extract source code.
     * @param diagnostic   Diagnostic to show.
     * @return The string with position highlighted.
     */
    public static String highlightDiagnostic(TextDocument textDocument, Diagnostic diagnostic) {
        // Get the source code
        String space = " ";
        String sourceLine = textDocument.line(diagnostic.location().lineRange().startLine().line()).text();
        int position = diagnostic.location().lineRange().startLine().offset();
        return String.format("%s%n%s%n%s^", diagnostic.message(), sourceLine, space.repeat(position));
    }

    /**
     * Creates and checks for errors in the syntax tree.
     *
     * @param document Document to parse.
     * @return Created syntax tree.
     * @throws SyntaxApiCallsGenException If tree contains errors.
     */
    protected SyntaxTree getSyntaxTree(TextDocument document) throws SyntaxApiCallsGenException {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<SyntaxTree> future = executor.submit(() -> SyntaxTree.from(document));
        executor.shutdown();

        SyntaxTree tree;
        try {
            tree = future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new SyntaxApiCallsGenException("Parsing was interrupted.");
        } catch (ExecutionException e) {
            throw new SyntaxApiCallsGenException("Executor failure because " + e.getCause().getMessage());
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new SyntaxApiCallsGenException("Parsing was timed out.");
        }

        for (Diagnostic diagnostic : tree.diagnostics()) {
            if (diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
                if (!SILENCED_ERRORS.contains(diagnostic.diagnosticInfo().code())) {
                    throw new SyntaxApiCallsGenException(highlightDiagnostic(document, diagnostic));
                }
            }
        }
        return tree;
    }

    /**
     * Parses a source code string into a Node.
     *
     * @param source Input source code statement.
     * @return Syntax tree for the source code.
     */
    public abstract Node parse(String source);

    /**
     * Helper assertion to throw if condition is not satisfied.
     *
     * @param condition Condition to check.
     * @param message   Error message if failed.
     */
    protected void assertIf(boolean condition, String message) {
        if (!condition) {
            throw new SyntaxApiCallsGenException(message);
        }
    }
}
