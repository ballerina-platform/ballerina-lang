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

package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.internal.types.semtype.AllOrNothing;
import io.ballerina.runtime.internal.types.semtype.SubTypeData;
import io.ballerina.runtime.internal.types.semtype.SubtypePair;
import io.ballerina.runtime.internal.types.semtype.SubtypePairs;

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_B_TYPE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_UNDEF;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.VT_MASK;

public final class Core {

    public static final SemType SEMTYPE_TOP = SemType.from((1 << (CODE_UNDEF + 1)) - 1);
    public static final SemType B_TYPE_TOP = SemType.from(1 << BT_B_TYPE.code());

    private Core() {
    }

    public static SemType diff(SemType t1, SemType t2) {
        int all1 = t1.all;
        int all2 = t2.all;
        int some1 = t1.some;
        int some2 = t2.some;
        if (some1 == 0) {
            if (some2 == 0) {
                return Builder.basicTypeUnion(all1 & ~all2);
            } else {
                if (all1 == 0) {
                    return t1;
                }
            }
        } else {
            if (some2 == 0) {
                if (all2 == VT_MASK) {
                    return Builder.basicTypeUnion(0);
                }
            }
        }
        int all = all1 & ~(all2 | some2);
        int some = (all1 | some1) & ~all2;
        some = some & ~all;
        if (some == 0) {
            return SemType.from(all);
        }
        SubType[] subtypes = Builder.initializeSubtypeArray();
        for (SubtypePair pair : new SubtypePairs(t1, t2, some)) {
            SubType data1 = pair.subType1();
            SubType data2 = pair.subType2();
            int code = pair.typeCode();
            SubType data;
            if (data1 == null) {
                data = data2.complement();
            } else if (data2 == null) {
                data = data1;
            } else {
                data = data1.diff(data2);
            }
            if (data.isAll()) {
                all |= 1 << code;
                some &= ~(1 << code);
            } else if (data.isNothing()) {
                some &= ~(1 << code);
            } else {
                subtypes[code] = data;
            }
        }
        return SemType.from(all, some, subtypes);
    }

    public static SubType getComplexSubtypeData(SemType t, BasicTypeCode code) {
        throw new IllegalStateException("Unimplemented");
    }

    public static SemType union(SemType t1, SemType t2) {
        int all1 = t1.all();
        int some1 = t1.some();
        int all2 = t2.all();
        int some2 = t2.some();
        if (some1 == 0) {
            if (some2 == 0) {
                return Builder.basicTypeUnion(all1 | all2);
            }
        }

        int all = all1 | all2;
        int some = (some1 | some2) & ~all;
        if (some == 0) {
            return Builder.basicTypeUnion(all);
        }
        SubType[] subtypes = Builder.initializeSubtypeArray();
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
            } else {
                subtypes[code] = data;
            }
        }
        if (some == 0) {
            return SemType.from(all);
        }
        return SemType.from(all, some, subtypes);
    }

    public static SemType intersect(SemType t1, SemType t2) {
        int all1 = t1.all;
        int some1 = t1.some;
        int all2 = t2.all;
        int some2 = t2.some;
        if (some1 == 0) {
            if (some2 == 0) {
                return SemType.from(all1 & all2);
            } else {
                if (all1 == 0) {
                    return t1;
                }
                if (all1 == VT_MASK) {
                    return t2;
                }
            }
        } else if (some2 == 0) {
            if (all2 == 0) {
                return t2;
            }
            if (all2 == VT_MASK) {
                return t1;
            }
        }

        int all = all1 & all2;
        int some = (some1 | all1) & (some2 | all2);
        some = some & ~all;
        if (some == 0) {
            return SemType.from(all);
        }

        SubType[] subtypes = Builder.initializeSubtypeArray();
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
                subtypes[code] = data;
            } else {
                some &= ~(1 << code);
            }
        }
        if (some == 0) {
            return SemType.from(all);
        }
        return SemType.from(all, some, subtypes);
    }

    public static boolean isEmpty(Context cx, SemType t) {
        if (t.some == 0) {
            return t.all == 0;
        }
        if (t.all != 0) {
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
        return t.all == 0 && t.some == 0;
    }

    public static boolean isSubType(Context cx, SemType t1, SemType t2) {
        // IF t1 and t2 are not pure semtypes calling this is an undefined
        return isEmpty(cx, diff(t1, t2));
    }

    public static boolean isSubtypeSimple(SemType t1, SemType t2) {
        int bits = t1.all | t1.some;
        return (bits & ~t2.all()) == 0;
    }

    public static SubTypeData subTypeData(SemType s, BasicTypeCode code) {
        if ((s.all & (1 << code.code())) != 0) {
            return AllOrNothing.ALL;
        }
        if (s.some == 0) {
            return AllOrNothing.NOTHING;
        }
        return s.subTypeData()[code.code()].data();
    }

    public static boolean containsBasicType(SemType t1, SemType t2) {
        int bits = t1.all | t1.some;
        return (bits & t2.all) != 0;
    }

    public static boolean isSameType(Context cx, SemType t1, SemType t2) {
        return isSubType(cx, t1, t2) && isSubType(cx, t2, t1);
    }

    public static BasicTypeBitSet widenToBasicTypes(SemType t) {
        int all = t.all | t.some;
        if (cardinality(all) > 1) {
            throw new IllegalStateException("Cannot widen to basic type for a type with multiple basic types");
        }
        return Builder.basicTypeUnion(all);
    }

    private static int cardinality(int bitset) {
        return Integer.bitCount(bitset);
    }

    public static SemType widenToBasicTypeUnion(SemType t) {
        if (t.some == 0) {
            return t;
        }
        int all = t.all | t.some;
        return Builder.basicTypeUnion(all);
    }
}
