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
import io.ballerina.types.EnumerableCharString;
import io.ballerina.types.EnumerableString;
import io.ballerina.types.EnumerableSubtype;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.TypeCheckContext;
import io.ballerina.types.UniformTypeOps;
import io.ballerina.types.subtypedata.AllOrNothingSubtype;
import io.ballerina.types.subtypedata.CharStringSubtype;
import io.ballerina.types.subtypedata.NonCharStringSubtype;
import io.ballerina.types.subtypedata.StringSubtype;

import java.util.ArrayList;

/**
 * Uniform subtype ops for string type.
 *
 * @since 3.0.0
 */
public class StringOps implements UniformTypeOps {
    @Override
    public SubtypeData union(SubtypeData d1, SubtypeData d2) {
        //List<EnumerableString> values = new ArrayList<>();
        //boolean allowed = EnumerableSubtype.enumerableSubtypeUnion((StringSubtype) d1, (StringSubtype) d2, values);
        //EnumerableString[] valueArray = new EnumerableString[values.size()];
        //return StringSubtype.createStringSubtype(allowed, values.toArray(valueArray));
        StringSubtype sd1 = (StringSubtype) d1;
        StringSubtype sd2 = (StringSubtype) d2;
        ArrayList<EnumerableCharString> chars = new ArrayList<>();
        ArrayList<EnumerableString> nonChars = new ArrayList<>();
        boolean charsAllowed = EnumerableSubtype.enumerableSubtypeUnion(sd1.getChar(), sd2.getChar(), chars);
        boolean nonCharsAllowed = EnumerableSubtype.enumerableSubtypeUnion(sd1.getNonChar(),
                sd2.getNonChar(), nonChars);

        return StringSubtype.createStringSubtype(CharStringSubtype.from(charsAllowed,
                        chars.toArray(new EnumerableCharString[]{})),
                NonCharStringSubtype.from(nonCharsAllowed, nonChars.toArray(new EnumerableString[]{})));
    }

    @Override
    public SubtypeData intersect(SubtypeData d1, SubtypeData d2) {
        if (d1 instanceof AllOrNothingSubtype) {
            return ((AllOrNothingSubtype) d1).isAllSubtype()? d2 : AllOrNothingSubtype.createNothing();
        }
        if (d2 instanceof AllOrNothingSubtype) {
            return ((AllOrNothingSubtype) d2).isAllSubtype() ? d1 : AllOrNothingSubtype.createNothing();
        }

        StringSubtype sd1 = (StringSubtype) d1;
        StringSubtype sd2 = (StringSubtype) d2;
        ArrayList<EnumerableCharString> chars = new ArrayList<>();
        ArrayList<EnumerableString> nonChars = new ArrayList<>();
        boolean charsAllowed = EnumerableSubtype.enumerableSubtypeUnion(sd1.getChar(), sd2.getChar(), chars);
        boolean nonCharsAllowed = EnumerableSubtype.enumerableSubtypeUnion(sd1.getNonChar(),
                sd2.getNonChar(), nonChars);

        return StringSubtype.createStringSubtype(CharStringSubtype.from(charsAllowed,
                        chars.toArray(new EnumerableCharString[]{})),
                NonCharStringSubtype.from(nonCharsAllowed, nonChars.toArray(new EnumerableString[]{})));
    }

    @Override
    public SubtypeData diff(SubtypeData d1, SubtypeData d2) {
        return intersect(d1, complement(d2));
    }

    @Override
    public SubtypeData complement(SubtypeData d) {
        StringSubtype st = (StringSubtype) d;
        if (st.getChar().values.length == 0 && st.getNonChar().values.length == 0) {
            if (st.getChar().allowed && st.getNonChar().allowed) {
                return AllOrNothingSubtype.createAll();
            }
        else if (!st.getChar().allowed && !st.getNonChar().allowed) {
                return AllOrNothingSubtype.createNothing();
            }
        }

        return StringSubtype.createStringSubtype(CharStringSubtype.from(!st.getChar().allowed, st.getChar().values),
                NonCharStringSubtype.from(!st.getNonChar().allowed, st.getNonChar().values));
    }

    @Override
    public boolean isEmpty(TypeCheckContext tc, SubtypeData t) {
        return Common.notIsEmpty(tc, t);
    }
}
