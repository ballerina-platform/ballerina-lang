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
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

/**
 * @since 0.94
 */
public class BArrayType extends BType implements ArrayType {

    private static final String SEMI_COLON = ";";

    public BType eType;
    public BIntersectionType immutableType;

    public int size = -1;

    public BArrayState state = BArrayState.UNSEALED;

    public BArrayType(BType elementType) {
        super(TypeTags.ARRAY, null);
        this.eType = elementType;
    }

    public BArrayType(BType elementType, BTypeSymbol tsymbol) {
        super(TypeTags.ARRAY, tsymbol);
        this.eType = elementType;
    }

    public BArrayType(BType elementType, BTypeSymbol tsymbol, int size, BArrayState state) {
        super(TypeTags.ARRAY, tsymbol);
        this.eType = elementType;
        this.size = size;
        this.state = state;
    }

    public BArrayType(BType elementType, BTypeSymbol tsymbol, int size, BArrayState state, int flags) {
        super(TypeTags.ARRAY, tsymbol, flags);
        this.eType = elementType;
        this.size = size;
        this.state = state;
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
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(eType.toString());
        String tempSize = (state == BArrayState.OPEN_SEALED) ? "*" : String.valueOf(size);
        if (eType.tag == TypeTags.ARRAY) {
            if (state != BArrayState.UNSEALED) {
                sb.insert(sb.indexOf("["), "[" + tempSize + "]");
            } else {
                sb.insert(sb.indexOf("["), "[]");
            }
        } else {
            if (state != BArrayState.UNSEALED) {
                sb.append("[").append(tempSize).append("]");
            } else {
                sb.append("[]");
            }
        }
        return !Symbols.isFlagOn(flags, Flags.READONLY) ? sb.toString() : sb.append(" & readonly").toString();
    }

    @Override
    public final boolean isAnydata() {
        return this.eType.isPureType();
    }

    @Override
    public BIntersectionType getImmutableType() {
        return this.immutableType;
    }
}
