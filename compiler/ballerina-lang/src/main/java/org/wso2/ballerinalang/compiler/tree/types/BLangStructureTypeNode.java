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

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.types.StructureTypeNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code BLangStructureTypeNode} represents a structure type node in Ballerina.
 *
 * @since 0.971.0
 */
public abstract class BLangStructureTypeNode extends BLangType implements StructureTypeNode {

    public List<BLangSimpleVariable> fields;
    public BLangFunction initFunction;
    public boolean isAnonymous;
    public boolean isLocal;
    public List<BLangType> typeRefs;
    public BSymbol symbol;

    // This is a cache of the fields referred through the type references
    public List<BLangSimpleVariable> referencedFields;

    public BLangStructureTypeNode() {
        this.fields = new ArrayList<>();
        this.typeRefs = new ArrayList<>();
        this.referencedFields = new ArrayList<>();
    }

    @Override
    public List<? extends SimpleVariableNode> getFields() {
        return fields;
    }

    @Override
    public void addField(SimpleVariableNode field) {
        this.fields.add((BLangSimpleVariable) field);
    }

    @Override
    public List<? extends TypeNode> getTypeReferences() {
        return Collections.unmodifiableList(this.typeRefs);
    }

    @Override
    public void addTypeReference(TypeNode type) {
        this.typeRefs.add((BLangType) type);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.RECORD_TYPE;
    }

    @Override
    public boolean getIsAnonymous() {
        return this.isAnonymous;
    }

    @Override
    public boolean getIsLocal() {
        return this.isLocal;
    }

    @Override
    public String toString() {
        return "record { " + this.fields + " }";
    }
}
