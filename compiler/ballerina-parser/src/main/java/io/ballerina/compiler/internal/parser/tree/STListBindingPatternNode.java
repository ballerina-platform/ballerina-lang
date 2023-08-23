/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.internal.parser.tree;

import io.ballerina.compiler.syntax.tree.ListBindingPatternNode;
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
public class STListBindingPatternNode extends STBindingPatternNode {
    public final STNode openBracket;
    public final STNode bindingPatterns;
    public final STNode closeBracket;

    STListBindingPatternNode(
            STNode openBracket,
            STNode bindingPatterns,
            STNode closeBracket) {
        this(
                openBracket,
                bindingPatterns,
                closeBracket,
                Collections.emptyList());
    }

    STListBindingPatternNode(
            STNode openBracket,
            STNode bindingPatterns,
            STNode closeBracket,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.LIST_BINDING_PATTERN, diagnostics);
        this.openBracket = openBracket;
        this.bindingPatterns = bindingPatterns;
        this.closeBracket = closeBracket;

        addChildren(
                openBracket,
                bindingPatterns,
                closeBracket);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STListBindingPatternNode(
                this.openBracket,
                this.bindingPatterns,
                this.closeBracket,
                diagnostics);
    }

    public STListBindingPatternNode modify(
            STNode openBracket,
            STNode bindingPatterns,
            STNode closeBracket) {
        if (checkForReferenceEquality(
                openBracket,
                bindingPatterns,
                closeBracket)) {
            return this;
        }

        return new STListBindingPatternNode(
                openBracket,
                bindingPatterns,
                closeBracket,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new ListBindingPatternNode(this, position, parent);
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
