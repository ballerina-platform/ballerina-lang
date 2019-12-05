/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.clauses.PatternStreamingEdgeInputNode;
import org.ballerinalang.model.tree.clauses.WhereNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

/**
 * Implementation of {@link PatternStreamingEdgeInputNode}.
 *
 * @since 0.965.0
 */
public class BLangPatternStreamingEdgeInput extends BLangNode implements PatternStreamingEdgeInputNode {

    public BLangExpression streamRef;

    public String alias;

    public BLangExpression expressionNode;

    public BLangWhere whereNode;

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.PATTERN_STREAMING_EDGE_INPUT;
    }

    @Override
    public void setStreamReference(ExpressionNode ref) {

        this.streamRef = (BLangExpression) ref;
    }

    @Override
    public void setWhereClause(WhereNode whereNode) {

        this.whereNode = (BLangWhere) whereNode;
    }

    @Override
    public void setExpression(ExpressionNode expressionNode) {

        this.expressionNode = (BLangExpression) expressionNode;
    }

    @Override
    public ExpressionNode getStreamReference() {
        return streamRef;
    }

    @Override
    public WhereNode getWhereClause() {
        return whereNode;
    }

    @Override
    public ExpressionNode getExpression() {
        return expressionNode;
    }

    @Override
    public void setAliasIdentifier(String alias) {
        this.alias = alias;
    }

    @Override
    public String getAliasIdentifier() {
        return alias;
    }
}
