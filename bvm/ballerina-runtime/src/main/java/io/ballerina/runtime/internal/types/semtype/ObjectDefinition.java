/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.BasicTypeCode;
import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.BddNode;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.List;
import java.util.stream.Stream;

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_OBJECT;
import static io.ballerina.runtime.api.types.semtype.BddNode.bddAtom;
import static io.ballerina.runtime.api.types.semtype.Core.createBasicSemType;
import static io.ballerina.runtime.api.types.semtype.Core.union;
import static io.ballerina.runtime.api.types.semtype.RecAtom.createDistinctRecAtom;

/**
 * {@code Definition} used to create an object type.
 * <p>
 * Each object type is represented as mapping type (with its basic type set to object) as fallows
 * {@code { "$qualifiers": { boolean isolated, "client"|"service" network }, [field_name]: { "field"|"method" kind,
 * "public"|"private" visibility, VAL value; } ...{ "field" kind, "public"|"private" visibility, VAL value; } | {
 * "method" kind, "public"|"private" visibility, FUNCTION value; } }}
 *
 * @since 2201.11.0
 */
public class ObjectDefinition extends Definition {

    private final MappingDefinition mappingDefinition = new MappingDefinition();

    @Override
    public SemType getSemType(Env env) {
        return objectContaining(mappingDefinition.getSemType(env));
    }

    public SemType define(Env env, ObjectQualifiers qualifiers, List<Member> members,
                          CellAtomicType.CellMutability mut) {
        Stream<MappingDefinition.Field> memberStream =
                members.stream().map(member -> memberField(env, member, qualifiers.readonly()));
        Stream<MappingDefinition.Field> qualifierStream = Stream.of(qualifiers.field(env));
        SemType mappingType = mappingDefinition.define(env, Stream.concat(memberStream, qualifierStream)
                .map(field -> MappingDefinition.BCellField.from(env, field, mut))
                .toArray(MappingDefinition.BCellField[]::new), restMemberType(env, mut, qualifiers.readonly()));
        SemType semType = objectContaining(mappingType);
        notifyContainer();
        return semType;
    }

    private SemType objectContaining(SemType mappingType) {
        Bdd mappingSubTypeData = (Bdd) Core.subTypeData(mappingType, BasicTypeCode.BT_MAPPING);
        return createBasicSemType(BT_OBJECT, mappingSubTypeData);
    }

    private SemType restMemberType(Env env, CellAtomicType.CellMutability mut, boolean readonly) {
        MappingDefinition fieldDefn = new MappingDefinition();
        SemType fieldMemberType = fieldDefn.defineMappingTypeWrapped(env, new MappingDefinition.Field[]{
                        new MappingDefinition.Field(
                                "value",
                                readonly ? Builder.getReadonlyType() : Builder.getValType(),
                                readonly,
                                false),
                        Member.Kind.Field.field(), Member.Visibility.ALL}, Builder.getNeverType(),
                CellAtomicType.CellMutability.CELL_MUT_LIMITED);
        MappingDefinition methodDefn = new MappingDefinition();

        SemType methodMemberType = methodDefn.defineMappingTypeWrapped(env, new MappingDefinition.Field[]{
                        new MappingDefinition.Field("value", Builder.getFunctionType(), true, false),
                        Member.Kind.Method.field(), Member.Visibility.ALL}, Builder.getNeverType(),
                CellAtomicType.CellMutability.CELL_MUT_LIMITED);
        return Builder.getCellContaining(env, union(fieldMemberType, methodMemberType), mut);
    }

    private static MappingDefinition.Field memberField(Env env, Member member, boolean immutableObject) {
        MappingDefinition md = new MappingDefinition();
        SemType semtype = md.defineMappingTypeWrapped(env, new MappingDefinition.Field[]{
                        new MappingDefinition.Field("value", member.valueTy(), member.immutable(), false),
                        member.kind().field(), member.visibility().field()}, Builder.getNeverType(),
                CellAtomicType.CellMutability.CELL_MUT_LIMITED);
        return new MappingDefinition.Field(member.name(), semtype, immutableObject | member.immutable(), false);
    }

    public static SemType distinct(int distinctId) {
        assert distinctId >= 0;
        BddNode bdd = bddAtom(createDistinctRecAtom(-distinctId - 1));
        return createBasicSemType(BT_OBJECT, bdd);
    }
}
