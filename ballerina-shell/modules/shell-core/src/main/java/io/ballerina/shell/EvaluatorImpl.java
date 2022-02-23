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
import io.ballerina.projects.PackageCompilation;
import io.ballerina.shell.exceptions.BallerinaShellException;
import io.ballerina.shell.exceptions.InvokerException;
import io.ballerina.shell.exceptions.InvokerPanicException;
import io.ballerina.shell.exceptions.PreprocessorException;
import io.ballerina.shell.exceptions.SnippetException;
import io.ballerina.shell.exceptions.TreeParserException;
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
            Optional<PackageCompilation> compilation = Optional.ofNullable(invoker.getCompilation(snippets));
            Optional<Object> invokerOut = invoker.execute(compilation);
            return invokerOut.map(StringUtils::getExpressionStringValue).orElse(null);
        } finally {
            addAllDiagnostics(treeParser.diagnostics());
            addAllDiagnostics(snippetFactory.diagnostics());
            addAllDiagnostics(invoker.diagnostics());
            treeParser.resetDiagnostics();
            snippetFactory.resetDiagnostics();
            invoker.resetDiagnostics();
        }
    }

    public ShellCompilation getCompilation(String source) {
        PackageCompilation compilation;
        ExceptionStatus exceptionStatus;
        try {
            Collection<Node> nodes = treeParser.parseString(source);
            Collection<Snippet> snippets = snippetFactory.createSnippets(nodes);
            compilation = invoker.getCompilation(snippets);
            exceptionStatus = ExceptionStatus.SUCCESS;
            clearDiagnostics(exceptionStatus);
            return new ShellCompilation(compilation, exceptionStatus);
        } catch (TreeParserException e) {
            exceptionStatus = ExceptionStatus.TREE_PARSER_FAILED;
            clearDiagnostics(exceptionStatus);
            return new ShellCompilation(exceptionStatus);
        } catch (SnippetException e) {
            exceptionStatus = ExceptionStatus.SNIPPET_FAILED;
            clearDiagnostics(exceptionStatus);
            return new ShellCompilation(exceptionStatus);
        } catch (InvokerException e) {
            exceptionStatus = ExceptionStatus.INVOKER_FAILED;
            clearDiagnostics(exceptionStatus);
            return new ShellCompilation(exceptionStatus);
        }
    }

    @Override
    public Optional<ShellReturnValue> getValue(Optional<PackageCompilation> compilation) throws
            BallerinaShellException {
        String result;
        ExceptionStatus exceptionStatus;
        try {
            Optional<Object> invokerOut = invoker.execute(compilation);
            result = invokerOut.map(StringUtils::getExpressionStringValue).orElse(null);
            exceptionStatus = ExceptionStatus.SUCCESS;
            addAllDiagnostics(invoker.diagnostics());
            invoker.resetDiagnostics();
            return Optional.of(new ShellReturnValue(result, exceptionStatus));
        } catch (InvokerPanicException e) {
            addAllDiagnostics(invoker.diagnostics());
            invoker.resetDiagnostics();
            throw e;
        } catch (InvokerException e) {
            exceptionStatus = ExceptionStatus.INVOKER_FAILED;
            addAllDiagnostics(invoker.diagnostics());
            invoker.resetDiagnostics();
            return Optional.of(new ShellReturnValue(exceptionStatus));
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void evaluateDeclarationFile(String filePath) throws BallerinaShellException {
        try {
            String statements = Files.readString(Paths.get(filePath), Charset.defaultCharset());
            Collection<Node> nodes = treeParser.parseDeclarations(statements);
            Collection<Snippet> snippets = snippetFactory.createSnippets(nodes);
            getValue(Optional.ofNullable(invoker.getCompilation(snippets)));
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

    private void clearDiagnostics(ExceptionStatus exceptionStatus) {
        switch (exceptionStatus) {
            case TREE_PARSER_FAILED:
                addAllDiagnostics(treeParser.diagnostics());
                treeParser.resetDiagnostics();
                break;
            case SNIPPET_FAILED:
                addAllDiagnostics(treeParser.diagnostics());
                addAllDiagnostics(snippetFactory.diagnostics());
                snippetFactory.resetDiagnostics();
                treeParser.resetDiagnostics();
                break;
            default:
                addAllDiagnostics(treeParser.diagnostics());
                addAllDiagnostics(snippetFactory.diagnostics());
                addAllDiagnostics(invoker.diagnostics());
                snippetFactory.resetDiagnostics();
                treeParser.resetDiagnostics();
                invoker.resetDiagnostics();
                break;
        }
    }
}
