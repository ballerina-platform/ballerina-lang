/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.NamedArgBindingPatternNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STNamedArgBindingPatternNode extends STBindingPatternNode {
    public final STNode argName;
    public final STNode equalsToken;
    public final STNode bindingPattern;

    STNamedArgBindingPatternNode(
            STNode argName,
            STNode equalsToken,
            STNode bindingPattern) {
        this(
                argName,
                equalsToken,
                bindingPattern,
                Collections.emptyList());
    }

    STNamedArgBindingPatternNode(
            STNode argName,
            STNode equalsToken,
            STNode bindingPattern,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.NAMED_ARG_BINDING_PATTERN, diagnostics);
        this.argName = argName;
        this.equalsToken = equalsToken;
        this.bindingPattern = bindingPattern;

        addChildren(
                argName,
                equalsToken,
                bindingPattern);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STNamedArgBindingPatternNode(
                this.argName,
                this.equalsToken,
                this.bindingPattern,
                diagnostics);
    }

    public STNamedArgBindingPatternNode modify(
            STNode argName,
            STNode equalsToken,
            STNode bindingPattern) {
        if (checkForReferenceEquality(
                argName,
                equalsToken,
                bindingPattern)) {
            return this;
        }

        return new STNamedArgBindingPatternNode(
                argName,
                equalsToken,
                bindingPattern,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new NamedArgBindingPatternNode(this, position, parent);
    }

    @Override
    public void accept(STNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(STNodeTransformer<T> transformer) {
        return transformer.transform(this);
    }
}
