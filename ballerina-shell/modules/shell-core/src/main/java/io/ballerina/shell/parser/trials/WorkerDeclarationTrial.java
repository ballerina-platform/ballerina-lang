/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.parser.trials;

import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarator;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.shell.parser.TrialTreeParser;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 *  Attempts to parse source as a worker declaration.
 *
 * @since 2.0.0
 */
public class WorkerDeclarationTrial extends TreeParserTrial {

    public WorkerDeclarationTrial(TrialTreeParser parentParser) {
        super(parentParser);
    }

    @Override
    public Collection<Node> parse(String source) throws ParserTrialFailedException {
        Optional<NamedWorkerDeclarator> workerDeclarator = NodeParser.parseFunctionBodyBlock(source)
                .namedWorkerDeclarator();
        if (workerDeclarator.isPresent()) {
            return Collections.singletonList(workerDeclarator.get());
        }

        throw new ParserTrialFailedException("Worker declarations not found.");
    }
}
