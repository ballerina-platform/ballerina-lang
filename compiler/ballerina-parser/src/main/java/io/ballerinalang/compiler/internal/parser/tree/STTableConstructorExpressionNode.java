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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.TableConstructorExpressionNode;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STTableConstructorExpressionNode extends STExpressionNode {
    public final STNode tableKeyword;
    public final STNode keySpecifier;
    public final STNode openBracket;
    public final STNode mappingConstructors;
    public final STNode closeBracket;

    STTableConstructorExpressionNode(
            STNode tableKeyword,
            STNode keySpecifier,
            STNode openBracket,
            STNode mappingConstructors,
            STNode closeBracket) {
        this(
                tableKeyword,
                keySpecifier,
                openBracket,
                mappingConstructors,
                closeBracket,
                Collections.emptyList());
    }

    STTableConstructorExpressionNode(
            STNode tableKeyword,
            STNode keySpecifier,
            STNode openBracket,
            STNode mappingConstructors,
            STNode closeBracket,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.TABLE_CONSTRUCTOR, diagnostics);
        this.tableKeyword = tableKeyword;
        this.keySpecifier = keySpecifier;
        this.openBracket = openBracket;
        this.mappingConstructors = mappingConstructors;
        this.closeBracket = closeBracket;

        addChildren(
                tableKeyword,
                keySpecifier,
                openBracket,
                mappingConstructors,
                closeBracket);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STTableConstructorExpressionNode(
                this.tableKeyword,
                this.keySpecifier,
                this.openBracket,
                this.mappingConstructors,
                this.closeBracket,
                diagnostics);
    }

    public STTableConstructorExpressionNode modify(
            STNode tableKeyword,
            STNode keySpecifier,
            STNode openBracket,
            STNode mappingConstructors,
            STNode closeBracket) {
        if (checkForReferenceEquality(
                tableKeyword,
                keySpecifier,
                openBracket,
                mappingConstructors,
                closeBracket)) {
            return this;
        }

        return new STTableConstructorExpressionNode(
                tableKeyword,
                keySpecifier,
                openBracket,
                mappingConstructors,
                closeBracket,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new TableConstructorExpressionNode(this, position, parent);
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
