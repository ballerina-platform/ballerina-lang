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
package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.clauses.OnFailClauseNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.BlockStatementNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;

/**
 * Implementation of "on-fail" clause statement.
 *
 * @since Swan Lake
 */
public class BLangOnFailClause extends BLangNode implements OnFailClauseNode {

    public BLangBlockStmt body;
    public VariableDefinitionNode variableDefinitionNode;
    public BType varType;
    public boolean isDeclaredWithVar;
    public boolean statementBlockReturns;
    public boolean bodyContainsFail;

    public BLangOnFailClause() {
    }

    @Override
    public void setDeclaredWithVar() {
        this.isDeclaredWithVar = true;
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

    @Override
    public BLangBlockStmt getBody() {
        return body;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ON_FAIL;
    }

    @Override
    public void setBody(BlockStatementNode body) {
        this.body = (BLangBlockStmt) body;
    }

    @Override
    public String toString() {
        return "on fail {" + body + "} ";
    }
}
