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
import io.ballerina.types.Common;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Core;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.ProperSubtypeData;
import io.ballerina.types.RecAtom;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.UniformSubtype;
import io.ballerina.types.UniformTypeBitSet;
import io.ballerina.types.UniformTypeCode;
import io.ballerina.types.typeops.BddCommonOps;

/**
 * Implementation specific to basic type xml.
 *
 * @since 3.0.0
 */
public class XmlSubtype implements ProperSubtypeData {
    public final int primitives;
    public final Bdd sequence;

    public static final int XML_PRIMITIVE_NEVER      = 1;
    public static final int XML_PRIMITIVE_TEXT       = 1 << 1;
    public static final int XML_PRIMITIVE_ELEMENT_RO = 1 << 2;
    public static final int XML_PRIMITIVE_PI_RO      = 1 << 3;
    public static final int XML_PRIMITIVE_COMMENT_RO = 1 << 4;
    public static final int XML_PRIMITIVE_ELEMENT_RW = 1 << 5;
    public static final int XML_PRIMITIVE_PI_RW      = 1 << 6;
    public static final int XML_PRIMITIVE_COMMENT_RW = 1 << 7;

    public static final int XML_PRIMITIVE_RO_SINGLETON = XML_PRIMITIVE_TEXT | XML_PRIMITIVE_ELEMENT_RO
            | XML_PRIMITIVE_PI_RO | XML_PRIMITIVE_COMMENT_RO;
    public static final int XML_PRIMITIVE_RO_MASK = XML_PRIMITIVE_NEVER | XML_PRIMITIVE_RO_SINGLETON;
    public static final int XML_PRIMITIVE_RW_MASK = XML_PRIMITIVE_ELEMENT_RW | XML_PRIMITIVE_PI_RW
            | XML_PRIMITIVE_COMMENT_RW;
    public static final int XML_PRIMITIVE_SINGLETON = XML_PRIMITIVE_RO_SINGLETON | XML_PRIMITIVE_RW_MASK;

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
            return xmlSequence(xmlSingleton(XML_PRIMITIVE_NEVER));
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
        int primitives = XML_PRIMITIVE_NEVER | d.primitives;
        int atom = d.primitives &
                (roPart ? XML_PRIMITIVE_RO_SINGLETON : XML_PRIMITIVE_SINGLETON);
        Bdd sequence = BddCommonOps.bddUnion(BddCommonOps.bddAtom(RecAtom.createRecAtom(atom)), d.sequence);
        return createXmlSubtype(roPart, primitives, sequence);
    }

    public static ComplexSemType createXmlSemtype(SubtypeData ro, SubtypeData rw) {
        if (Common.isNothingSubtype(ro)) {
            return ComplexSemType.createComplexSemType(0, UniformSubtype.from(UniformTypeCode.UT_XML_RW, rw));
        } else if (Common.isNothingSubtype(rw)) {
            return ComplexSemType.createComplexSemType(0, UniformSubtype.from(UniformTypeCode.UT_XML_RO, ro));
        } else {
            return ComplexSemType.createComplexSemType(0, UniformSubtype.from(UniformTypeCode.UT_XML_RO, ro),
                    UniformSubtype.from(UniformTypeCode.UT_XML_RW, rw));
        }
    }

    public static SubtypeData createXmlSubtype(boolean isRo, int primitives, Bdd sequence) {
        int mask = isRo ? XML_PRIMITIVE_RO_MASK : XML_PRIMITIVE_RW_MASK;
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
}
