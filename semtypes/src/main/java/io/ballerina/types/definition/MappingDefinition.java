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

import io.ballerina.types.Atom;
import io.ballerina.types.BasicTypeCode;
import io.ballerina.types.CellAtomicType;
import io.ballerina.types.CellSemType;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Core;
import io.ballerina.types.Definition;
import io.ballerina.types.Env;
import io.ballerina.types.MappingAtomicType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.RecAtom;
import io.ballerina.types.SemType;
import io.ballerina.types.subtypedata.BddNode;
import io.ballerina.types.typeops.BddCommonOps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.types.Core.union;
import static io.ballerina.types.PredefinedType.UNDEF;
import static io.ballerina.types.subtypedata.CellSubtype.cellContaining;

/**
 * Represent mapping type desc.
 *
 * @since 2201.12.0
 */
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

    /**
     * This is a deviation from nBallerina. jBallerina considers `record {never x;}` as `never`.
     * This method is to support jBallerina behavior.
     */
    public void setSemTypeToNever() {
        this.semType = PredefinedType.NEVER;
    }

    public SemType define(Env env, List<CellField> fields, CellSemType rest) {
        SplitField sfh = splitFields(fields);
        MappingAtomicType atomicType = MappingAtomicType.from(sfh.names.toArray(String[]::new),
                sfh.types.toArray(CellSemType[]::new), rest);
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

    public SemType defineMappingTypeWrapped(Env env, List<Field> fields, SemType rest) {
        return defineMappingTypeWrapped(env, fields, rest, CELL_MUT_LIMITED);
    }

    public SemType defineMappingTypeWrapped(Env env, List<Field> fields, SemType rest,
                                            CellAtomicType.CellMutability mut) {
        List<CellField> cellFields = new ArrayList<>(fields.size());
        for (Field field : fields) {
            SemType ty = field.ty();
            cellFields.add(
                    CellField.from(field.name(), cellContaining(
                            env,
                            field.opt() ? union(ty, UNDEF) : ty,
                            field.ro() ? CELL_MUT_NONE : mut
                    ))
            );
        }
        CellSemType restCell = cellContaining(
                env,
                union(rest, UNDEF),
                Core.isNever(rest) ? CELL_MUT_NONE : mut
        );
        return define(env, cellFields, restCell);
    }

    private SemType createSemType(Env env, Atom atom) {
        BddNode bdd = BddCommonOps.bddAtom(atom);
        ComplexSemType s = PredefinedType.basicSubtype(BasicTypeCode.BT_MAPPING, bdd);
        this.semType = s;
        return s;
    }

    private SplitField splitFields(List<CellField> fields) {
        CellField[] sortedFields = fields.toArray(CellField[]::new);
        Arrays.sort(sortedFields, Comparator.comparing(MappingDefinition::fieldName));
        List<String> names = new ArrayList<>();
        List<CellSemType> types = new ArrayList<>();
        for (CellField field : sortedFields) {
            names.add(field.name());
            types.add(field.type());
        }
        return SplitField.from(names, types);
    }

    private static String fieldName(CellField f) {
        return f.name();
    }
}
