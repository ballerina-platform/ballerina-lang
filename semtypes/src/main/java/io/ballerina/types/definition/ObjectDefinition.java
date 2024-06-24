/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import io.ballerina.types.CellSemType;
import io.ballerina.types.Core;
import io.ballerina.types.Definition;
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;

import java.util.List;
import java.util.stream.Stream;

import static io.ballerina.types.Core.createBasicSemType;
import static io.ballerina.types.Core.union;
import static io.ballerina.types.subtypedata.CellSubtype.cellContaining;

/**
 * Represent object type desc.
 *
 * @since 2201.10.0
 */
public final class ObjectDefinition implements Definition {

    private final MappingDefinition mappingDefinition = new MappingDefinition();

    public SemType define(Env env, ObjectQualifiers qualifiers, List<Member> members) {
        Stream<CellField> memberStream = members.stream().map(member -> memberField(env, member));
        Stream<CellField> qualifierStream = Stream.of(qualifiers.field(env));
        SemType mappingType = mappingDefinition.define(env, Stream.concat(memberStream, qualifierStream).toList(),
                restMemberType(env));
        return objectContaining(mappingType);
    }

    private SemType objectContaining(SemType mappingType) {
        SubtypeData bdd = Core.subtypeData(mappingType, BasicTypeCode.BT_MAPPING);
        assert bdd instanceof Bdd;
        return createBasicSemType(BasicTypeCode.BT_OBJECT, bdd);
    }

    private CellSemType restMemberType(Env env) {
        MappingDefinition fieldDefn = new MappingDefinition();
        SemType fieldMemberType = fieldDefn.defineMappingTypeWrapped(
                env,
                List.of(
                        new Field("value", PredefinedType.ANY, false, false),
                        Member.Kind.Field.field(),
                        Member.Visibility.ALL
                ),
                PredefinedType.NEVER);

        MappingDefinition methodDefn = new MappingDefinition();
        SemType methodMemberType = methodDefn.defineMappingTypeWrapped(
                env,
                List.of(
                        new Field("value", PredefinedType.FUNCTION, false, false),
                        Member.Kind.Method.field(),
                        Member.Visibility.ALL
                ),
                PredefinedType.NEVER);
        return cellContaining(env, union(fieldMemberType, methodMemberType));
    }

    private static CellField memberField(Env env, Member member) {
        MappingDefinition md = new MappingDefinition();
        SemType semtype = md.defineMappingTypeWrapped(
                env,
                List.of(
                        new Field("value", member.valueTy(), false, false),
                        member.kind().field(),
                        member.visibility().field()
                ),
                PredefinedType.NEVER);
        return CellField.from(member.name(), cellContaining(env, semtype));
    }

    @Override
    public SemType getSemType(Env env) {
        return objectContaining(mappingDefinition.getSemType(env));
    }
}
