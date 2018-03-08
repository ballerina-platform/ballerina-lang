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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.tree.StreamletType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

/**
 * @since 0.955.0
 */
public class BStreamletType extends BType implements StreamletType {
    public List<BType> paramTypes;

    public BStreamletType(List<BType> paramTypes, BTypeSymbol tsymbol) {
        super(TypeTags.STREAMLET, tsymbol);
        this.paramTypes = paramTypes;
    }

    public String getDesc() {
        return TypeDescriptor.SIG_STREAMLET + getQualifiedTypeName() + ";";
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.STREAMLET;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        return Names.DEFAULT_PACKAGE.equals(tsymbol.pkgID.name) ? tsymbol.name.value : getQualifiedTypeName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BStreamletType that = (BStreamletType) o;

        return paramTypes != null ? paramTypes.equals(that.paramTypes) : that.paramTypes == null;
    }

    @Override
    public int hashCode() {
        return paramTypes != null ? paramTypes.hashCode() : 0;
    }
}
