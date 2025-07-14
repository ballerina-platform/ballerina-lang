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
import io.ballerina.types.Bdd;
import io.ballerina.types.Common;
import io.ballerina.types.Conjunction;
import io.ballerina.types.Context;
import io.ballerina.types.RecAtom;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.subtypedata.BddAllOrNothing;
import io.ballerina.types.subtypedata.XmlSubtype;

import static io.ballerina.types.subtypedata.XmlSubtype.XML_PRIMITIVE_ALL_MASK;
import static io.ballerina.types.subtypedata.XmlSubtype.XML_PRIMITIVE_RO_MASK;
import static io.ballerina.types.subtypedata.XmlSubtype.XML_PRIMITIVE_RO_SINGLETON;

/**
 * Basic subtype ops for xml type.
 *
 * @since 2201.8.0
 */
public class XmlOps implements BasicTypeOps {

    public static final XmlSubtype XML_SUBTYPE_RO = XmlSubtype.from(XML_PRIMITIVE_RO_MASK,
            BddCommonOps.bddAtom(RecAtom.createXMLRecAtom(XML_PRIMITIVE_RO_SINGLETON)));
    public static final XmlSubtype XML_SUBTYPE_TOP = XmlSubtype.from(XML_PRIMITIVE_ALL_MASK, BddAllOrNothing.bddAll());

    @Override
    public SubtypeData union(SubtypeData d1, SubtypeData d2) {
        XmlSubtype v1 = (XmlSubtype) d1;
        XmlSubtype v2 = (XmlSubtype) d2;
        int primitives = v1.primitives | v2.primitives;
        return XmlSubtype.createXmlSubtype(primitives, BddCommonOps.bddUnion(v1.sequence, v2.sequence));
    }

    @Override
    public SubtypeData intersect(SubtypeData d1, SubtypeData d2) {
        XmlSubtype v1 = (XmlSubtype) d1;
        XmlSubtype v2 = (XmlSubtype) d2;
        int primitives = v1.primitives & v2.primitives;
        return XmlSubtype.createXmlSubtypeOrEmpty(primitives, BddCommonOps.bddIntersect(v1.sequence, v2.sequence));
    }

    @Override
    public SubtypeData diff(SubtypeData d1, SubtypeData d2) {
        XmlSubtype v1 = (XmlSubtype) d1;
        XmlSubtype v2 = (XmlSubtype) d2;
        int primitives = v1.primitives & ~v2.primitives;
        return XmlSubtype.createXmlSubtypeOrEmpty(primitives, BddCommonOps.bddDiff(v1.sequence, v2.sequence));
    }

    @Override
    public SubtypeData complement(SubtypeData d) {
        return diff(XML_SUBTYPE_TOP, d);
    }

    @Override
    public boolean isEmpty(Context cx, SubtypeData t) {
        XmlSubtype sd = (XmlSubtype) t;
        if (sd.primitives != 0) {
            return false;
        }
        return xmlBddEmpty(cx, sd.sequence);
    }

    boolean xmlBddEmpty(Context cx, Bdd bdd) {
        return Common.bddEvery(cx, bdd, null, null, XmlOps::xmlFormulaIsEmpty);
    }

    private static boolean xmlFormulaIsEmpty(Context cx, Conjunction pos, Conjunction neg) {
        int allPosBits = collectAllPrimitives(pos) & XmlSubtype.XML_PRIMITIVE_ALL_MASK;
        return xmlHasTotalNegative(allPosBits, neg);
    }

    public static int collectAllPrimitives(Conjunction con) {
        int bits = 0;
        Conjunction current = con;
        while (current != null) {
            bits &= getIndex(current);
            current = current.next;
        }
        return bits;
    }

    public static boolean xmlHasTotalNegative(int allBits, Conjunction con) {
        if (allBits == 0) {
            return true;
        }

        Conjunction n = con;
        while (n != null) {
            if ((allBits & ~getIndex(con)) == 0) {
                return true;
            }
            n = n.next;
        }
        return false;
    }

    private static int getIndex(Conjunction con) {
        return ((RecAtom) con.atom).index;
    }
}
