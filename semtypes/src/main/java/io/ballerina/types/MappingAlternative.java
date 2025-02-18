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
package io.ballerina.types;

import java.util.ArrayList;
import java.util.List;

/**
 *  Represents MappingAlternative record in core.bal.
 *
 * @since 2201.12.0
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

    public MappingAlternative[] mappingAlternatives(Context cx, SemType t) {
        if (t instanceof BasicTypeBitSet b) {
            if ((b.bitset & PredefinedType.MAPPING.bitset) == 0) {
                return new MappingAlternative[]{};
            } else {
                return new MappingAlternative[]{
                        from(cx, PredefinedType.MAPPING, List.of(), List.of())
                };
            }
        } else {
            List<BddPath> paths = new ArrayList<>();
            BddPath.bddPaths((Bdd) Core.getComplexSubtypeData((ComplexSemType) t, BasicTypeCode.BT_MAPPING), paths,
                    BddPath.from());
            List<MappingAlternative> alts = new ArrayList<>();
            for (BddPath bddPath : paths) {
                SemType semType = Core.createBasicSemType(BasicTypeCode.BT_MAPPING, bddPath.bdd);
                if (!Core.isNever(semType)) {
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
