/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types.typeops;

import io.ballerina.types.BasicTypeOps;
import io.ballerina.types.Common;
import io.ballerina.types.Context;
import io.ballerina.types.EnumerableFloat;
import io.ballerina.types.EnumerableSubtype;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.subtypedata.FloatSubtype;

import java.util.ArrayList;

/**
 * Float specific methods operate on SubtypeData.
 *
 * @since 2201.8.0
 */
public class FloatOps extends CommonOps implements BasicTypeOps {
    @Override
    public SubtypeData union(SubtypeData t1, SubtypeData t2) {
        ArrayList<EnumerableFloat> values = new ArrayList<>();
        boolean allowed = EnumerableSubtype.enumerableSubtypeUnion((FloatSubtype) t1, (FloatSubtype) t2, values);
        EnumerableFloat[] valueArray = new EnumerableFloat[values.size()];
        return FloatSubtype.createFloatSubtype(allowed, values.toArray(valueArray));
    }

    @Override
    public SubtypeData intersect(SubtypeData t1, SubtypeData t2) {
        ArrayList<EnumerableFloat> values = new ArrayList<>();
        boolean allowed = EnumerableSubtype.enumerableSubtypeIntersect((FloatSubtype) t1, (FloatSubtype) t2, values);
        return FloatSubtype.createFloatSubtype(allowed, values.toArray(new EnumerableFloat[]{}));
    }

    @Override
    public SubtypeData diff(SubtypeData t1, SubtypeData t2) {
        return intersect(t1, complement(t2));
    }

    @Override
    public SubtypeData complement(SubtypeData t) {
        FloatSubtype s = (FloatSubtype) t;
        return FloatSubtype.createFloatSubtype(!s.allowed, s.values);
    }

    @Override
    public boolean isEmpty(Context cx, SubtypeData t) {
        return Common.notIsEmpty(cx, t);
    }
}
