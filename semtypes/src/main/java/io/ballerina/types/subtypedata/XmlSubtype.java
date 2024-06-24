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
package io.ballerina.types.subtypedata;

import io.ballerina.types.BasicTypeBitSet;
import io.ballerina.types.BasicTypeCode;
import io.ballerina.types.Bdd;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Core;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.ProperSubtypeData;
import io.ballerina.types.RecAtom;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.typeops.BddCommonOps;

/**
 * Implementation specific to basic type xml.
 *
 * @since 2201.8.0
 */
public class XmlSubtype implements ProperSubtypeData {
    // This is the bitwise-or of above XML_PRIMITIVE_* fields.
    // If the XML_PRIMITIVE_NEVER bit is set, then the empty XML sequence belongs to the type.
    // If one of the other XML_PRIMITVE_* bits is set, then the type contains the
    // corresponding singleton type.
    public final int primitives;
    // This is a logical combination of the allowed sequences types. The `atom` field of
    // the `BddNode` is a bitwise-or of XML_PRIMTIVE_* (except for XML_PRIMITIVE_NEVER).
    // It represents a sequence of two or more singletons, where the allowed singletons
    // are those whose bit is set in the `atom` field.
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
    public static final int XML_PRIMITIVE_ALL_MASK = XML_PRIMITIVE_RO_MASK | XML_PRIMITIVE_RW_MASK;

    private XmlSubtype(int primitives, Bdd sequence) {
        this.primitives = primitives;
        this.sequence = sequence;
    }

    public static XmlSubtype from(int primitives, Bdd sequence) {
        return new XmlSubtype(primitives, sequence);
    }

    public static SemType xmlSingleton(int primitives) {
        return createXmlSemtype(createXmlSubtype(primitives, BddAllOrNothing.bddNothing()));
    }

    public static SemType xmlSequence(SemType constituentType) {
        // It is a precondition that constituentType is a subtype of XML
        assert Core.isSubtypeSimple(constituentType, PredefinedType.XML);

        if (Core.isNever(constituentType)) {
            return xmlSequence(xmlSingleton(XML_PRIMITIVE_NEVER));
        }
        if (constituentType instanceof BasicTypeBitSet) {
            return constituentType;
        } else {
            ComplexSemType cct = (ComplexSemType) constituentType;
            SubtypeData xmlSubtype = Core.getComplexSubtypeData(cct, BasicTypeCode.BT_XML);
            xmlSubtype = (xmlSubtype instanceof AllOrNothingSubtype) ?
                    xmlSubtype : makeXmlSequence((XmlSubtype) xmlSubtype);
            return createXmlSemtype(xmlSubtype);
        }
    }

    private static SubtypeData makeXmlSequence(XmlSubtype d) {
        int primitives = XML_PRIMITIVE_NEVER | d.primitives;
        int atom = d.primitives & XML_PRIMITIVE_SINGLETON;
        Bdd sequence = BddCommonOps.bddUnion(BddCommonOps.bddAtom(RecAtom.createXMLRecAtom(atom)), d.sequence);
        return createXmlSubtype(primitives, sequence);
    }

    public static SemType createXmlSemtype(SubtypeData xmlSubtype) {
        if (xmlSubtype instanceof AllOrNothingSubtype allOrNothingSubtype) {
            return allOrNothingSubtype.isAllSubtype() ? PredefinedType.XML : PredefinedType.NEVER;
        } else {
            return PredefinedType.basicSubtype(BasicTypeCode.BT_XML, (ProperSubtypeData) xmlSubtype);
        }
    }

    public static SubtypeData createXmlSubtype(int primitives, Bdd sequence) {
        int p = primitives & XML_PRIMITIVE_ALL_MASK;
        if (sequence instanceof BddAllOrNothing allOrNothing && allOrNothing.isAll() &&
                p == XML_PRIMITIVE_ALL_MASK) {
            return AllOrNothingSubtype.createAll();
        }
        return createXmlSubtypeOrEmpty(p, sequence);
    }

    public static SubtypeData createXmlSubtypeOrEmpty(int primitives, Bdd sequence) {
        if (sequence instanceof BddAllOrNothing allOrNothing && allOrNothing.isNothing() && primitives == 0) {
            return AllOrNothingSubtype.createNothing();
        }
        return from(primitives, sequence);
    }
}
