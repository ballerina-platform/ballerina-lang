/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.clauses.InputClauseNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

/**
 * Abstract implementation of from/join clause statements.
 *
 * @since 1.3.0
 */
public abstract class BLangInputClause extends BLangNode implements InputClauseNode {

    // BLangNodes
    public BLangExpression collection;
    public VariableDefinitionNode variableDefinitionNode;

    // Parser Flags and Data
    public boolean isDeclaredWithVar;

    // Semantic Data
    public BType varType; // T
    public BType resultType; // map<T>
    public BType nillableResultType; // map<T>?

    @Override
    public ExpressionNode getCollection() {
        return collection;
    }

    @Override
    public void setCollection(ExpressionNode collection) {
        this.collection = (BLangExpression) collection;
    }

    @Override
    public boolean setDeclaredWithVar() {
        return false;
    }

    @Override
    public boolean isDeclaredWithVar() {
        return isDeclaredWithVar;
    }

    @Override
    public VariableDefinitionNode getVariableDefinitionNode() {
        return variableDefinitionNode;
    }

    @Override
    public void setVariableDefinitionNode(VariableDefinitionNode variableDefinitionNode) {
        this.variableDefinitionNode = variableDefinitionNode;
    }

}
