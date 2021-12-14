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
package io.ballerina.types;

import java.util.ArrayList;
import java.util.List;

/**
 *  Represents MappingAlternative record in core.bal.
 *
 * @since 3.0.0
 */
public class MappingAlternative {
    SemType semType;
    MappingAtomicType[] pos;
    MappingAtomicType[] neg;

    private MappingAlternative(SemType semType, MappingAtomicType[] pos, MappingAtomicType[] neg) {
        this.semType = semType;
        this.pos = pos;
        this.neg = neg;
    }

    public MappingAlternative[] mappingAlternativesRw(Context cx, SemType t) {
        if (t instanceof UniformTypeBitSet) {
            if ((((UniformTypeBitSet) t).bitset & PredefinedType.MAPPING_RW.bitset) == 0) {
                return new MappingAlternative[]{};
            } else {
                return new MappingAlternative[]{
                        from(cx, PredefinedType.MAPPING_RW, new ArrayList<>(), new ArrayList<>())
                };
            }
        } else {
            List<BddPath> paths = new ArrayList<>();
            BddPath.bddPaths((Bdd) Core.getComplexSubtypeData((ComplexSemType) t, UniformTypeCode.UT_MAPPING_RW), paths,
                    BddPath.from());
            List<MappingAlternative> alts = new ArrayList<>();
            for (BddPath bddPath : paths) {
                SemType semType = Core.createUniformSemType(UniformTypeCode.UT_MAPPING_RW, bddPath.bdd);
                if (semType != PredefinedType.NEVER) {
                    alts.add(from(cx, semType, bddPath.pos, bddPath.neg));
                }
            }
            return alts.toArray(new MappingAlternative[]{});
        }
    }

    public MappingAlternative from(Context cx, SemType semType, List<Atom> pos, List<Atom> neg) {
        MappingAtomicType[] p = new MappingAtomicType[pos.size()];
        MappingAtomicType[] n = new MappingAtomicType[neg.size()];
        for (int i = 0; i < pos.size(); i++) {
            p[i] = cx.mappingAtomType(pos.get(i));
        }
        for (int i = 0; i < neg.size(); i++) {
            n[i] = cx.mappingAtomType(neg.get(i));
        }
        return new MappingAlternative(semType, p, n);
    }
}
