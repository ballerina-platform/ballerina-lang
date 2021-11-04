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
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This will process without timing out so we can get a
 * better error message.
 * Otherwise error will be just 'timed out'
 *
 * @since 2.0.0
 */
public class GetErrorMessageTrial extends TreeParserTrial {
    private static final int ERROR_TIMEOUT_MULTIPLIER = 10;
    private static final Set<String> MODULE_LEVEL_ERROR_CODES = Set.of("BCE0007", "BCE0646");

    public GetErrorMessageTrial(TrialTreeParser parentParser) {
        super(parentParser);
    }

    @Override
    public Collection<Node> parse(String source) throws ParserTrialFailedException {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<?> future = executor.submit(() -> processSource(source));
        executor.shutdown();
        try {
            future.get(getTimeOutDurationMs(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new ParserTrialFailedException("Tree parsing was interrupted.");
        } catch (ExecutionException e) {
            // Error message thrown should be in "syntax error: ERROR" format.
            String errorMessage = e.getCause().getMessage();
            if (errorMessage.startsWith("error:")) {
                throw new ParserTrialFailedException("syntax " + errorMessage);
            }
            throw new ParserTrialFailedException("syntax error: " + errorMessage);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ParserTrialFailedException("Tree parsing was timed out.");
        }
        throw new IllegalStateException("Unknown statement");
    }

    /**
     * Process a invalid source and throw relevant error.
     * This would be run under a timing threshold.
     * So this has to detect the error within that constraint.
     *
     * @param source Source code with errors.
     */
    private void processSource(String source) {
        try {
            TextDocument document = TextDocuments.from(String.format("function main(){%n%s%n}", source));
            SyntaxTree tree = SyntaxTree.from(document);
            for (Diagnostic diagnostic : tree.diagnostics()) {
                DiagnosticInfo diagnosticInfo = diagnostic.diagnosticInfo();
                if (diagnosticInfo.severity() == DiagnosticSeverity.ERROR) {
                    if (!MODULE_LEVEL_ERROR_CODES.contains(diagnosticInfo.code())) {
                        throw new ParserTrialFailedException(tree.textDocument(), diagnostic);
                    }
                    break;
                }
            }

            // We got to parse as a top level module dcln
            tree = SyntaxTree.from(TextDocuments.from(source));
            for (Diagnostic diagnostic : tree.diagnostics()) {
                if (diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
                    throw new ParserTrialFailedException(tree.textDocument(), diagnostic);
                }
            }
        } catch (ParserTrialFailedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    protected long getTimeOutDurationMs() {
        return super.getTimeOutDurationMs() * ERROR_TIMEOUT_MULTIPLIER;
    }
}
