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

import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.ValueType;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;

import static org.wso2.ballerinalang.compiler.util.TypeTags.BLOB;
import static org.wso2.ballerinalang.compiler.util.TypeTags.BOOLEAN;
import static org.wso2.ballerinalang.compiler.util.TypeTags.FLOAT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.INT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.NIL;
import static org.wso2.ballerinalang.compiler.util.TypeTags.STRING;
import static org.wso2.ballerinalang.compiler.util.TypeTags.TYPEDESC;

/**
 * @since 0.94
 */
public class BType implements ValueType {

    public int tag;
    public BTypeSymbol tsymbol;
    public int flags;

    public BType(int tag, BTypeSymbol tsymbol) {
        this.tag = tag;
        this.tsymbol = tsymbol;
    }

    public String getDesc() {
        switch (tag) {
            case INT:
                return TypeDescriptor.SIG_INT;
            case FLOAT:
                return TypeDescriptor.SIG_FLOAT;
            case STRING:
                return TypeDescriptor.SIG_STRING;
            case BOOLEAN:
                return TypeDescriptor.SIG_BOOLEAN;
            case BLOB:
                return TypeDescriptor.SIG_BLOB;
            case TYPEDESC:
                return TypeDescriptor.SIG_TYPEDESC;
            default:
                return null;
        }
    }

    public BType getReturnType() {
        return null;
    }

    public boolean isNullable() {
        return false;
    }

    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {
        switch (tag) {
            case INT:
                return TypeKind.INT;
            case FLOAT:
                return TypeKind.FLOAT;
            case STRING:
                return TypeKind.STRING;
            case BOOLEAN:
                return TypeKind.BOOLEAN;
            case BLOB:
                return TypeKind.BLOB;
            case TYPEDESC:
                return TypeKind.TYPEDESC;
            case NIL:
                return TypeKind.NIL;
            default:
                return TypeKind.OTHER;
        }
    }

    @Override
    public String toString() {
        return getKind().typeName();
    }

    protected String getQualifiedTypeName() {
        return tsymbol.pkgID.bvmAlias() + ":" + tsymbol.name;
    }
}
