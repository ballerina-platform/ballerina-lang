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
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.SubType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.ballerina.runtime.api.constants.RuntimeConstants.INT_MAX_VALUE;
import static io.ballerina.runtime.api.constants.RuntimeConstants.INT_MIN_VALUE;

/**
 * Runtime representation of IntSubType.
 *
 * @since 2201.10.0
 */
public final class BIntSubType extends SubType {

    final SubTypeData data;

    private BIntSubType(SubTypeData data) {
        super(data == AllOrNothing.ALL, data == AllOrNothing.NOTHING);
        this.data = data;
    }

    private static final BIntSubType ALL = new BIntSubType(AllOrNothing.ALL);
    private static final BIntSubType NOTHING = new BIntSubType(AllOrNothing.NOTHING);

    public static BIntSubType createIntSubType(List<Long> values) {
        Collections.sort(values);
        List<Range> ranges = new ArrayList<>();
        long start = values.get(0);
        long end = start;
        for (int i = 1; i < values.size(); i++) {
            long value = values.get(i);
            if (value == end + 1) {
                end = value;
            } else {
                ranges.add(new Range(start, end));
                start = value;
                end = value;
            }
        }
        ranges.add(new Range(start, end));
        return new BIntSubType(new IntSubTypeData(ranges.toArray(Range[]::new)));
    }

    public static BIntSubType createIntSubType(long min, long max) {
        Range range = new Range(min, max);
        Range[] ranges = {range};
        return new BIntSubType(new IntSubTypeData(ranges));
    }

    @Override
    public SubType union(SubType otherSubType) {
        BIntSubType other = (BIntSubType) otherSubType;
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
        return new BIntSubType(v);
    }

    @Override
    public SubType intersect(SubType otherSubType) {
        BIntSubType other = (BIntSubType) otherSubType;
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
        return new BIntSubType(v);
    }

    @Override
    public SubType complement() {
        if (this.data == AllOrNothing.ALL) {
            return NOTHING;
        } else if (this.data == AllOrNothing.NOTHING) {
            return ALL;
        }
        IntSubTypeData intData = (IntSubTypeData) data;
        return new BIntSubType(intData.complement());
    }

    @Override
    public boolean isEmpty() {
        return data == AllOrNothing.NOTHING;
    }

    @Override
    public SubTypeData data() {
        return data;
    }

    record Range(long min, long max) {

    }

    static final class IntSubTypeData implements SubTypeData {

        private final Range[] ranges;

        private IntSubTypeData(Range[] ranges) {
            this.ranges = ranges;
        }

        private IntSubTypeData union(IntSubTypeData other) {
            List<Range> result = new ArrayList<>();
            int i1 = 0;
            int i2 = 0;
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
            return new IntSubTypeData(result.toArray(Range[]::new));
        }

        IntSubTypeData intersect(IntSubTypeData other) {
            List<Range> result = new ArrayList<>();
            int i1 = 0;
            int i2 = 0;
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
            return new IntSubTypeData(result.toArray(Range[]::new));
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
            return new IntSubTypeData(result.toArray(Range[]::new));
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
