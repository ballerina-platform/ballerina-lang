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

import io.ballerina.types.ComplexSemType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import io.ballerina.types.subtypedata.BooleanSubtype;
import io.ballerina.types.subtypedata.DecimalSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.StringSubtype;
import org.ballerinalang.model.types.ReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.Optional;
import java.util.StringJoiner;

import static io.ballerina.types.Core.getComplexSubtypeData;
import static io.ballerina.types.Core.singleShape;
import static io.ballerina.types.SemTypes.isSubtypeSimple;
import static io.ballerina.types.BasicTypeCode.BT_BOOLEAN;
import static io.ballerina.types.BasicTypeCode.BT_DECIMAL;
import static io.ballerina.types.BasicTypeCode.BT_FLOAT;
import static io.ballerina.types.BasicTypeCode.BT_INT;
import static io.ballerina.types.BasicTypeCode.BT_STRING;

/**
 * {@code BFiniteType} represents the finite type in Ballerina.
 *
 */
public class BFiniteType extends BType implements ReferenceType {

    public SemNamedType[] valueSpace;

    public BFiniteType(BTypeSymbol tsymbol, SemNamedType[] valueSpace) {
        super(TypeTags.FINITE, tsymbol, Flags.READONLY);
        this.valueSpace = valueSpace;
        assert validValueSpace(valueSpace);
    }

    public static BFiniteType newSingletonBFiniteType(BTypeSymbol tsymbol, SemType singletonSemType) {
        return new BFiniteType(tsymbol, new SemNamedType[]{
                new SemNamedType(singletonSemType, Optional.empty())
        });
    }

    private boolean validValueSpace(SemNamedType[] valueSpace) {
        for (SemNamedType semNamedType : valueSpace) {
            if (singleShape(semNamedType.semType()).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.FINITE;
    }

    @Override
    public SemType semType() {
        if (this.semType == null) {
            this.semType = computeResultantSemType(valueSpace);
        }
        return this.semType;
    }

    private SemType computeResultantSemType(SemNamedType[] valueSpace) {
        SemType s = PredefinedType.NEVER;
        for (SemNamedType semNamedType : valueSpace) {
            s = SemTypes.union(s, semNamedType.semType());
        }
        return s;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent") // xxxSubtypeSingleValue() are guaranteed to have a value
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("|");
        for (SemNamedType semNamedType : valueSpace) {
            SemType semType = semNamedType.semType();
            Optional<String> name = semNamedType.optName();

            if (PredefinedType.NIL.equals(semType)) {
                joiner.add(name.orElse(Names.NIL_VALUE.value));
                continue;
            }

            ComplexSemType cs = (ComplexSemType) semType;
            if (isSubtypeSimple(semType, PredefinedType.BOOLEAN)) {
                joiner.add(name.orElse(
                        BooleanSubtype.booleanSubtypeSingleValue(getComplexSubtypeData(cs, BT_BOOLEAN)).get() ?
                                Names.TRUE.value : Names.FALSE.value
                ));
            } else if (isSubtypeSimple(semType, PredefinedType.INT)) {
                joiner.add(name.orElse(
                        Long.toString(IntSubtype.intSubtypeSingleValue(getComplexSubtypeData(cs, BT_INT)).get())
                ));
            } else if (isSubtypeSimple(semType, PredefinedType.FLOAT)) {
                joiner.add(name.orElse(
                        Double.toString(FloatSubtype.floatSubtypeSingleValue(getComplexSubtypeData(cs, BT_FLOAT)).get())
                ) + "f");
            } else if (isSubtypeSimple(semType, PredefinedType.DECIMAL)) {
                joiner.add(name.orElse(
                        DecimalSubtype.decimalSubtypeSingleValue(getComplexSubtypeData(cs, BT_DECIMAL)).get()
                                .toPlainString()
                ) + "d");
            } else if (isSubtypeSimple(semType, PredefinedType.STRING)) {
                joiner.add("\"" + name.orElse(
                        StringSubtype.stringSubtypeSingleValue(getComplexSubtypeData(cs, BT_STRING)).get()
                ) + "\"");
            } else {
                throw new IllegalStateException("Unexpected value space type in BFiniteType: " + semType);
            }
        }

        return joiner.toString();
    }
}
