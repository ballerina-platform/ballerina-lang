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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STSpecificFieldNode extends STMappingFieldNode {
    public final STNode readonlyKeyword;
    public final STNode fieldName;
    public final STNode colon;
    public final STNode valueExpr;

    STSpecificFieldNode(
            STNode readonlyKeyword,
            STNode fieldName,
            STNode colon,
            STNode valueExpr) {
        this(
                readonlyKeyword,
                fieldName,
                colon,
                valueExpr,
                Collections.emptyList());
    }

    STSpecificFieldNode(
            STNode readonlyKeyword,
            STNode fieldName,
            STNode colon,
            STNode valueExpr,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.SPECIFIC_FIELD, diagnostics);
        this.readonlyKeyword = readonlyKeyword;
        this.fieldName = fieldName;
        this.colon = colon;
        this.valueExpr = valueExpr;

        addChildren(
                readonlyKeyword,
                fieldName,
                colon,
                valueExpr);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STSpecificFieldNode(
                this.readonlyKeyword,
                this.fieldName,
                this.colon,
                this.valueExpr,
                diagnostics);
    }

    public STSpecificFieldNode modify(
            STNode readonlyKeyword,
            STNode fieldName,
            STNode colon,
            STNode valueExpr) {
        if (checkForReferenceEquality(
                readonlyKeyword,
                fieldName,
                colon,
                valueExpr)) {
            return this;
        }

        return new STSpecificFieldNode(
                readonlyKeyword,
                fieldName,
                colon,
                valueExpr,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new SpecificFieldNode(this, position, parent);
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
