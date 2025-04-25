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

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.AtomicType;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.FieldPair;
import io.ballerina.runtime.api.types.semtype.FieldPairs;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.ArrayList;
import java.util.Collection;

import static io.ballerina.runtime.api.types.semtype.Core.cellInner;
import static io.ballerina.runtime.api.types.semtype.Core.intersectCellMemberSemTypes;
import static io.ballerina.runtime.api.types.semtype.Core.isNever;

/**
 * Represent mapping atomic type.
 *
 * @param names required member names of the mapping
 * @param types required member types of the mapping
 * @param rest  rest of member type of the mapping
 * @since 2201.12.0
 */
public record MappingAtomicType(String[] names, SemType[] types, SemType rest) implements AtomicType {

    public MappingAtomicType {
        assert names.length == types.length;
    }

    public MappingAtomicType intersectMapping(Env env, MappingAtomicType other) {
        int expectedSize = Integer.min(types().length, other.types().length);
        Collection<String> names = new ArrayList<>(expectedSize);
        Collection<SemType> types = new ArrayList<>(expectedSize);
        for (FieldPair fieldPair : new FieldPairs(this, other)) {
            names.add(fieldPair.name());
            SemType t = intersectCellMemberSemTypes(env, fieldPair.type1(), fieldPair.type2());
            if (isNever(cellInner(fieldPair.type1()))) {
                return null;

            }
            types.add(t);
        }
        SemType rest = intersectCellMemberSemTypes(env, this.rest(), other.rest());
        return new MappingAtomicType(names.toArray(String[]::new), types.toArray(SemType[]::new), rest);
    }
}
