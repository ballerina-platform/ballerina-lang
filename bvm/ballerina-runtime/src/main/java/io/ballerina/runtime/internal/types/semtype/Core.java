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

import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_BTYPE;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_NIL;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.N_TYPES;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.isSet;

public class Core {

    // TODO: move to builder
    static BSemType basicTypeUnion(int all) {
        return new BSemType(all, 0, new SubType[N_TYPES]);
    }

    // NOTE: all type operations expect intersection and union has undefined behavior for the BType part.
    //  With intersection, you can use all of BType inorder to separate out BType part of a semtype. Union should work
    //  as expected for BType part.
    public static BSemType intersect(BSemType t1, BSemType t2) {
        // all = all1 & all2
        int all = t1.all & t2.all;

        // some = (some1 | all1) & (some2 | all2) & ~all
        int some = (t1.some | t1.all) & (t2.some | t2.all);
        some &= ~all;

        if (some == 0) {
            return basicTypeUnion(all);
        }
        SubType[] subTypes = new SubType[N_TYPES];
        for (int i = 0; i < N_TYPES; i++) {
            boolean t1Has = isSet(t1.some, i);
            boolean t2Has = isSet(t2.some, i);
            if (!t1Has && !t2Has) {
                continue;
            }
            SubType data;
            if (t1Has && t2Has) {
                data = t1.subTypeData[i].intersect(t2.subTypeData[i]);
            } else if (!t1Has) {
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
        int all = t1.all & ~(t2.all | t2.some);

        // some = some1 & ~(all2)
        int some = (t1.all | t1.some) & ~t2.all;
        some &= ~all;

        if (some == 0) {
            return basicTypeUnion(all);
        }
        SubType[] subTypes = new SubType[N_TYPES];
        for (int i = 0; i < BT_BTYPE; i++) {
            boolean t1Has = isSet(t1.some, i);
            boolean t2Has = isSet(t2.some, i);
            if (!t1Has && !t2Has) {
                continue;
            }
            if (t1Has && t2Has) {
                subTypes[i] = t1.subTypeData[i].diff(t2.subTypeData[i]);
            } else if (!t1Has) {
                subTypes[i] = t2.subTypeData[i].complement();
            } else {
                subTypes[i] = t1.subTypeData[i];
            }
        }

        return new BSemType(all, some, subTypes);
    }

    public static boolean isEmpty(BSemType type) {
        if (type.all != 0) {
            return false;
        }
        if (type.some == 0) {
            return true;
        }
        for (int i = 0; i < BT_BTYPE; i++) {
            if (isSet(type.some, i) && !type.subTypeData[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSubType(BSemType t1, BSemType t2) {
        return isEmpty(diff(t1, t2));
    }

    public static boolean containsSimple(BSemType t1, int basicTypeCode) {
        int mask = 1 << basicTypeCode;
        int bits = t1.all | t1.some;
        return (bits & mask) != 0;
    }

    public static boolean isSubTypeSimple(BSemType semType, int basicTypeBitSet) {
        int bits = semType.all | semType.some;
        return (bits & ~basicTypeBitSet) == 0 && bits != 0;
    }

    public static boolean belongToBasicType(BSemType semType, int basicTypeCode) {
        return isSubTypeSimple(semType, 1 << basicTypeCode);
    }

    public static boolean isNever(BSemType semType) {
        return semType.all == 0 && semType.some == 0;
    }

    public static boolean containsNil(BSemType semType) {
        return (semType.all & (1 << BT_NIL)) != 0;
    }

    public static BSemType union(BSemType t1, BSemType t2) {
        int all = 0;
        int some = 0;
        SubType[] subTypeData = new SubType[N_TYPES];

        all = t1.all | t2.all;

        some = t1.some | t2.some;
        some &= ~all;

        for (int i = 0; i < N_TYPES; i++) {
            // TODO: this don't lift the some to all, (I think each subtype needs to provide a method similar to
            //  empty to check this)
            boolean t1Has = isSet(t1.some, i);
            boolean t2Has = isSet(t2.some, i);
            if (t1Has && t2Has) {
                subTypeData[i] = t1.subTypeData[i].union(t2.subTypeData[i]);
            } else if (t1Has) {
                subTypeData[i] = t1.subTypeData[i];
            } else if (t2Has) {
                subTypeData[i] = t2.subTypeData[i];
            }
        }
        return new BSemType(all, some, subTypeData);
    }
}
