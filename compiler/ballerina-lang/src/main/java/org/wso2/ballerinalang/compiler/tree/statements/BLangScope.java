/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
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
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.ScopeNode;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;

import java.util.Stack;

/**
 * Implementation of the Scope block.
 * @since 0.980.0
 */
public class BLangScope extends BLangStatement implements ScopeNode {

    public BLangBlockStmt scopeBody;
    public BLangLambdaFunction compensationFunction;
    public IdentifierNode name;
    public Stack<String> childScopes = new Stack<>();

    public BLangScope() {}

    public BLangScope(BLangBlockStmt scopeBody,
            BLangIdentifier name) {
        this.scopeBody = scopeBody;
        this.name = name;
    }

    public void addChildScope(String name) {
        childScopes.push(name);
    }

    @Override
    public BLangBlockStmt getScopeBody() {
        return scopeBody;
    }

    @Override
    public IdentifierNode getScopeName() {
        return name;
    }

    @Override
    public void setScopeName(IdentifierNode name) {
        this.name = name;
    }

    @Override
    public void setScopeBody(BlockNode body) {
        this.scopeBody = (BLangBlockStmt) body;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.SCOPE;
    }

    public BLangLambdaFunction getCompensationFunction() {
        return compensationFunction;
    }

    public void setCompensationFunction(BLangLambdaFunction compensationFunction) {
        this.compensationFunction = compensationFunction;
    }

    @Override
    public String toString() {
        return "scope " + this.name + " {" + this.scopeBody.toString() + "}";
    }
}
