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

import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
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
public class STMappingConstructorExpressionNode extends STExpressionNode {
    public final STNode openBrace;
    public final STNode fields;
    public final STNode closeBrace;

    STMappingConstructorExpressionNode(
            STNode openBrace,
            STNode fields,
            STNode closeBrace) {
        this(
                openBrace,
                fields,
                closeBrace,
                Collections.emptyList());
    }

    STMappingConstructorExpressionNode(
            STNode openBrace,
            STNode fields,
            STNode closeBrace,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.MAPPING_CONSTRUCTOR, diagnostics);
        this.openBrace = openBrace;
        this.fields = fields;
        this.closeBrace = closeBrace;

        addChildren(
                openBrace,
                fields,
                closeBrace);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STMappingConstructorExpressionNode(
                this.openBrace,
                this.fields,
                this.closeBrace,
                diagnostics);
    }

    public STMappingConstructorExpressionNode modify(
            STNode openBrace,
            STNode fields,
            STNode closeBrace) {
        if (checkForReferenceEquality(
                openBrace,
                fields,
                closeBrace)) {
            return this;
        }

        return new STMappingConstructorExpressionNode(
                openBrace,
                fields,
                closeBrace,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new MappingConstructorExpressionNode(this, position, parent);
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
