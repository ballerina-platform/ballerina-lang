/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
 *
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.BddAllOrNothing;
import io.ballerina.runtime.api.types.semtype.BddNode;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.MappingAtomicType;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_MAPPING;
import static io.ballerina.runtime.api.types.semtype.Core.diff;
import static io.ballerina.runtime.api.types.semtype.Core.getComplexSubtypeData;
import static io.ballerina.runtime.api.types.semtype.Core.isNothingSubtype;
import static io.ballerina.runtime.api.types.semtype.Core.stringSubtype;

public final class BMappingProj {

    private BMappingProj() {
    }

    public static SemType mappingMemberTypeInnerVal(Context cx, SemType t, SemType k) {
        return diff(mappingMemberTypeInner(cx, t, k), Builder.undef());
    }

    // This computes the spec operation called "member type of K in T",
    // for when T is a subtype of mapping, and K is either `string` or a singleton string.
    // This is what Castagna calls projection.
    public static SemType mappingMemberTypeInner(Context cx, SemType t, SemType k) {
        if (t.some() == 0) {
            return (t.all() & Builder.mappingType().all()) != 0 ? Builder.valType() : Builder.undef();
        } else {
            SubTypeData keyData = stringSubtype(k);
            if (isNothingSubtype(keyData)) {
                return Builder.undef();
            }
            return bddMappingMemberTypeInner(cx, (Bdd) getComplexSubtypeData(t, BT_MAPPING), keyData,
                    Builder.inner());
        }
    }

    static SemType bddMappingMemberTypeInner(Context cx, Bdd b, SubTypeData key, SemType accum) {
        if (b instanceof BddAllOrNothing allOrNothing) {
            return allOrNothing.isAll() ? accum : Builder.neverType();
        } else {
            BddNode bdd = (BddNode) b;
            return Core.union(
                    bddMappingMemberTypeInner(cx, bdd.left(), key,
                            Core.intersect(mappingAtomicMemberTypeInner(cx.mappingAtomType(bdd.atom()), key),
                                    accum)),
                    Core.union(bddMappingMemberTypeInner(cx, bdd.middle(), key, accum),
                            bddMappingMemberTypeInner(cx, bdd.right(), key, accum)));
        }
    }

    static SemType mappingAtomicMemberTypeInner(MappingAtomicType atomic, SubTypeData key) {
        SemType memberType = null;
        for (SemType ty : mappingAtomicApplicableMemberTypesInner(atomic, key)) {
            if (memberType == null) {
                memberType = ty;
            } else {
                memberType = Core.union(memberType, ty);
            }
        }
        return memberType == null ? Builder.undef() : memberType;
    }

    static List<SemType> mappingAtomicApplicableMemberTypesInner(MappingAtomicType atomic, SubTypeData key) {
        List<SemType> types = new ArrayList<>(atomic.types().length);
        for (SemType t : atomic.types()) {
            types.add(Core.cellInner(t));
        }

        List<SemType> memberTypes = new ArrayList<>();
        SemType rest = Core.cellInner(atomic.rest());
        if (isAllSubtype(key)) {
            memberTypes.addAll(types);
            memberTypes.add(rest);
        } else {
            BStringSubType.StringSubtypeListCoverage coverage =
                    ((BStringSubType.StringSubTypeData) key).stringSubtypeListCoverage(atomic.names());
            for (int index : coverage.indices()) {
                memberTypes.add(types.get(index));
            }
            if (!coverage.isSubType()) {
                memberTypes.add(rest);
            }
        }
        return memberTypes;
    }

    static boolean isAllSubtype(SubTypeData d) {
        return d == AllOrNothing.ALL;
    }
}
