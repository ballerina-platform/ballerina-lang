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
import io.ballerina.types.SubtypeData;
import io.ballerina.types.Context;
import io.ballerina.types.UniformTypeOps;
import io.ballerina.types.subtypedata.AllOrNothingSubtype;
import io.ballerina.types.subtypedata.BooleanSubtype;

/**
 * Uniform type ops for boolean type.
 *
 * @since 3.0.0
 */
public class BooleanOps implements UniformTypeOps {
    @Override
    public SubtypeData union(SubtypeData d1, SubtypeData d2) {
        BooleanSubtype v1 = (BooleanSubtype) d1;
        BooleanSubtype v2 = (BooleanSubtype) d2;
        return v1.value == v2.value ? v1 : AllOrNothingSubtype.createAll();
    }

    @Override
    public SubtypeData intersect(SubtypeData d1, SubtypeData d2) {
        BooleanSubtype v1 = (BooleanSubtype) d1;
        BooleanSubtype v2 = (BooleanSubtype) d2;
        return v1.value == v2.value ? v1 : AllOrNothingSubtype.createNothing();
    }

    @Override
    public SubtypeData diff(SubtypeData d1, SubtypeData d2) {
        BooleanSubtype v1 = (BooleanSubtype) d1;
        BooleanSubtype v2 = (BooleanSubtype) d2;
        return v1.value == v2.value ? AllOrNothingSubtype.createNothing() : v1;
    }

    @Override
    public SubtypeData complement(SubtypeData d) {
        BooleanSubtype v = (BooleanSubtype) d;
        BooleanSubtype t = BooleanSubtype.from(!v.value);
        return t;
    }

    @Override
    public boolean isEmpty(Context cx, SubtypeData t) {
        return Common.notIsEmpty(cx, t);
    }
}
