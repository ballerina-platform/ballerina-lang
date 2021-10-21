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
import io.ballerina.shell.exceptions.PreprocessorException;
import io.ballerina.shell.invoker.ShellSnippetsInvoker;
import io.ballerina.shell.parser.TreeParser;
import io.ballerina.shell.preprocessor.Preprocessor;
import io.ballerina.shell.snippet.Snippet;
import io.ballerina.shell.snippet.factory.SnippetFactory;
import io.ballerina.shell.utils.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

/**
 * Implemented Evaluator.
 *
 * @since 2.0.0
 */
class EvaluatorImpl extends Evaluator {
    protected EvaluatorImpl(Preprocessor preprocessor, TreeParser treeParser,
                            SnippetFactory snippetFactory, ShellSnippetsInvoker invoker) {
        super(preprocessor, treeParser, snippetFactory, invoker);
    }

    @Override
    public void initialize() throws BallerinaShellException {
        try {
            invoker.initialize();
        } catch (BallerinaShellException e) {
            addAllDiagnostics(invoker.diagnostics());
            throw e;
        }
    }

    @Override
    public String evaluate(String source) throws BallerinaShellException {
        try {
            Collection<Node> nodes = treeParser.parseString(source);
            Collection<Snippet> snippets = snippetFactory.createSnippets(nodes);
            Optional<Object> invokerOut = invoker.execute(snippets);
            return invokerOut.map(StringUtils::getExpressionStringValue).orElse(null);
        } finally {
            addAllDiagnostics(treeParser.diagnostics());
            addAllDiagnostics(snippetFactory.diagnostics());
            addAllDiagnostics(invoker.diagnostics());
            preprocessor.resetDiagnostics();
            treeParser.resetDiagnostics();
            snippetFactory.resetDiagnostics();
            invoker.resetDiagnostics();
        }
    }

    @Override
    public void evaluateDeclarationFile(String filePath) throws BallerinaShellException {
        try {
            String statements = Files.readString(Paths.get(filePath), Charset.defaultCharset());
            Collection<Node> nodes = treeParser.parseDeclarations(statements);
            Collection<Snippet> snippets = snippetFactory.createSnippets(nodes);
            invoker.execute(snippets);
        } catch (IOException e) {
            addErrorDiagnostic("Failed to load declarations from file: " + filePath);
            throw new PreprocessorException();
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

    @Override
    public void delete(Collection<String> declarationNames) throws BallerinaShellException {
        try {
            invoker.delete(new HashSet<>(declarationNames));
        } finally {
            addAllDiagnostics(invoker.diagnostics());
            invoker.resetDiagnostics();
        }
    }

    @Override
    public void reset() {
        preprocessor.resetDiagnostics();
        treeParser.resetDiagnostics();
        snippetFactory.resetDiagnostics();
        invoker.resetDiagnostics();
        this.resetDiagnostics();
        invoker.reset();
    }
}
