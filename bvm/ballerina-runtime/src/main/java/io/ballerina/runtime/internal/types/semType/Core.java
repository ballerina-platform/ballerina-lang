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

package io.ballerina.runtime.internal.types.semType;

import java.util.BitSet;

import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.N_TYPES;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_BTYPE;

public class Core {

    // TODO: move to builder
    static BSemType basicTypeUnion(BitSet all) {
        return new BSemType(all, new BitSet(N_TYPES), new SubType[N_TYPES]);
    }

    public static BSemType diff(BSemType t1, BSemType t2) {
        // all = all1 & ~(all2 | some2)
        BitSet rhs = new BitSet(N_TYPES);
        rhs.or(t2.all);
        rhs.or(t2.some);
        BitSet all = new BitSet(N_TYPES);
        all.or(t1.all);
        all.andNot(rhs);

        // some = some1 & ~(all2)
        BitSet some = new BitSet(N_TYPES);
        some.or(t1.some);
        some.or(t1.all);
        some.andNot(t2.all);
        some.andNot(all);

        if (some.isEmpty()) {
            return basicTypeUnion(all);
        }
        SubType[] subTypes = new SubType[N_TYPES];
        for (int i = 0; i < UT_BTYPE; i++) {
            if (!t1.some.get(i) && !t2.some.get(i)) {
                continue;
            }
            if (t1.some.get(i) && t2.some.get(i)) {
                subTypes[i] = t1.subTypeData[i].diff(t2.subTypeData[i]);
            } else if (!t1.some.get(i)) {
                subTypes[i] = t2.subTypeData[i].complement();
            } else {
                subTypes[i] = t1.subTypeData[i];
            }
        }

        return new BSemType(all, some, subTypes);
    }

    public static boolean isEmpty(BSemType type) {
        if (!type.all.isEmpty()) {
            return false;
        }
        if (type.some.isEmpty()) {
            return true;
        }
        for (int i = 0; i < UT_BTYPE; i++) {
            if (type.some.get(i) && !type.subTypeData[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSubType(BSemType t1, BSemType t2) {
        return isEmpty(diff(t1, t2));
    }

    public static boolean containsSimple(BSemType t1, int uniformTypeCode) {
        return t1.all.get(uniformTypeCode) || t1.some.get(uniformTypeCode);
    }
}
