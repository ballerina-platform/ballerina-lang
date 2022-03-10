/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.types.definition;

import io.ballerina.types.Atom;
import io.ballerina.types.Common;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Core;
import io.ballerina.types.Definition;
import io.ballerina.types.Env;
import io.ballerina.types.MappingAtomicType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.RecAtom;
import io.ballerina.types.SemType;
import io.ballerina.types.UniformSubtype;
import io.ballerina.types.UniformTypeCode;
import io.ballerina.types.subtypedata.BddNode;
import io.ballerina.types.typeops.BddCommonOps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Represent mapping type desc.
 *
 * @since 3.0.0
 */
public class MappingDefinition implements Definition {

    private RecAtom roRec = null;
    private RecAtom rwRec = null;
    private SemType semType = null;

    @Override
    public SemType getSemType(Env env) {
        SemType s = this.semType;
        if (s == null) {
            RecAtom ro = env.recMappingAtom();
            RecAtom rw = env.recMappingAtom();
            this.roRec = ro;
            this.rwRec = rw;
            return createSemType(env, ro, rw);
        } else {
            return s;
        }
    }

    public SemType define(Env env, List<Field> fields, SemType rest) {
        SplitField sfh = splitFields(fields);
        MappingAtomicType rwType = MappingAtomicType.from(sfh.names.toArray(new String[]{}),
                sfh.types.toArray(new SemType[]{}), rest);
        Atom rw;
        RecAtom rwRec = this.rwRec;
        if (rwRec != null) {
            rw = rwRec;
            env.setRecMappingAtomType(rwRec, rwType);
        } else {
            rw = env.mappingAtom(rwType);
        }
        Atom ro;
        if (Common.typeListIsReadOnly(rwType.types) && Core.isReadOnly(rest)) {
            RecAtom roRec = this.roRec;
            if (roRec == null) {
                ro = rw;
            } else {
                ro = roRec;
                env.setRecMappingAtomType(roRec, rwType);
            }
        } else {
            MappingAtomicType roType = MappingAtomicType.from(rwType.names,
                    (Common.readOnlyTypeList(rwType.types)),
                    Core.intersect(rest, PredefinedType.READONLY));
            ro = env.mappingAtom(roType);
            RecAtom roRec = this.roRec;
            if (roRec != null) {
                env.setRecMappingAtomType(roRec, roType);
            }
        }
        return this.createSemType(env, ro, rw);
    }

    private SemType createSemType(Env env, Atom ro, Atom rw) {
        BddNode roBdd = BddCommonOps.bddAtom(ro);
        BddNode rwBdd;
        if (BddCommonOps.atomCmp(ro, rw) == 0) {
            rwBdd = roBdd;
        } else {
            rwBdd = BddCommonOps.bddAtom(rw);
        }
        SemType s = ComplexSemType.createComplexSemType(0,
                UniformSubtype.from(UniformTypeCode.UT_MAPPING_RO, roBdd),
                UniformSubtype.from(UniformTypeCode.UT_MAPPING_RW, rwBdd));
        this.semType = s;
        return s;
    }

    private SplitField splitFields(List<Field> fields) {
        Field[] sortedFields = fields.toArray(new Field[]{});
        Arrays.sort(sortedFields, Comparator.comparing(MappingDefinition::fieldName));
        List<String> names = new ArrayList<>();
        List<SemType> types = new ArrayList<>();
        for (Field field : sortedFields) {
            names.add(field.name);
            types.add(field.type);
        }
        return SplitField.from(names, types);
    }

    private static String fieldName(Field f) {
        return f.name;
    }
}
