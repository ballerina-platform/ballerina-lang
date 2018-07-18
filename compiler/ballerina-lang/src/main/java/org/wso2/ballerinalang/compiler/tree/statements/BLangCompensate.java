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

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.CompensateNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;

/**
 * Implementation of Compensate statement.
 * @since 0.980.0
 */
public class BLangCompensate extends BLangStatement implements CompensateNode {

    public IdentifierNode scopeName;

    public BLangInvocation invocation;

    public BLangCompensate() {
        invocation = (BLangInvocation) TreeBuilder.createInvocationNode();
    }

    public BLangCompensate(IdentifierNode scopeName) {
        invocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        this.scopeName = scopeName;
    }

    public IdentifierNode getScopeName() {
        return scopeName;
    }

    public void setScopeName(IdentifierNode scopeName) {
        this.scopeName = scopeName;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.COMPENSATE;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "Compensate";
    }
}
