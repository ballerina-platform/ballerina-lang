/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.runtime.api.types.SemType;

import io.ballerina.runtime.api.types.BasicTypeBitSet;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.semtype.AllOrNothing;
import io.ballerina.runtime.internal.types.semtype.BBasicTypeBitSet;
import io.ballerina.runtime.internal.types.semtype.BSemType;
import io.ballerina.runtime.internal.types.semtype.SubTypeData;
import io.ballerina.runtime.internal.types.semtype.SubtypePair;
import io.ballerina.runtime.internal.types.semtype.SubtypePairs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static io.ballerina.runtime.api.types.SemType.BasicTypeCode.BT_B_TYPE;
import static io.ballerina.runtime.api.types.SemType.BasicTypeCode.CODE_UNDEF;
import static io.ballerina.runtime.api.types.SemType.BasicTypeCode.VT_MASK;

public final class Core {

    private static final SemType SEMTYPE_TOP = BBasicTypeBitSet.from((1 << (CODE_UNDEF + 1)) - 1);

    private Core() {
    }

    public static SemType diff(SemType t1, SemType t2) {
        if (t1.some() == 0) {
            if (t2.some() == 0) {
                return Builder.basicTypeUnion(t1.all() & ~t2.all());
            } else {
                if (t1.all() == 0) {
                    return t1;
                }
            }
        } else {
            if (t2.some() == 0) {
                if (t2.all() == VT_MASK) {
                    return BBasicTypeBitSet.from(0);
                }
            }
        }
        int all1 = t1.all();
        int all2 = t2.all();
        int some1 = t1.some();
        int some2 = t2.some();
        int all = all1 & ~(all2 | some2);
        int some = (all1 | some1) & ~all2;
        some = some & ~all;
        if (some == 0) {
            return BBasicTypeBitSet.from(all);
        }
        List<SubType> subtypes = new ArrayList<>();
        for (SubtypePair pair : new SubtypePairs(t1, t2, some)) {
            SubType data1 = pair.subType1();
            SubType data2 = pair.subType2();
            int typeCode = pair.typeCode();
            SubType data;
            if (data1 == null) {
                data = data2.complement();
            } else if (data2 == null) {
                data = data1;
            } else {
                data = data1.diff(data2);
            }
            if (data.isAll()) {
                all |= 1 << typeCode;
                some &= ~(1 << typeCode);
                subtypes.add(null);
            } else if (data.isNothing()) {
                some &= ~(1 << typeCode);
                subtypes.add(null);
            } else {
                subtypes.add(data);
            }
        }
        return BSemType.from(all, some, subtypes);
    }

    public static SubType getComplexSubtypeData(SemType t, BasicTypeCode code) {
        throw new IllegalStateException("Unimplemented");
    }

    public static SemType union(SemType t1, SemType t2) {
        if (t1.some() == 0) {
            if (t2.some() == 0) {
                return BBasicTypeBitSet.from(t1.all() | t2.all());
            }
        }
        int all1 = t1.all();
        int some1 = t1.some();
        int all2 = t2.all();
        int some2 = t2.some();

        int all = all1 | all2;
        int some = (some1 | some2) & ~all;
        if (some == 0) {
            return BBasicTypeBitSet.from(all);
        }
        List<SubType> subtypes = new ArrayList<>();
        for (SubtypePair pair : new SubtypePairs(t1, t2, some)) {
            int code = pair.typeCode();
            SubType data1 = pair.subType1();
            SubType data2 = pair.subType2();
            SubType data;
            if (data1 == null) {
                data = data2;
            } else if (data2 == null) {
                data = data1;
            } else {
                data = data1.union(data2);
            }
            if (data.isAll()) {
                all |= 1 << code;
                some &= ~(1 << code);
                subtypes.add(null);
            } else {
                subtypes.add(data);
            }
        }
        if (some == 0) {
            return BBasicTypeBitSet.from(all);
        }
        return BSemType.from(all, some, subtypes);
    }

    public static SemType intersect(SemType t1, SemType t2) {
        if (t1.some() == 0) {
            if (t2.some() == 0) {
                return BBasicTypeBitSet.from(t1.all() & t2.all());
            } else {
                if (t1.all() == 0) {
                    return t1;
                }
                if (t1.all() == VT_MASK) {
                    return t2;
                }
            }
        } else if (t2.some() == 0) {
            if (t2.all() == 0) {
                return t2;
            }
            if (t2.all() == VT_MASK) {
                return t1;
            }
        }
        int all1 = t1.all();
        int some1 = t1.some();
        int all2 = t2.all();
        int some2 = t2.some();

        int all = all1 & all2;
        int some = (some1 | all1) & (some2 | all2);
        some = some & ~all;
        if (some == 0) {
            return BBasicTypeBitSet.from(all);
        }

        List<SubType> subtypes = new ArrayList<>();
        for (SubtypePair pair : new SubtypePairs(t1, t2, some)) {
            int code = pair.typeCode();
            SubType data1 = pair.subType1();
            SubType data2 = pair.subType2();

            SubType data;
            if (data1 == null) {
                data = data2;
            } else if (data2 == null) {
                data = data1;
            } else {
                data = data1.intersect(data2);
            }

            if (!data.isNothing()) {
                subtypes.add(data);
            } else {
                some &= ~(1 << code);
                subtypes.add(null);
            }
        }
        if (some == 0) {
            return BBasicTypeBitSet.from(all);
        }
        return BSemType.from(all, some, subtypes);
    }

    public static boolean isEmpty(Context cx, SemType t) {
        if (t.some() == 0) {
            return t.all() == 0;
        }
        if (t.all() != 0) {
            return false;
        }
        for (SubType subType : t.subTypeData()) {
            if (subType == null) {
                continue;
            }
            if (!subType.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static SemType complement(SemType t1) {
        throw new IllegalStateException("Unimplemented");
    }

    public static boolean isNever(SemType t) {
        throw new IllegalStateException("Unimplemented");
    }

    public static boolean isSubType(Context cx, SemType t1, SemType t2) {
        // IF t1 and t2 are not pure semtypes calling this is an error
        return isEmpty(cx, diff(t1, t2));
    }

    public static boolean isSubType(Context cx, SemType t1, SemType t2,
                                    BiFunction<? super BType, ? super BType, Boolean> fallback) {
        SemType s1 = intersect(t1, SEMTYPE_TOP);
        SemType s2 = intersect(t2, SEMTYPE_TOP);
        return isEmpty(cx, diff(s1, s2)) && applyFallback(t1, t2, fallback);
    }

    private static boolean applyFallback(SemType t1, SemType t2,
                                         BiFunction<? super BType, ? super BType, Boolean> fallback) {
        BType bType1 = (BType) subTypeData(t1, BT_B_TYPE);
        BType bType2 = (BType) subTypeData(t2, BT_B_TYPE);
        return fallback.apply(bType1, bType2);
    }

    private static SubTypeData subTypeData(SemType s, BasicTypeCode code) {
        if ((s.all() & (1 << code.code())) != 0) {
            return AllOrNothing.ALL;
        }
        if (s.some() == 0) {
            return AllOrNothing.NOTHING;
        }
        return s.subTypeData().get(code.code()).data();
    }

    public static boolean isSubTypeSimple(SemType t1, BasicTypeBitSet t2) {
        throw new IllegalStateException("Unimplemented");
    }

    public static boolean isSameType(Context cx, SemType t1, SemType t2) {
        return isSubType(cx, t1, t2) && isSubType(cx, t2, t1);
    }

    public static BasicTypeBitSet widenToBasicTypes(SemType t) {
        int all = t.all() | t.some();
        if (cardinality(all) > 1) {
            throw new IllegalStateException("Cannot widen to basic type for a type with multiple basic types");
        }
        return BBasicTypeBitSet.from(all);
    }

    private static boolean isSet(int bitset, int index) {
        return (bitset & (1 << index)) != 0;
    }

    private static int cardinality(int bitset) {
        int count = 0;
        while (bitset != 0) {
            count += bitset & 1;
            bitset >>= 1;
        }
        return count;
    }
}
