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

import io.ballerina.shell.invoker.ShellSnippetsInvoker;
import io.ballerina.shell.invoker.classload.ClassLoadInvoker;
import io.ballerina.shell.parser.TreeParser;
import io.ballerina.shell.parser.TrialTreeParser;
import io.ballerina.shell.preprocessor.Preprocessor;
import io.ballerina.shell.preprocessor.SeparatorPreprocessor;
import io.ballerina.shell.snippet.factory.BasicSnippetFactory;
import io.ballerina.shell.snippet.factory.SnippetFactory;

import java.util.Objects;

/**
 * Builder for the evaluator.
 * Allows to change the components used to evaluation.
 * By default this will use {@link SeparatorPreprocessor}, {@link TrialTreeParser},
 * {@link BasicSnippetFactory} and {@link ClassLoadInvoker}.
 *
 * @since 2.0.0
 */
public class EvaluatorBuilder {
    private Preprocessor preprocessor;
    private TreeParser treeParser;
    private SnippetFactory snippetFactory;
    private ShellSnippetsInvoker invoker;

    public Evaluator build() {
        preprocessor = Objects.requireNonNullElseGet(preprocessor, SeparatorPreprocessor::new);
        treeParser = Objects.requireNonNullElseGet(treeParser, TrialTreeParser::defaultParser);
        snippetFactory = Objects.requireNonNullElseGet(snippetFactory, BasicSnippetFactory::new);
        invoker = Objects.requireNonNullElseGet(invoker, ClassLoadInvoker::new);
        return new Evaluator(preprocessor, treeParser, snippetFactory, invoker);
    }

    public EvaluatorBuilder preprocessor(Preprocessor preprocessor) {
        this.preprocessor = preprocessor;
        return this;
    }

    public EvaluatorBuilder treeParser(TreeParser treeParser) {
        this.treeParser = treeParser;
        return this;
    }

    public EvaluatorBuilder snippetFactory(SnippetFactory snippetFactory) {
        this.snippetFactory = snippetFactory;
        return this;
    }

    public EvaluatorBuilder invoker(ShellSnippetsInvoker invoker) {
        this.invoker = invoker;
        return this;
    }
}
