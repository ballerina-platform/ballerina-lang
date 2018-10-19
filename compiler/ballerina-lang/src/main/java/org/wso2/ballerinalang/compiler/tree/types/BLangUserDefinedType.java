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
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * @since 0.94
 */
public class BLangUserDefinedType extends BLangType implements UserDefinedTypeNode {
    public BLangIdentifier pkgAlias;
    public BLangIdentifier typeName;
    public Set<Flag> flagSet;

    public BLangUserDefinedType() {
        this.flagSet = new HashSet<>();
    }

    public BLangUserDefinedType(BLangIdentifier pkgAlias, BLangIdentifier typeName) {
        this.pkgAlias = pkgAlias;
        this.typeName = typeName;
        this.flagSet = new HashSet<>();
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
    public String toString() {
        if (pkgAlias == null || pkgAlias.value.isEmpty()) {
            return typeName.value;
        }
        return pkgAlias.value + ":" + typeName.value;
    }
}
