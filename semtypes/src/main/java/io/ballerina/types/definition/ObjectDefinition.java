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

import static io.ballerina.types.Core.createBasicSemType;
import static io.ballerina.types.Core.union;
import static io.ballerina.types.SemTypes.stringConst;
import static io.ballerina.types.subtypedata.CellSubtype.cellContaining;

public final class ObjectDefinition implements Definition {

    private static final SemType MEMBER_KIND_FIELD = stringConst("field");
    private static final SemType MEMBER_KIND_METHOD = stringConst("method");
    private final MappingDefinition mappingDefinition = new MappingDefinition();

    public SemType define(Env env, List<Member> members) {
        if (members.isEmpty()) {
            return PredefinedType.OBJECT;
        }
        List<CellField> memberCells = members.stream().map(member -> switch (member.kind()) {
            case Field -> fieldMember(env, member);
            case Method -> methodMember(env, member);
        }).toList();
        SemType mappingType = mappingDefinition.define(env, memberCells, restMemberType(env));
        return objectContaining(mappingType);
    }

    private CellField fieldMember(Env env, Member member) {
        return CellField.from(member.name(), cellContaining(env, createInnerFieldType(env, member.valueTy())));
    }

    private CellField methodMember(Env env, Member member) {
        return CellField.from(member.name(), cellContaining(env, createInnerMethodType(env, member.valueTy())));
    }

    private SemType objectContaining(SemType mappingType) {
        SubtypeData bdd = Core.subtypeData(mappingType, BasicTypeCode.BT_MAPPING);
        assert bdd instanceof Bdd;
        return createBasicSemType(BasicTypeCode.BT_OBJECT, bdd);
    }

    private CellSemType restMemberType(Env env) {
        SemType fieldMemberType = createInnerFieldType(env, PredefinedType.ANY);

        SemType methodMemberType = createInnerMethodType(env, PredefinedType.FUNCTION);
        return cellContaining(env, union(fieldMemberType, methodMemberType));
    }

    private static SemType createInnerMethodType(Env env, SemType methodType) {
        MappingDefinition methodDefn = new MappingDefinition();
        return methodDefn.defineMappingTypeWrapped(
                env,
                List.of(
                        new Field("value", methodType, false, false),
                        new Field("kind", MEMBER_KIND_METHOD, true, false)
                ),
                PredefinedType.NEVER);
    }

    private static SemType createInnerFieldType(Env env, SemType memberType) {
        MappingDefinition fieldDefn = new MappingDefinition();
        return fieldDefn.defineMappingTypeWrapped(
                env,
                List.of(
                        new Field("value", memberType, false, false),
                        new Field("kind", MEMBER_KIND_FIELD, true, false)
                ),
                PredefinedType.NEVER);
    }

    @Override
    public SemType getSemType(Env env) {
        return objectContaining(mappingDefinition.getSemType(env));
    }
}
