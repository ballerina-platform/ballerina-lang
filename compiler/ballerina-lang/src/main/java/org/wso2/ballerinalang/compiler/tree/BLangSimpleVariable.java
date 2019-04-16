/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.SimpleVariableNode;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Represents a simple variable.
 * Example:
 *      int a = 3;
 *      float b;
 *      Foo f = expr;
 *
 * @since 0.985.0
 */
public class BLangSimpleVariable extends BLangVariable implements SimpleVariableNode {

    public BLangIdentifier name;

    public BLangSimpleVariable() {
        this.annAttachments = new ArrayList<>();
        this.flagSet = EnumSet.noneOf(Flag.class);
        this.deprecatedAttachments = new ArrayList<>();
    }

    @Override
    public BLangIdentifier getName() {
        return name;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setName(IdentifierNode name) {
        this.name = (BLangIdentifier) name;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.VARIABLE;
    }

    @Override
    public String toString() {
        String varName = "_";
        if (symbol != null && symbol.name != null) {
            varName = symbol.name.value;
        }
        return String.valueOf(type) + " " + varName + (expr != null ? " = " + String.valueOf(expr) : "");
    }
}
