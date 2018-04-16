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

import org.ballerinalang.model.types.StructType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code BStructType} represents the type of a particular struct definition.
 * <p>
 * This class holds details of struct fields and attached functions .
 *
 * @since 0.94
 */
public class BStructType extends BType implements StructType {
    public List<BStructField> fields;

    public BStructType(BTypeSymbol tSymbol) {
        super(TypeTags.STRUCT, tSymbol);
        this.fields = new ArrayList<>();
    }

    public String getDesc() {
        return TypeDescriptor.SIG_STRUCT + getQualifiedTypeName() + ";";
    }

    @Override
    public List<BStructField> getFields() {
        return fields;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.STRUCT;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        return this.tsymbol.toString();
    }

    /**
     * A wrapper class which holds struct field name, type and the variable symbol.
     *
     * @since 0.94
     */
    public static class BStructField implements Field {
        public Name name;
        public BType type;
        public BVarSymbol symbol;

        //no need to persist below flag
        public boolean expAvailable;

        public BStructField(Name name, BVarSymbol symbol, boolean expAvailable) {
            this.name = name;
            this.symbol = symbol;
            this.type = symbol.type;
            this.expAvailable = expAvailable;
        }

        @Override
        public Name getName() {
            return name;
        }

        @Override
        public BType getType() {
            return symbol.type;
        }
    }

}
