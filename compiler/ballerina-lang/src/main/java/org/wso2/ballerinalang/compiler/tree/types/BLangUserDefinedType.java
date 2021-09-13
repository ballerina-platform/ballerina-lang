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
package org.wso2.ballerinalang.compiler.tree.types;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.types.UserDefinedTypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * @since 0.94
 */
public class BLangUserDefinedType extends BLangType implements UserDefinedTypeNode {
    public BLangIdentifier pkgAlias;
    public BLangIdentifier typeName;
    public BSymbol symbol;

    public BLangUserDefinedType() {
        this.flagSet = new HashSet<>();
    }

    public BLangUserDefinedType(BLangIdentifier pkgAlias, BLangIdentifier typeName) {
        this.pkgAlias = pkgAlias;
        this.typeName = typeName;
    }

    @Override
    public BLangIdentifier getPackageAlias() {
        return pkgAlias;
    }

    @Override
    public BLangIdentifier getTypeName() {
        return typeName;
    }

    @Override
    public Set<? extends Flag> getFlags() {
        return flagSet;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.USER_DEFINED_TYPE;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R accept(BLangNodeTransformer<T, R> transformer, T props) {
        return transformer.transform(this, props);
    }

    @Override
    public String toString() {
        String typeName = this.typeName.value;

        BType thisType = this.getBType();
        // When there are errors in this type, we can't just use the tName.
        // Fall back to pkgAlias:typeName pattern
        if (typeName.startsWith("$") && thisType != null && thisType.getKind() != TypeKind.OTHER) {
            return thisType.toString();
        }
        if (pkgAlias == null || pkgAlias.value.isEmpty()) {
            return typeName;
        }
        return pkgAlias.value + ":" + typeName;
    }
}
