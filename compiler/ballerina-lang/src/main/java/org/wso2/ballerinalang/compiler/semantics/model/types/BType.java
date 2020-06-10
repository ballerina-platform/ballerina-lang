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

import org.ballerinalang.model.Name;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.ValueType;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import static org.wso2.ballerinalang.compiler.util.TypeTags.BOOLEAN;
import static org.wso2.ballerinalang.compiler.util.TypeTags.BYTE;
import static org.wso2.ballerinalang.compiler.util.TypeTags.DECIMAL;
import static org.wso2.ballerinalang.compiler.util.TypeTags.ERROR;
import static org.wso2.ballerinalang.compiler.util.TypeTags.FLOAT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.INT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.NEVER;
import static org.wso2.ballerinalang.compiler.util.TypeTags.NIL;
import static org.wso2.ballerinalang.compiler.util.TypeTags.READONLY;
import static org.wso2.ballerinalang.compiler.util.TypeTags.STRING;
import static org.wso2.ballerinalang.compiler.util.TypeTags.TYPEDESC;

/**
 * @since 0.94
 */
public class BType implements ValueType {

    public int tag;
    public BTypeSymbol tsymbol;

    // Add name, flag fields to hold typeparam data.
    // TypeParam is a part of the TSymbol, but not part of the type. We have to add this because,
    // Current compiler use both types (for built-in types) and TSymbols for semantic analysis. Because of this,
    // sometimes we loose type param information down the line. which is a problem.
    // TODO: Refactor this after JBallerina 1.0.
    public Name name;
    public int flags;

    public BType(int tag, BTypeSymbol tsymbol) {
        this.tag = tag;
        this.tsymbol = tsymbol;
        this.name = Names.EMPTY;
        this.flags = 0;
    }

    public BType(int tag, BTypeSymbol tsymbol, int flags) {
        this.tag = tag;
        this.tsymbol = tsymbol;
        this.name = Names.EMPTY;
        this.flags = flags;
    }

    public BType(int tag, BTypeSymbol tsymbol, Name name, int flags) {
        this.tag = tag;
        this.tsymbol = tsymbol;
        this.name = name;
        this.flags = flags;
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
            case BYTE:
                return TypeKind.BYTE;
            case FLOAT:
                return TypeKind.FLOAT;
            case DECIMAL:
                return TypeKind.DECIMAL;
            case STRING:
                return TypeKind.STRING;
            case BOOLEAN:
                return TypeKind.BOOLEAN;
            case TYPEDESC:
                return TypeKind.TYPEDESC;
            case NIL:
                return TypeKind.NIL;
            case NEVER:
                return TypeKind.NEVER;
            case ERROR:
                return TypeKind.ERROR;
            case READONLY:
                return TypeKind.READONLY;
            default:
                return TypeKind.OTHER;
        }
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getKind().typeName();
    }

    protected String getQualifiedTypeName() {
        return tsymbol.pkgID.toString() + ":" + tsymbol.name;
    }

    /**
     * A data holder to hold the type associated with an expression.
     */
    public static class NarrowedTypes {
        public BType trueType;
        public BType falseType;

        public NarrowedTypes(BType trueType, BType falseType) {
            this.trueType = trueType;
            this.falseType = falseType;
        }

        @Override
        public String toString() {
            return "(" + trueType + ", " + falseType + ")";
        }
    }

    public boolean isAnydata() {
        return this.tag <= TypeTags.ANYDATA;
    }

    public boolean isPureType() {
        return this.tag == TypeTags.ERROR || this.isAnydata();
    }
}
