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

import io.ballerina.semtype.Common;
import io.ballerina.semtype.EnumerableString;
import io.ballerina.semtype.EnumerableSubtype;
import io.ballerina.semtype.SubtypeData;
import io.ballerina.semtype.TypeCheckContext;
import io.ballerina.semtype.UniformTypeOps;
import io.ballerina.semtype.subtypedata.StringSubtype;

import java.util.ArrayList;
import java.util.List;

/**
 * Uniform subtype ops for string type.
 *
 * @since 2.0.0
 */
public class StringOps implements UniformTypeOps {
    @Override
    public SubtypeData union(SubtypeData d1, SubtypeData d2) {
        List<EnumerableString> values = new ArrayList<>();
        boolean allowed = EnumerableSubtype.enumerableSubtypeUnion((StringSubtype) d1, (StringSubtype) d2, values);
        EnumerableString[] valueArray = new EnumerableString[values.size()];
        return StringSubtype.createStringSubtype(allowed, values.toArray(valueArray));
    }

    @Override
    public SubtypeData intersect(SubtypeData d1, SubtypeData d2) {
        List<EnumerableString> values = new ArrayList<>();
        boolean allowed = EnumerableSubtype.enumerableSubtypeIntersect((StringSubtype) d1, (StringSubtype) d2, values);
        EnumerableString[] valueArray = new EnumerableString[values.size()];
        return StringSubtype.createStringSubtype(allowed, values.toArray(valueArray));
    }

    @Override
    public SubtypeData diff(SubtypeData d1, SubtypeData d2) {
        return intersect(d1, complement(d2));
    }

    @Override
    public SubtypeData complement(SubtypeData d) {
        StringSubtype s = (StringSubtype) d;
        return StringSubtype.createStringSubtype(!s.allowed, (EnumerableString[]) s.values);
    }

    @Override
    public boolean isEmpty(TypeCheckContext tc, SubtypeData t) {
        return Common.notIsEmpty(tc, t);
    }
}
