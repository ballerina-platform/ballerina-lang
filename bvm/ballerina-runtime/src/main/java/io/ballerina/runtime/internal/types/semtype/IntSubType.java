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

import io.ballerina.runtime.api.TypeTags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.ballerina.runtime.api.constants.RuntimeConstants.INT_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.INT_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED16_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED32_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.SIGNED8_MIN_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED16_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED32_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.UNSIGNED8_MAX_VALUE;

public class IntSubType implements SubType {

    final SubTypeData data;

    // we have this because we represent small ints (and byte) differently in memory from int if the value type is not
    // a Java Long type tag must be given as byte always, otherwise we'll cast things to Long and fail
    boolean isByte = false;

    private IntSubType(SubTypeData data) {
        this.data = data;
    }

    private static final IntSubType ALL = new IntSubType(AllOrNothing.ALL);
    private static final IntSubType NOTHING = new IntSubType(AllOrNothing.NOTHING);

    public static IntSubType createIntSubType(List<Long> values) {
        Collections.sort(values);
        List<IntSubType.Range> ranges = new ArrayList<>();
        long start = values.get(0);
        long end = start;
        for (int i = 1; i < values.size(); i++) {
            long value = values.get(i);
            if (value == end + 1) {
                end = value;
            } else {
                ranges.add(new IntSubType.Range(start, end));
                start = value;
                end = value;
            }
        }
        ranges.add(new IntSubType.Range(start, end));
        return new IntSubType(new IntSubTypeData(ranges.toArray(new IntSubType.Range[0])));
    }

    protected long defaultValue() {
        if (data instanceof AllOrNothing) {
            if (data == AllOrNothing.ALL) {
                return 0;
            } else {
                throw new UnsupportedOperationException("Cannot get default value for nothing");
            }
        } else if (data instanceof IntSubTypeData intSubTypeData) {
            long absMin = intSubTypeData.ranges[0].min;
            long absMax = intSubTypeData.ranges[0].max;
            for (int i = 1; i < intSubTypeData.ranges.length; i++) {
                Range range = intSubTypeData.ranges[i];
                if (range.min < absMin) {
                    absMin = range.min;
                }
                if (range.max > absMax) {
                    absMax = range.max;
                }
            }
            if (absMin == absMax) { // We have a singleton
                return absMin;
            } else if (absMin <= 0 && absMax >= 0) {
                return 0;
            } else {
                // TODO: is this correct?
                return 0;
            }
        } else {
            throw new IllegalStateException("Unexpected subtype data for int subtype " + data);
        }
    }

    public static IntSubType createIntSubType(Range[] ranges) {
        return new IntSubType(new IntSubTypeData(ranges));
    }

    @Override
    public SubType union(SubType otherSubType) {
        IntSubType other = (IntSubType) otherSubType;
        if (data instanceof AllOrNothing) {
            if (data == AllOrNothing.ALL) {
                return this;
            } else {
                return other;
            }
        } else if (other.data instanceof AllOrNothing) {
            if (other.data == AllOrNothing.ALL) {
                return other;
            } else {
                return this;
            }
        }
        IntSubTypeData thisData = (IntSubTypeData) data;
        IntSubTypeData otherData = (IntSubTypeData) other.data;
        IntSubTypeData v = thisData.union(otherData);
        Range[] resultRanges = v.ranges;
        if (resultRanges.length == 1 && resultRanges[0].min == INT_MAX_VALUE && resultRanges[0].max == INT_MAX_VALUE) {
            return ALL;
        }
        return new IntSubType(v);
    }

    @Override
    public SubType intersect(SubType otherSubType) {
        IntSubType other = (IntSubType) otherSubType;
        if (data instanceof AllOrNothing) {
            if (data == AllOrNothing.ALL) {
                return other;
            } else {
                return NOTHING;
            }
        } else if (other.data instanceof AllOrNothing) {
            if (other.data == AllOrNothing.ALL) {
                return this;
            } else {
                return NOTHING;
            }
        }
        IntSubTypeData thisData = (IntSubTypeData) data;
        IntSubTypeData otherData = (IntSubTypeData) other.data;
        IntSubTypeData v = thisData.intersect(otherData);
        Range[] resultRanges = v.ranges;
        if (resultRanges.length == 0) {
            return NOTHING;
        }
        return new IntSubType(v);
    }

    @Override
    public SubType complement() {
        if (this.data == AllOrNothing.ALL) {
            return NOTHING;
        } else if (this.data == AllOrNothing.NOTHING) {
            return ALL;
        }
        IntSubTypeData intData = (IntSubTypeData) data;
        return new IntSubType(intData.complement());
    }

    @Override
    public boolean isEmpty() {
        return data == NOTHING.data;
    }

    Optional<Integer> getTag() {
        if (data instanceof AllOrNothing) {
            return Optional.empty();
        }
        if (isByte) {
            return Optional.of(TypeTags.BYTE_TAG);
        }
        IntSubTypeData intData = (IntSubTypeData) data;
        Range[] ranges = intData.ranges;
        if (ranges.length != 1) {
            return Optional.empty();
        }
        Range r = ranges[0];
        if (r.min == 0) {
            if (r.max == UNSIGNED8_MAX_VALUE) {
                return Optional.of(TypeTags.UNSIGNED8_INT_TAG);
            }
            if (r.max == UNSIGNED16_MAX_VALUE) {
                return Optional.of(TypeTags.UNSIGNED16_INT_TAG);
            }
            if (r.max == UNSIGNED32_MAX_VALUE) {
                return Optional.of(TypeTags.UNSIGNED32_INT_TAG);
            }
            return Optional.empty();
        }
        if (r.min == SIGNED8_MIN_VALUE && r.max == SIGNED8_MAX_VALUE) {
            return Optional.of(TypeTags.SIGNED8_INT_TAG);
        }
        if (r.min == SIGNED16_MIN_VALUE && r.max == SIGNED16_MAX_VALUE) {
            return Optional.of(TypeTags.SIGNED16_INT_TAG);
        }
        if (r.min == SIGNED32_MIN_VALUE && r.max == SIGNED32_MAX_VALUE) {
            return Optional.of(TypeTags.SIGNED32_INT_TAG);
        }
        return Optional.of(TypeTags.INT_TAG);
    }

    record Range(long min, long max) {

    }

    static class IntSubTypeData implements SubTypeData {

        private final Range[] ranges;

        IntSubTypeData(Range[] ranges) {
            this.ranges = ranges;
        }

        IntSubTypeData union(IntSubTypeData other) {
            List<Range> result = new ArrayList<>();
            int i1, i2;
            i1 = i2 = 0;
            Range[] v1 = this.ranges;
            Range[] v2 = other.ranges;
            int len1 = ranges.length;
            int len2 = other.ranges.length;
            while (true) {
                if (i1 >= len1) {
                    if (i2 >= len2) {
                        break;
                    }
                    rangeUnionPush(result, v2[i2]);
                    i2++;
                } else if (i2 >= len2) {
                    rangeUnionPush(result, v1[i1]);
                    i1++;
                } else {
                    Range r1 = v1[i1];
                    Range r2 = v2[i2];
                    RangeOpResult combined = rangeUnion(r1, r2);
                    switch (combined.tag) {
                        case OVERLAP -> {
                            rangeUnionPush(result, combined.range);
                            i1++;
                            i2++;
                        }
                        case BEFORE -> {
                            rangeUnionPush(result, r1);
                            i1++;
                        }
                        case AFTER -> {
                            rangeUnionPush(result, r2);
                            i2++;
                        }
                    }
                }
            }
            return new IntSubTypeData(result.toArray(new Range[0]));
        }

        IntSubTypeData intersect(IntSubTypeData other) {
            List<Range> result = new ArrayList<>();
            int i1, i2;
            i1 = i2 = 0;
            Range[] v1 = this.ranges;
            Range[] v2 = other.ranges;
            int len1 = ranges.length;
            int len2 = other.ranges.length;
            while (true) {
                if (i1 >= len1 || i2 >= len2) {
                    break;
                }
                Range r1 = v1[i1];
                Range r2 = v2[i2];
                RangeOpResult combined = rangeIntersect(r1, r2);
                switch (combined.tag) {
                    case OVERLAP -> {
                        rangeUnionPush(result, combined.range);
                        i1++;
                        i2++;
                    }
                    case BEFORE -> i1++;
                    case AFTER -> i2++;
                }
            }
            return new IntSubTypeData(result.toArray(new Range[0]));
        }

        IntSubTypeData complement() {
            List<Range> result = new ArrayList<>();
            Range[] v = this.ranges;
            int len = v.length;
            long min = v[0].min;
            if (min > INT_MIN_VALUE) {
                result.add(new Range(INT_MIN_VALUE, min - 1));
            }
            for (int i = 1; i < len; i++) {
                result.add(new Range(v[i - 1].max + 1, v[i].min - 1));
            }
            long max = v[v.length - 1].max;
            if (max < INT_MAX_VALUE) {
                result.add(new Range(max + 1, INT_MAX_VALUE));
            }
            return new IntSubTypeData(result.toArray(new Range[0]));
        }

        private static void rangeUnionPush(List<Range> ranges, Range next) {
            int lastIndex = ranges.size() - 1;
            if (lastIndex < 0) {
                ranges.add(next);
                return;
            }
            RangeOpResult result = rangeUnion(ranges.get(lastIndex), next);
            if (result.tag == RangeOpResultTag.OVERLAP) {
                ranges.set(lastIndex, result.range);
            } else {
                ranges.add(next);
            }
        }

        private static RangeOpResult rangeIntersect(Range r1, Range r2) {
            if (r1.max < r2.min) {
                return new RangeOpResult(RangeOpResultTag.BEFORE, null);
            }
            if (r2.max < r1.min) {
                return new RangeOpResult(RangeOpResultTag.AFTER, null);
            }
            return new RangeOpResult(RangeOpResultTag.OVERLAP,
                    new Range(Math.max(r1.min, r2.min), Math.min(r1.max, r2.max)));
        }

        enum RangeOpResultTag {
            BEFORE,
            OVERLAP,
            AFTER,
        }

        record RangeOpResult(RangeOpResultTag tag, Range range) {

        }

        private static RangeOpResult rangeUnion(Range r1, Range r2) {
            if (r1.max < r2.min) {
                if (r1.max + 1 != r2.min) {
                    return new RangeOpResult(RangeOpResultTag.BEFORE, null);
                }
            }
            if (r2.max < r1.min) {
                if (r1.max + 1 != r2.min) {
                    return new RangeOpResult(RangeOpResultTag.AFTER, null);
                }
            }
            return new RangeOpResult(RangeOpResultTag.OVERLAP,
                    new Range(Math.min(r1.min, r2.min), Math.max(r1.max, r2.max)));
        }
    }
}
