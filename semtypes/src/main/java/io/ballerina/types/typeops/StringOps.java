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
import io.ballerina.types.EnumerableCharString;
import io.ballerina.types.EnumerableString;
import io.ballerina.types.EnumerableSubtype;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.subtypedata.AllOrNothingSubtype;
import io.ballerina.types.subtypedata.CharStringSubtype;
import io.ballerina.types.subtypedata.NonCharStringSubtype;
import io.ballerina.types.subtypedata.StringSubtype;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.types.EnumerableSubtype.EQ;
import static io.ballerina.types.EnumerableSubtype.GT;
import static io.ballerina.types.EnumerableSubtype.LT;

/**
 * Basic subtype ops for string type.
 *
 * @since 2201.8.0
 */
public class StringOps implements BasicTypeOps {
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
            return ((AllOrNothingSubtype) d1).isAllSubtype() ? d2 : AllOrNothingSubtype.createNothing();
        }
        if (d2 instanceof AllOrNothingSubtype) {
            return ((AllOrNothingSubtype) d2).isAllSubtype() ? d1 : AllOrNothingSubtype.createNothing();
        }

        StringSubtype sd1 = (StringSubtype) d1;
        StringSubtype sd2 = (StringSubtype) d2;
        ArrayList<EnumerableCharString> chars = new ArrayList<>();
        ArrayList<EnumerableString> nonChars = new ArrayList<>();
        boolean charsAllowed = EnumerableSubtype.enumerableSubtypeIntersect(sd1.getChar(), sd2.getChar(), chars);
        boolean nonCharsAllowed = EnumerableSubtype.enumerableSubtypeIntersect(sd1.getNonChar(),
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
            } else if (!st.getChar().allowed && !st.getNonChar().allowed) {
                return AllOrNothingSubtype.createNothing();
            }
        }

        return StringSubtype.createStringSubtype(CharStringSubtype.from(!st.getChar().allowed, st.getChar().values),
                NonCharStringSubtype.from(!st.getNonChar().allowed, st.getNonChar().values));
    }

    @Override
    public boolean isEmpty(Context cx, SubtypeData t) {
        return Common.notIsEmpty(cx, t);
    }

    // Returns a description of the relationship between a StringSubtype and a list of strings
    // `values` must be ordered.
    static StringSubtype.StringSubtypeListCoverage stringSubtypeListCoverage(StringSubtype subtype, String[] values) {
        List<Integer> indices = new ArrayList<>();
        CharStringSubtype ch = subtype.getChar();
        NonCharStringSubtype nonChar = subtype.getNonChar();
        int stringConsts = 0;
        if (ch.allowed) {
            stringListIntersect(values, toStringArray(ch.values), indices);
            stringConsts = ch.values.length;
        } else if (ch.values.length == 0) {
            for (int i = 0; i < values.length; i++) {
                if (values[i].length() == 1) {
                    indices.add(i);
                }
            }
        }
        if (nonChar.allowed) {
            stringListIntersect(values, toStringArray(nonChar.values), indices);
            stringConsts += nonChar.values.length;
        } else if (nonChar.values.length == 0) {
            for (int i = 0; i < values.length; i++) {
                if (values[i].length() != 1) {
                    indices.add(i);
                }
            }
        }
        int[] inds = indices.stream().mapToInt(i -> i).toArray();
        return StringSubtype.StringSubtypeListCoverage.from(stringConsts == indices.size(), inds);
    }

    private static String[] toStringArray(EnumerableCharString[] ar) {
        String[] strings = new String[ar.length];
        for (int i = 0; i < ar.length; i++) {
            strings[i] = ar[i].value;
        }
        return strings;
    }

    private static String[] toStringArray(EnumerableString[] ar) {
        String[] strings = new String[ar.length];
        for (int i = 0; i < ar.length; i++) {
            strings[i] = ar[i].value;
        }
        return strings;
    }

    static void stringListIntersect(String[] values, String[] target, List<Integer> indices) {
        int i1 = 0;
        int i2 = 0;
        int len1 = values.length;
        int len2 = target.length;
        while (true) {
            if (i1 >= len1 || i2 >= len2) {
                break;
            } else {
                int comp = EnumerableSubtype.compareEnumerable(EnumerableString.from(values[i1]),
                                                               EnumerableString.from(target[i2]));
                switch (comp) {
                    case EQ:
                        indices.add(i1);
                        i1 += 1;
                        i2 += 1;
                        break;
                    case LT:
                        i1 += 1;
                        break;
                    case GT:
                        i2 += 1;
                        break;
                    default:
                        throw new AssertionError("Invalid comparison value!");
                }
            }
        }
    }
}
