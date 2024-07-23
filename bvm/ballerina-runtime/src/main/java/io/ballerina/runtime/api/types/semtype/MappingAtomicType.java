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

package io.ballerina.runtime.api.types.semtype;

import java.util.ArrayList;
import java.util.Collection;

import static io.ballerina.runtime.api.types.semtype.Core.cellInner;
import static io.ballerina.runtime.api.types.semtype.Core.intersectMemberSemTypes;
import static io.ballerina.runtime.api.types.semtype.Core.isNever;

public record MappingAtomicType(String[] names, SemType[] types, SemType rest) implements AtomicType {

    public MappingAtomicType intersectMapping(Env env, MappingAtomicType other) {
        int expectedSize = Integer.min(types().length, other.types().length);
        Collection<String> names = new ArrayList<>(expectedSize);
        Collection<SemType> types = new ArrayList<>(expectedSize);
        for (FieldPair fieldPair : new FieldPairs(this, other)) {
            names.add(fieldPair.name());
            SemType t = intersectMemberSemTypes(env, fieldPair.type1(), fieldPair.type2());
            if (isNever(cellInner(fieldPair.type1()))) {
                return null;

            }
            types.add(t);
        }
        SemType rest = intersectMemberSemTypes(env, this.rest(), other.rest());
        return new MappingAtomicType(names.toArray(String[]::new), types.toArray(SemType[]::new), rest);
    }
}
