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
package io.ballerina.types.typeops;

import io.ballerina.types.Common;
import io.ballerina.types.EnumerableDecimal;
import io.ballerina.types.EnumerableSubtype;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.TypeCheckContext;
import io.ballerina.types.UniformTypeOps;
import io.ballerina.types.subtypedata.DecimalSubtype;

import java.util.ArrayList;

/**
 * Decimal specific methods operate on SubtypeData.
 *
 * @since 3.0.0
 */
public class DecimalOps extends CommonOps implements UniformTypeOps {
    @Override
    public SubtypeData union(SubtypeData t1, SubtypeData t2) {
        ArrayList<EnumerableDecimal> values = new ArrayList<>();
        boolean allowed = EnumerableSubtype.enumerableSubtypeUnion((DecimalSubtype) t1, (DecimalSubtype) t2, values);
        EnumerableDecimal[] valueArray = new EnumerableDecimal[values.size()];
        return DecimalSubtype.createDecimalSubtype(allowed, values.toArray(valueArray));
    }

    @Override
    public SubtypeData intersect(SubtypeData t1, SubtypeData t2) {
        ArrayList<EnumerableDecimal> values = new ArrayList<>();
        boolean allowed = EnumerableSubtype.enumerableSubtypeIntersect((DecimalSubtype) t1,
                (DecimalSubtype) t2, values);
        return DecimalSubtype.createDecimalSubtype(allowed, values.toArray(new EnumerableDecimal[]{}));
    }

    @Override
    public SubtypeData diff(SubtypeData t1, SubtypeData t2) {
        return intersect(t1, complement(t2));
    }

    @Override
    public SubtypeData complement(SubtypeData t) {
        DecimalSubtype s = (DecimalSubtype) t;
        return DecimalSubtype.createDecimalSubtype(!s.allowed, (EnumerableDecimal[]) s.values);
    }

    @Override
    public boolean isEmpty(TypeCheckContext tc, SubtypeData t) {
        return Common.notIsEmpty(tc, t);
    }
}
