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
package io.ballerina.types.subtypedata;

import io.ballerina.types.Bdd;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Conjunction;
import io.ballerina.types.Core;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.ProperSubtypeData;
import io.ballerina.types.RecAtom;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.UniformSubtype;
import io.ballerina.types.UniformTypeBitSet;
import io.ballerina.types.UniformTypeCode;
import io.ballerina.types.XmlPrimitive;
import io.ballerina.types.typeops.BddCommonOps;

/**
 * Implementation specific to basic type xml.
 *
 * @since 3.0.0
 */
public class XmlSubtype implements ProperSubtypeData {
    public int primitives;
    public Bdd sequence;

    private XmlSubtype(int primitives, Bdd sequence) {
        this.primitives = primitives;
        this.sequence = sequence;
    }

    public static XmlSubtype from(int primitives, Bdd sequence) {
        return new XmlSubtype(primitives, sequence);
    }

    public static SemType xmlSingleton(int primitives) {
        return createXmlSemtype(
                createXmlSubtype(true, primitives, BddAllOrNothing.bddNothing()),
                createXmlSubtype(false, primitives, BddAllOrNothing.bddNothing())
        );
    }

    public static SemType xmlSequence(SemType constituentType) {
        if (constituentType == PredefinedType.NEVER) {
            return xmlSequence(xmlSingleton(XmlPrimitive.XML_PRIMITIVE_NEVER));
        }
        if (constituentType instanceof UniformTypeBitSet) {
            return constituentType;
        } else {
            ComplexSemType cct = (ComplexSemType) constituentType; 
            SubtypeData ro = Core.getComplexSubtypeData(cct, UniformTypeCode.UT_XML_RO);
            ro = (ro instanceof AllOrNothingSubtype) ? ro : makeSequence(true, (XmlSubtype) ro);

            SubtypeData rw = Core.getComplexSubtypeData(cct, UniformTypeCode.UT_XML_RW);
            rw = (rw instanceof AllOrNothingSubtype) ? rw : makeSequence(false, (XmlSubtype) rw);

            return createXmlSemtype(ro, rw);
        }
    }

    private static SubtypeData makeSequence(boolean roPart, XmlSubtype d) {
        int primitives = XmlPrimitive.XML_PRIMITIVE_NEVER | d.primitives;
        int atom = d.primitives &
                (roPart ? XmlPrimitive.XML_PRIMITIVE_RO_SINGLETON : XmlPrimitive.XML_PRIMITIVE_SINGLETON);
        Bdd sequence = BddCommonOps.bddUnion(BddCommonOps.bddAtom(RecAtom.createRecAtom(atom)), d.sequence);
        return createXmlSubtype(roPart, primitives, sequence);
    }

    public static ComplexSemType createXmlSemtype(SubtypeData ro, SubtypeData rw) {
        if (ro instanceof AllOrNothingSubtype && ((AllOrNothingSubtype) ro).isNothingSubtype()) {
            return ComplexSemType.createComplexSemType(0, UniformSubtype.from(UniformTypeCode.UT_XML_RW, rw));
        } else if (rw instanceof AllOrNothingSubtype && ((AllOrNothingSubtype) rw).isNothingSubtype()) {
            return ComplexSemType.createComplexSemType(0, UniformSubtype.from(UniformTypeCode.UT_XML_RO, ro));
        } else {
            return ComplexSemType.createComplexSemType(0, UniformSubtype.from(UniformTypeCode.UT_XML_RO, ro),
                    UniformSubtype.from(UniformTypeCode.UT_XML_RW, rw));
        }
    }

    public static SubtypeData createXmlSubtype(boolean isRo, int primitives, Bdd sequence) {
        int mask = isRo ? XmlPrimitive.XML_PRIMITIVE_RO_MASK : XmlPrimitive.XML_PRIMITIVE_RW_MASK;
        int p = primitives & mask;
        if (sequence instanceof BddAllOrNothing && ((BddAllOrNothing) sequence).isAll() && p == mask) {
            return AllOrNothingSubtype.createAll();
        }
        return createXmlSubtypeOrEmpty(p, sequence);
    }

    public static SubtypeData createXmlSubtypeOrEmpty(int primitives, Bdd sequence) {
        if (sequence instanceof BddAllOrNothing  && ((BddAllOrNothing) sequence).isNothing() && primitives == 0) {
            return AllOrNothingSubtype.createNothing();
        }
        return from(primitives, sequence);
    }

    public static int collectAllBits(Conjunction con) {
        int allBits = 0;
        Conjunction current = con;
        while (current != null) {
            allBits |= current.atom.getIndex();
            current = current.next;
        }
        return allBits;
    }

    public static boolean hasTotalNegative(int allBits, Conjunction con) {
        if (allBits == 0) {
            return true;
        }

        Conjunction n = con;
        while (n != null) {
            if ((allBits & ~n.atom.getIndex()) == 0) {
                return true;
            }
            n = n.next;
        }
        return false;
    }
}
