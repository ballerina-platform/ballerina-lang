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

import io.ballerina.types.Core;
import io.ballerina.types.EnumerableCharString;
import io.ballerina.types.EnumerableDecimal;
import io.ballerina.types.EnumerableFloat;
import io.ballerina.types.EnumerableString;
import io.ballerina.types.EnumerableType;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.subtypedata.AllOrNothingSubtype;
import io.ballerina.types.subtypedata.BooleanSubtype;
import io.ballerina.types.subtypedata.CharStringSubtype;
import io.ballerina.types.subtypedata.DecimalSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.NonCharStringSubtype;
import io.ballerina.types.subtypedata.Range;
import io.ballerina.types.subtypedata.StringSubtype;
import org.ballerinalang.model.types.ReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.StringJoiner;

/**
 * {@code BFiniteType} represents the finite type in Ballerina.
 *
 */
public class BFiniteType extends BType implements ReferenceType {

    public SemType[] valueSpace;

    @Deprecated
    public BFiniteType(BTypeSymbol tsymbol, SemType semType) { // TODO: Get rid of this constructor
        this(tsymbol, semType, null, new SemType[]{semType});
    }

    public BFiniteType(BTypeSymbol tsymbol, SemType semType, String userStrRep, SemType[] valueSpace) {
        super(TypeTags.FINITE, tsymbol, semType, userStrRep);
        this.flags |= Flags.READONLY;
        this.valueSpace = valueSpace;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.FINITE;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    public String toNormalizedString() {
        StringJoiner joiner = new StringJoiner("|");
        for (SemType t : this.valueSpace) {
            if (Core.containsNil(t)) {
                joiner.add("()");
            }

            SubtypeData subtypeData = Core.booleanSubtype(t);
            if (subtypeData instanceof AllOrNothingSubtype allOrNothing) {
                if (allOrNothing.isAllSubtype()) {
                    joiner.add("true");
                    joiner.add("false");
                }
            } else {
                BooleanSubtype booleanSubtype = (BooleanSubtype) subtypeData;
                joiner.add(booleanSubtype.value ? "true" : "false");
            }

            subtypeData = Core.intSubtype(t);
            if (subtypeData instanceof IntSubtype intSubtype) {
                for (Range range : intSubtype.ranges) {
                    for (long i = range.min; i <= range.max; i++) {
                        joiner.add(Long.toString(i));
                        if (i == Long.MAX_VALUE) {
                            // To avoid overflow
                            break;
                        }
                    }
                }
            }

            subtypeData = Core.floatSubtype(t);
            if (subtypeData instanceof FloatSubtype floatSubtype) {
                for (EnumerableType enumerableFloat : floatSubtype.values()) {
                    joiner.add(((EnumerableFloat) enumerableFloat).value + "f");
                }
            }

            subtypeData = Core.decimalSubtype(t);
            if (subtypeData instanceof DecimalSubtype decimalSubtype) {
                for (EnumerableType enumerableDecimal : decimalSubtype.values()) {
                    joiner.add(((EnumerableDecimal) enumerableDecimal).value + "d");
                }
            }

            subtypeData = Core.stringSubtype(t);
            if (subtypeData instanceof StringSubtype stringSubtype) {
                CharStringSubtype charStringSubtype = stringSubtype.getChar();
                for (EnumerableType enumerableType : charStringSubtype.values()) {
                    joiner.add("\"" + ((EnumerableCharString) enumerableType).value + "\"");
                }

                NonCharStringSubtype nonCharStringSubtype = stringSubtype.getNonChar();
                for (EnumerableType enumerableType : nonCharStringSubtype.values()) {
                    joiner.add("\"" + ((EnumerableString) enumerableType).value + "\"");
                }
            }
        }

        return joiner.toString();
    }

    @Override
    public String toString() {
        if (this.userStrRep != null) {
            return userStrRep;
        }
        return toNormalizedString();
    }
}
