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
package org.wso2.ballerinalang.compiler.tree.types;

import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.types.ObjectTypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code BLangObjectTypeNode} represents a object type node in Ballerina
 * <p>
 * e.g. object { public { int a; } private { string name; }};
 *
 * @since 0.971.0
 */
public class BLangObjectTypeNode extends BLangType implements ObjectTypeNode {

    public List<BLangVariable> fields;
    public List<BLangFunction> functions;
    public BLangFunction initFunction;
    public BLangVariable receiver;
    public boolean isAnonymous;
    public boolean isFieldAnalyseRequired;

    public BSymbol symbol;

    public BLangObjectTypeNode() {
        this.fields = new ArrayList<>();
        this.functions = new ArrayList<>();
    }

    @Override
    public List<BLangVariable> getFields() {
        return fields;
    }

    @Override
    public void addField(VariableNode var) {
        this.getFields().add((BLangVariable) var);
    }

    @Override
    public List<BLangFunction> getFunctions() {
        return functions;
    }

    @Override
    public void addFunction(FunctionNode function) {
        this.functions.add((BLangFunction) function);
    }

    @Override
    public FunctionNode getInitFunction() {
        return initFunction;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.OBJECT_TYPE;
    }

    @Override
    public String toString() {
        //TODO fix
        return "object { " + this.fields + " }";
    }

}
