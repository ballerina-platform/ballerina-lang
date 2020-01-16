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
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * @since 0.94
 */
public class BArrayType extends BType implements ArrayType {

    private static final String SEMI_COLON = ";";

    public BType eType;

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

    public String getDesc() {
        if (state == BArrayState.UNSEALED) {
            return TypeDescriptor.SIG_ARRAY + -1 + SEMI_COLON + eType.getDesc();
        } else {
            return TypeDescriptor.SIG_ARRAY + size + SEMI_COLON + eType.getDesc();
        }
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
            return (state != BArrayState.UNSEALED) ?
                    sb.insert(sb.indexOf("["), "[" + tempSize + "]").toString() :
                    sb.insert(sb.indexOf("["), "[]").toString();
        } else {
            return (state != BArrayState.UNSEALED) ?
                    sb.append("[").append(tempSize).append("]").toString() : sb.append("[]").toString();
        }
    }

    @Override
    public final boolean isAnydata() {
        return this.eType.isPureType();
    }
}
