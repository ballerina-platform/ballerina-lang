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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ErrorConstructorExpressionNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.NamedArgNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeModifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;

import java.util.List;
import java.util.StringJoiner;

/**
 * Implementation of error-constructor-expr.
 *
 * @since 2.0.0
 */
public class BLangErrorConstructorExpr extends BLangExpression implements ErrorConstructorExpressionNode {

    // BLangNodes
    public BLangUserDefinedType errorTypeRef;
    public List<BLangExpression> positionalArgs;
    public List<BLangNamedArgsExpression> namedArgs;

    // Semantic Data
    // This is added to store the detail mapping created from the named args and it is used only at desugar.
    public BLangExpression errorDetail;

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
    public NodeKind getKind() {
        return NodeKind.ERROR_CONSTRUCTOR_EXPRESSION;
    }

    @Override
    public List<? extends ExpressionNode> getPositionalArgs() {
        return positionalArgs;
    }

    @Override
    public List<? extends NamedArgNode> getNamedArgs() {
        return namedArgs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        StringJoiner args = new StringJoiner(", ", "(", ")");
        for (BLangExpression positionalArg : positionalArgs) {
            args.add(positionalArg.toString());
        }

        for (BLangNamedArgsExpression namedArg : namedArgs) {
            args.add(namedArg.toString());
        }

        return sb.append("error ")
                .append(errorTypeRef != null ? errorTypeRef.typeName.toString() : "")
                .append(args)
                .toString();
    }
}
