/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.debugadapter.completions;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.debugadapter.SuspendedContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Ballerina debug completion context.
 *
 * @since 2.0.0
 */
public class CompletionsContext {

    private final SuspendedContext suspendedContext;
    private final List<Node> resolverChain = new ArrayList<>();

    public CompletionsContext(SuspendedContext suspendedContext) {
        this.suspendedContext = suspendedContext;
    }

    public void addResolver(Node node) {
        this.resolverChain.add(node);
    }

    public List<Node> getResolverChain() {
        return this.resolverChain;
    }

    public List<Symbol> visibleSymbols(int line, int offset) {
        SemanticModel semanticContext = suspendedContext.getDebugCompiler().getSemanticInfo();
        return semanticContext.visibleSymbols(suspendedContext.getDocument(), LinePosition.from(line, offset));
    }

    public Optional<SemanticModel> currentSemanticModel() {
        return Optional.ofNullable(suspendedContext.getDebugCompiler().getSemanticInfo());
    }

    public SuspendedContext getSuspendedContext() {
        return suspendedContext;
    }
}
