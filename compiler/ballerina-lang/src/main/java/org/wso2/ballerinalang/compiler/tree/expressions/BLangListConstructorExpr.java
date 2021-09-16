/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.wso2.ballerinalang.compiler.tree.expressions;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ListConstructorExprNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeModifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The super class of all the List constructor expressions.
 *
 * @see BLangListConstructorExpr
 * @see BLangArrayLiteral
 * @see BLangJSONArrayLiteral
 * @since 1.0
 */
public class BLangListConstructorExpr extends BLangExpression implements ListConstructorExprNode {

    // BLangNodes
    public List<BLangExpression> exprs;

    // Semantic Data
    public boolean isTypedescExpr = false;
    public BType typedescType = null;

    @Override
    public NodeKind getKind() {
        return NodeKind.LIST_CONSTRUCTOR_EXPR;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R apply(BLangNodeModifier<T, R> modifier, T props) {
        return modifier.modify(this, props);
    }

    @Override
    public List<BLangExpression> getExpressions() {
        return exprs;
    }

    @Override
    public String toString() {
        return Arrays.toString(exprs.toArray());
    }

    /**
     * Implementation of Array literal.
     *
     * @since 0.94
     */
    public static class BLangArrayLiteral extends BLangListConstructorExpr {

        public BLangArrayLiteral() {
            super();
            this.exprs = new ArrayList<>();
        }

        public BLangArrayLiteral(Location pos, List<BLangExpression> exprs, BType tupleType) {
            this.pos = pos;
            this.exprs = exprs;
            this.setBType(tupleType);
        }

        @Override
        public NodeKind getKind() {
            return NodeKind.ARRAY_LITERAL_EXPR;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * Implementation of Tuple literal.
     *
     * @since 1.0
     */
    public static class BLangTupleLiteral extends BLangListConstructorExpr {

        public BLangTupleLiteral() {
            super();
            this.exprs = new ArrayList<>();
        }

        public BLangTupleLiteral(Location pos, List<BLangExpression> exprs, BType tupleType) {
            this.pos = pos;
            this.exprs = exprs;
            this.setBType(tupleType);
        }

        @Override
        public NodeKind getKind() {
            return NodeKind.TUPLE_LITERAL_EXPR;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * Implementation of ArrayLiteralNode.
     *
     * @since 0.94
     */
    public static class BLangJSONArrayLiteral extends BLangArrayLiteral {

        public BLangJSONArrayLiteral(List<BLangExpression> exprs, BType jsonType) {
            this.exprs = exprs;
            this.setBType(jsonType);
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }
    }
}
