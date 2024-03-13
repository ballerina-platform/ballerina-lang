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

import io.ballerina.runtime.api.types.Type;

import java.util.BitSet;

import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.BasicTypeCodes.BT_BTYPE;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.BasicTypeCodes.N_TYPES;

public class Core {

    // TODO: move to builder
    static BSemType basicTypeUnion(BitSet all) {
        return new BSemType(all, new BitSet(N_TYPES), new SubType[N_TYPES]);
    }

    // NOTE: all type operations expect intersection and union has undefined behavior for the BType part.
    //  With intersection, you can use all of BType inorder to separate out BType part of a semtype. Union should work
    //  as expected for BType part.
    public static BSemType intersect(BSemType t1, BSemType t2) {
        // all = all1 & all2
        BitSet all = new BitSet(N_TYPES);
        all.or(t1.all);
        all.and(t2.all);

        // some = (some1 | all1) & (some2 | all2) & ~all
        BitSet t1Some = new BitSet(N_TYPES);
        t1Some.or(t1.some);
        t1Some.or(t1.all);

        BitSet t2Some = new BitSet(N_TYPES);
        t2Some.or(t2.some);
        t2Some.or(t2.all);

        BitSet some = new BitSet(N_TYPES);
        some.or(t1Some);
        some.and(t2Some);
        some.andNot(all);

        if (some.isEmpty()) {
            return basicTypeUnion(all);
        }
        SubType[] subTypes = new SubType[N_TYPES];
        for (int i = 0; i < N_TYPES; i++) {
            if (!t1.some.get(i) && !t2.some.get(i)) {
                continue;
            }
            SubType data;
            if (t1.some.get(i) && t2.some.get(i)) {
                data = t1.subTypeData[i].intersect(t2.subTypeData[i]);
            } else if (!t1.some.get(i)) {
                data = t2.subTypeData[i];
            } else {
                data = t1.subTypeData[i];
            }
            if (!data.isEmpty()) {
                subTypes[i] = data;
            }
        }
        return new BSemType(all, some, subTypes);
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
        for (int i = 0; i < BT_BTYPE; i++) {
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
        for (int i = 0; i < BT_BTYPE; i++) {
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

    public static boolean isNever(Type type) {
        return type instanceof BSemType semType && semType.all.isEmpty() && semType.some.isEmpty();
    }

    public static BSemType union(BSemType t1, BSemType t2) {
        BSemType semtype = new BSemType();

        semtype.all.or(t1.all);
        semtype.all.or(t2.all);

        semtype.some.or(t1.some);
        semtype.some.or(t2.some);
        semtype.some.andNot(semtype.all);

        SubType[] subTypeData = semtype.subTypeData;
        for (int i = 0; i < N_TYPES; i++) {
            // TODO: this don't lift the some to all, (I think each subtype needs to provide a method similar to empty to
            //  check this)
            boolean t1Has = t1.some.get(i);
            boolean t2Has = t2.some.get(i);
            if (t1Has && t2Has) {
                subTypeData[i] = t1.subTypeData[i].union(t2.subTypeData[i]);
            } else if (t1Has) {
                subTypeData[i] = t1.subTypeData[i];
            } else if (t2Has) {
                subTypeData[i] = t2.subTypeData[i];
            }
        }
        return semtype;
    }
}
