/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation.engine.expression;

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;

/**
 * Query expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class QueryExpressionEvaluator extends ExpressionAsProgramEvaluator {

    public QueryExpressionEvaluator(EvaluationContext context, QueryExpressionNode queryExpressionNode) {
        super(context, getQueryWithTypeCasts(queryExpressionNode));
    }

    private static ExpressionNode getQueryWithTypeCasts(QueryExpressionNode syntaxNode) {
        if (syntaxNode.parent() != null && syntaxNode.parent().kind() == SyntaxKind.TYPE_CAST_EXPRESSION) {
            // Since Ballerina query expression return type might depend on the contextual type, we need to inject
            // any existing type castings o top of the query expressions (expression will results in semantic errors
            // otherwise.)
            return (TypeCastExpressionNode) syntaxNode.parent();
        } else {
            return syntaxNode;
        }
    }
}
