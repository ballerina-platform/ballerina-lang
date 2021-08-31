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
package io.ballerina.semtype;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;

/**
 * Int Range node.
 *
 * @since 2.0.0
 */
public class Range implements RangeHolder {
    public final long min;
    public final long max;

    public Range(long min, long max) {
        this.min = min;
        this.max = max;
    }

    public static Range[] rangeListUnion(Range[] v1, Range[] v2) {
        List<Range> result = new ArrayList<>();
        int i1 = 0;
        int i2 = 0;
        int len1 = v1.length;
        int len2 = v2.length;

        while (true) {
            if (i1 >= len1) {
                if (i2 >= len2) {
                    break;
                }
                rangeUnionPush(result, v2[i2]);
                i2 += 1;
            } else if (i2 >= len2) {
                rangeUnionPush(result, v1[i1]);
                i1 += 1;
            } else {
                Range r1 = v1[i1];
                Range r2 = v2[i2];
                RangeHolder combined = rangeUnion(r1, r2);
                if (combined instanceof Range) {
                    rangeUnionPush(result, (Range) combined);
                    i1 += 1;
                    i2 += 1;
                } else if (((RangeValue) combined).value < 0) {
                    rangeUnionPush(result, r1);
                    i1 += 1;
                } else {
                    rangeUnionPush(result, r2);
                    i2 += 1;
                }
            }
        }
        Range[] rangeList = new Range[result.size()];
        return result.toArray(rangeList);
    }

    public static void rangeUnionPush(List<Range> ranges, Range next) {
        int lastIndex = ranges.size() - 1;
        if (lastIndex < 0) {
            ranges.add(next);
            return;
        }
        RangeHolder combined = rangeUnion(ranges.get(lastIndex), next);
        if (combined instanceof Range) {
            ranges.set(lastIndex, (Range) combined);
        } else {
            ranges.add(next);
        }
    }
    /* [from nballerina] Returns a range if there is a single range representing the union of r1 and r1.
     -1 means union is empty because r1 is before r2, with no overlap
     1 means union is empty because r2 is before r1 with no overlap
     Precondition r1 and r2 are non-empty */
    public static RangeHolder rangeUnion(Range r1, Range r2) {
        if (r1.max < r2.min) {
            if (r1.max + 1 != r2.min) {
                return RangeValue.from(-1);
            }
        }
        if (r2.max < r1.min) {
            if (r2.max + 1 != r1.min) {
                return RangeValue.from(1);
            }
        }
        return new Range(Long.min(r1.min, r2.min), Long.max(r1.max, r2.max));
    }

    public static Range[] rangeListIntersect(Range[] v1, Range[] v2) {
        List<Range> result = new ArrayList<>();
        int i1 = 0;
        int i2 = 0;
        int len1 = v1.length;
        int len2 = v2.length;
        while (true) {
            if (i1 >= len1 || i2 >= len2) {
                break;
            } else {
                Range r1 = v1[i1];
                Range r2 = v2[i2];
                RangeHolder combined = rangeIntersect(r1, r2);
                if (combined instanceof Range) {
                    result.add((Range) combined);
                    i1 += 1;
                    i2 += 1;
                } else if (((RangeValue) combined).value < 0) {
                    i1 += 1;
                } else {
                    i2 += 1;
                }
            }
        }
        Range[] rangeList = new Range[result.size()];
        return result.toArray(rangeList);
    }

    /* [from nballerina] When Range is returned, it is non-empty and the intersection of r1 and r2
     -1 means empty intersection because r1 before r2
     1 means empty intersection because r1 after r2 */
    public static RangeHolder rangeIntersect(Range r1, Range r2) {
        if (r1.max < r2.min) {
            return RangeValue.from(-1);
        }
        if (r2.max < r1.min) {
            return RangeValue.from(1);
        }
        return new Range(Long.max(r1.min, r2.min), Long.min(r1.max, r2.max));
    }

    public static Range[] rangeListComplement(Range[] v) {
        List<Range> result = new ArrayList<>();
        int len = v.length;
        long min = v[0].min;
        if (min > MIN_VALUE) {
            result.add(new Range(MIN_VALUE,  min - 1));
        }
        for (int i = 1; i < len; i++) {
            result.add(new Range(v[i - 1].max + 1, v[i].min - 1));
        }
        long max = v[v.length - 1].max;
        if (max < MAX_VALUE) {
            result.add(new Range(max + 1, MAX_VALUE));
        }
        Range[] rangeList = new Range[result.size()];
        return result.toArray(rangeList);
    }
}
