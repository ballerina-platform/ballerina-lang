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
package io.ballerina.semtype.typeops;

import io.ballerina.semtype.Range;
import io.ballerina.semtype.SubtypeData;
import io.ballerina.semtype.TypeCheckContext;
import io.ballerina.semtype.UniformTypeOps;
import io.ballerina.semtype.subtypedata.AllOrNothingSubtype;
import io.ballerina.semtype.subtypedata.IntSubtype;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;

/**
 * Uniform subtype ops for int type.
 *
 * @since 2.0.0
 */
public class IntOps implements UniformTypeOps {
    @Override
    public SubtypeData union(SubtypeData d1, SubtypeData d2) {
        IntSubtype v1 = (IntSubtype) d1;
        IntSubtype v2 = (IntSubtype) d1;
        Range[] v = Range.rangeListUnion(v1.ranges, v2.ranges);
        if (v.length == 1 && v[0].min == MIN_VALUE && v[0].max == MAX_VALUE) {
            return AllOrNothingSubtype.createAll();
        }
        return IntSubtype.createIntSubtype(v);
    }

    @Override
    public SubtypeData intersect(SubtypeData d1, SubtypeData d2) {
        IntSubtype v1 = (IntSubtype) d1;
        IntSubtype v2 = (IntSubtype) d1;
        Range[] v = Range.rangeListIntersect(v1.ranges, v2.ranges);
        if (v.length == 0) {
            return AllOrNothingSubtype.createNothing();
        }
        return IntSubtype.createIntSubtype(v);
    }

    @Override
    public SubtypeData diff(SubtypeData d1, SubtypeData d2) {
        IntSubtype v1 = (IntSubtype) d1;
        IntSubtype v2 = (IntSubtype) d1;
        Range[] v = Range.rangeListIntersect(v1.ranges, Range.rangeListComplement(v2.ranges));
        if (v.length == 0) {
            return AllOrNothingSubtype.createNothing();
        }
        return IntSubtype.createIntSubtype(v);
    }

    @Override
    public SubtypeData complement(SubtypeData d) {
        IntSubtype v = (IntSubtype) d;
        return IntSubtype.createIntSubtype(Range.rangeListComplement(v.ranges));
    }

    @Override
    public boolean isEmpty(TypeCheckContext tc, SubtypeData t) {
        throw new AssertionError();
    }
}
