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
package io.ballerina.types.subtypedata;

import io.ballerina.types.PredefinedType;
import io.ballerina.types.ProperSubtypeData;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.UniformTypeCode;

import java.util.Arrays;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represent IntSubtype.
 *
 * @since 2.0.0
 */
public class IntSubtype implements ProperSubtypeData {

    public final Range[] ranges;

    public IntSubtype(Range[] ranges) {
        this.ranges = Arrays.copyOf(ranges, ranges.length);
    }

    public static IntSubtype createIntSubtype(Range... ranges) {
        return new IntSubtype(ranges);
    }

    public static IntSubtype createSingleRangeSubtype(long min, long max) {
        return new IntSubtype(new Range[]{new Range(min, max)});
    }

    public static SemType intConst(long value) {
        return PredefinedType.uniformSubtype(UniformTypeCode.UT_INT, createSingleRangeSubtype(value, value));
    }

    static void validIntWidth(boolean signed, long bits) {
        if (bits <= 0) {
            throw error((bits == 0 ? "zero" : "negative") + " width in bits");
        }
        if (signed) {
            if (bits > 64) {
                throw error("width of signed integers limited to 64");
            }
        } else {
            if (bits > 63) {
                throw error("width of unsigned integers limited to 63");
            }
        }
    }

    static AssertionError error(String message) {
        return new AssertionError(message);
    }

    public static void validIntWidthSigned(int bits) {
        validIntWidth(true, bits);
    }

    public static void validIntWidthUnsigned(int bits) {
        validIntWidth(false, bits);
    }

    public static SemType intWidthSigned(long bits) {
        validIntWidth(true, bits);
        if (bits == 64) {
            return PredefinedType.INT;
        }
        IntSubtype t = createSingleRangeSubtype(-(1L << (bits - 1L)), (1L << (bits - 1L)) - 1L);
        return PredefinedType.uniformSubtype(UniformTypeCode.UT_INT, t);
    }

    public static SemType intWidthUnsigned(int bits) {
        validIntWidth(false, bits);
        IntSubtype t = createSingleRangeSubtype(0L, (1L << bits) - 1L);
        return PredefinedType.uniformSubtype(UniformTypeCode.UT_INT, t);
    }

    // Widen to UnsignedN
    public static SubtypeData intSubtypeWidenUnsigned(SubtypeData d) {
        if (d instanceof AllOrNothingSubtype) {
            return d;
        }

        IntSubtype v = (IntSubtype) d;
        if (v.ranges[0].min < 0L) {
            return AllOrNothingSubtype.createAll();
        }

        Range r = v.ranges[v.ranges.length - 1];
        long i = 8L;
        while (i <= 32L) {
            if (r.max < (1L << i)) {
                IntSubtype w = createSingleRangeSubtype(0L, (1L << i) - 1);
                return w;
            }
            i = i * 2;
        }
        return AllOrNothingSubtype.createAll();
    }

    public static Optional<Long> intSubtypeSingleValue(SubtypeData d) {
        if (d instanceof AllOrNothingSubtype) {
            return Optional.empty();
        }

        IntSubtype v = (IntSubtype) d;
        if (v.ranges.length != 1) {
            return Optional.empty();
        }

        long min = v.ranges[0].min;
        if (min != v.ranges[0].max) {
            return Optional.empty();
        }
        return Optional.of(min);
    }

    public static boolean intSubtypeContains(SubtypeData d, long n) {
        if (d instanceof AllOrNothingSubtype) {
            return ((AllOrNothingSubtype) d).isAllSubtype();
        }
        IntSubtype v = (IntSubtype) d;
        for (Range r : v.ranges) {
            if (r.min <= n && n <= r.max) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringJoiner j = new StringJoiner(", ", "Int:Range[", "]");
        for (Range r : ranges) {
            j.add(minusIndi(r.min) + "-" + minusIndi(r.max));
        }
        return j.toString();
    }

    private String minusIndi(long i) {
        if (i < 0) {
            return "(" + i + ")";
        }
        return String.valueOf(i);
    }
}
