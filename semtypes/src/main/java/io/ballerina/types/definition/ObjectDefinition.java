/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
 */

package io.ballerina.types.definition;

import io.ballerina.types.BasicTypeCode;
import io.ballerina.types.Bdd;
import io.ballerina.types.CellAtomicType;
import io.ballerina.types.CellSemType;
import io.ballerina.types.Core;
import io.ballerina.types.Definition;
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.subtypedata.BddNode;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static io.ballerina.types.BasicTypeCode.BT_OBJECT;
import static io.ballerina.types.Core.createBasicSemType;
import static io.ballerina.types.Core.union;
import static io.ballerina.types.PredefinedType.basicSubtype;
import static io.ballerina.types.RecAtom.createDistinctRecAtom;
import static io.ballerina.types.subtypedata.CellSubtype.cellContaining;
import static io.ballerina.types.typeops.BddCommonOps.bddAtom;

/**
 * Represent object type desc.
 *
 * @since 2201.12.0
 */
public final class ObjectDefinition implements Definition {

    private final MappingDefinition mappingDefinition = new MappingDefinition();

    public static SemType distinct(int distinctId) {
        assert distinctId >= 0;
        BddNode bdd = bddAtom(createDistinctRecAtom(-distinctId - 1));
        return basicSubtype(BT_OBJECT, bdd);
    }

    // Each object type is represented as mapping type (with its basic type set to object) as fallows
    // {
    //   "$qualifiers": {
    //     boolean isolated,
    //     "client"|"service" network
    //   },
    //    [field_name]: {
    //      "field"|"method" kind,
    //      "public"|"private" visibility,
    //       VAL value;
    //    }
    //    ...{
    //      "field" kind,
    //      "public"|"private" visibility,
    //       VAL value;
    //    } | {
    //       "method" kind,
    //       "public"|"private" visibility,
    //       FUNCTION value;
    //    }
    // }
    public SemType define(Env env, ObjectQualifiers qualifiers, Collection<Member> members) {
        assert validataMembers(members); // This should never happen, so let's not run this in production
        CellAtomicType.CellMutability mut = qualifiers.readonly() ? CellAtomicType.CellMutability.CELL_MUT_NONE :
                CellAtomicType.CellMutability.CELL_MUT_LIMITED;
        Stream<CellField> memberStream = members.stream()
                .map(member -> memberField(env, member, mut));
        Stream<CellField> qualifierStream = Stream.of(qualifiers.field(env));
        SemType mappingType = mappingDefinition.define(env, Stream.concat(memberStream, qualifierStream).toList(),
                restMemberType(env, mut, qualifiers.readonly()));
        return objectContaining(mappingType);
    }

    private static boolean validataMembers(Collection<Member> members) {
        // Check if there are two members with same name
        return members.stream().map(Member::name).distinct().count() == members.size();
    }

    private SemType objectContaining(SemType mappingType) {
        SubtypeData bdd = Core.subtypeData(mappingType, BasicTypeCode.BT_MAPPING);
        assert bdd instanceof Bdd;
        return createBasicSemType(BT_OBJECT, bdd);
    }

    private CellSemType restMemberType(Env env, CellAtomicType.CellMutability mut, boolean immutable) {
        MappingDefinition fieldDefn = new MappingDefinition();
        SemType fieldMemberType = fieldDefn.defineMappingTypeWrapped(
                env,
                List.of(
                        new Field("value", immutable ? PredefinedType.VAL_READONLY : PredefinedType.VAL,
                                immutable, false),
                        Member.Kind.Field.field(),
                        Member.Visibility.ALL
                ),
                PredefinedType.NEVER);

        MappingDefinition methodDefn = new MappingDefinition();
        SemType methodMemberType = methodDefn.defineMappingTypeWrapped(
                env,
                List.of(
                        new Field("value", PredefinedType.FUNCTION, true, false),
                        Member.Kind.Method.field(),
                        Member.Visibility.ALL
                ),
                PredefinedType.NEVER);
        return cellContaining(env, union(fieldMemberType, methodMemberType), mut);
    }

    private static CellField memberField(Env env, Member member, CellAtomicType.CellMutability mut) {
        MappingDefinition md = new MappingDefinition();
        CellAtomicType.CellMutability fieldMut = member.immutable() ? CellAtomicType.CellMutability.CELL_MUT_NONE : mut;
        SemType semtype = md.defineMappingTypeWrapped(
                env,
                List.of(
                        new Field("value", member.valueTy(), member.immutable(), false),
                        member.kind().field(),
                        member.visibility().field()
                ),
                PredefinedType.NEVER);
        return CellField.from(member.name(), cellContaining(env, semtype, fieldMut));
    }

    @Override
    public SemType getSemType(Env env) {
        return objectContaining(mappingDefinition.getSemType(env));
    }
}
