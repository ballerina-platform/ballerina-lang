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

import io.ballerina.runtime.api.types.semtype.Atom;
import io.ballerina.runtime.api.types.semtype.BasicTypeCode;
import io.ballerina.runtime.api.types.semtype.BddNode;
import io.ballerina.runtime.api.types.semtype.CellAtomicType;
import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.MappingAtomicType;
import io.ballerina.runtime.api.types.semtype.RecAtom;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.Arrays;
import java.util.Comparator;

import static io.ballerina.runtime.api.types.semtype.BddNode.bddAtom;
import static io.ballerina.runtime.api.types.semtype.Builder.basicSubType;
import static io.ballerina.runtime.api.types.semtype.Builder.cellContaining;
import static io.ballerina.runtime.api.types.semtype.Builder.undef;
import static io.ballerina.runtime.api.types.semtype.Core.isNever;
import static io.ballerina.runtime.api.types.semtype.Core.union;

public class MappingDefinition implements Definition {

    private RecAtom rec = null;
    private SemType semType = null;

    @Override
    public SemType getSemType(Env env) {
        SemType s = this.semType;
        if (s == null) {
            RecAtom rec = env.recMappingAtom();
            this.rec = rec;
            return this.createSemType(env, rec);
        } else {
            return s;
        }
    }

    private SemType createSemType(Env env, Atom atom) {
        BddNode bdd = bddAtom(atom);
        this.semType = basicSubType(BasicTypeCode.BT_MAPPING, BMappingSubType.createDelegate(bdd));
        return this.semType;
    }

    public SemType defineMappingTypeWrapped(Env env, Field[] fields, SemType rest, CellAtomicType.CellMutability mut) {
        assert rest != null;
        BCellField[] cellFields = new BCellField[fields.length];
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            BCellField cellField = BCellField.from(env, field, mut);
            cellFields[i] = cellField;
        }
        SemType restCell = cellContaining(env, union(rest, undef()),
                isNever(rest) ? CellAtomicType.CellMutability.CELL_MUT_NONE : mut);
        return define(env, cellFields, restCell);
    }

    SemType define(Env env, BCellField[] cellFields, SemType rest) {
        String[] names = new String[cellFields.length];
        SemType[] types = new SemType[cellFields.length];
        sortAndSplitFields(cellFields, names, types);
        MappingAtomicType atomicType = new MappingAtomicType(names, types, rest);
        Atom atom;
        RecAtom rec = this.rec;
        if (rec != null) {
            atom = rec;
            env.setRecMappingAtomType(rec, atomicType);
        } else {
            atom = env.mappingAtom(atomicType);
        }
        return this.createSemType(env, atom);
    }

    private void sortAndSplitFields(BCellField[] fields, String[] names, SemType[] types) {
        assert fields.length == names.length && fields.length == types.length;
        Arrays.sort(fields, Comparator.comparing((field) -> field.name));
        for (int i = 0; i < fields.length; i++) {
            names[i] = fields[i].name;
            types[i] = fields[i].type;
        }
    }

    public record Field(String name, SemType ty, boolean readonly, boolean optional) {

    }

    record BCellField(String name, SemType type) {

        static BCellField from(Env env, Field field, CellAtomicType.CellMutability mut) {
            SemType type = field.ty;
            SemType cellType = cellContaining(env, field.optional ? union(type, undef()) : type,
                    field.readonly ? CellAtomicType.CellMutability.CELL_MUT_NONE : mut);
            BCellField cellField = new BCellField(field.name, cellType);
            return cellField;
        }
    }
}
