/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;

/**
 * type Foo record {
 *     int a;
 *     string b;
 * }
 *
 * Foo {a: b, c: d, !...} = {a: 5, b: "foo"};.
 *
 * @since 0.985.0
 */
public class BLangRecordVariableDef extends BLangStatement implements VariableDefinitionNode {

    public BLangRecordVariable var;

    @Override
    public BLangRecordVariable getVariable() {
        return var;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setVariable(VariableNode var) {
        this.var = (BLangRecordVariable) var;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.VARIABLE_DEF;
    }

    @Override
    public String toString() {
        return this.var.toString();
    }
}
