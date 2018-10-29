/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TupleVariableNode;
import org.ballerinalang.model.tree.VariableNode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a tuple variable node.
 * Example:
 *      (string, int, float) (s, i, f) = ("Foo", 12, 4.5);
 *      ((string, boolean), int) ((s, b), i) = expr;
 *
 * @since 0.985.0
 */
public class BLangTupleVariable extends BLangVariable implements TupleVariableNode {

    public List<BLangVariable> memberVariables;

    public BLangTupleVariable() {
        this.annAttachments = new ArrayList<>();
        this.flagSet = EnumSet.noneOf(Flag.class);
        this.deprecatedAttachments = new ArrayList<>();
        this.memberVariables = new ArrayList<>();
    }

    @Override
    public List<BLangVariable> getVariables() {
        return memberVariables;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void addVariable(VariableNode variable) {
        this.memberVariables.add((BLangVariable) variable);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.TUPLE_VARIABLE;
    }

    @Override
    public String toString() {
        return "(" + memberVariables.stream().map(BLangVariable::toString).collect(Collectors.joining(",")) + ") " +
                (expr != null ? " = " + String.valueOf(expr) : "");
    }
}
