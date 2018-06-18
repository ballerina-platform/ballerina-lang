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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.ArrayType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * @since 0.94
 */
public class BArrayType extends BType implements ArrayType {

    public BType eType;

    public int size = -1; // Default to unsealed

    public BArrayType(BType elementType) {
        super(TypeTags.ARRAY, null);
        this.eType = elementType;
    }

    public BArrayType(BType elementType, BTypeSymbol tsymbol) {
        super(TypeTags.ARRAY, tsymbol);
        this.eType = elementType;
    }

    public BArrayType(BType elementType, BTypeSymbol tsymbol, int size) {
        super(TypeTags.ARRAY, tsymbol);
        this.eType = elementType;
        this.size = size; // -2 value to infer size from expression
    }

    public String getDesc() {
        return TypeDescriptor.SIG_ARRAY + size + ";" + eType.getDesc();
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public BType getElementType() {
        return eType;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.ARRAY;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(eType.toString());
        if (sb.indexOf("[") != -1) {
            return size != -1 ?
                    sb.insert(sb.indexOf("["), "[" + size + "]").toString() :
                    sb.insert(sb.indexOf("["), "[]").toString();
        } else {
            return size != -1 ? sb.append("[").append(size).append("]").toString() : sb.append("[]").toString();
        }
    }
}
