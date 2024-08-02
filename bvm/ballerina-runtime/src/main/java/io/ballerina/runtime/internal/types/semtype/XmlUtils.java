/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.BasicTypeCode;
import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.BddAllOrNothing;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.RecAtom;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.SubType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.ballerina.runtime.api.types.semtype.BddNode.bddAtom;

// TODO: this should be a part of the public API
public final class XmlUtils {

    public static final int XML_PRIMITIVE_NEVER = 1;
    public static final int XML_PRIMITIVE_TEXT = 1 << 1;
    public static final int XML_PRIMITIVE_ELEMENT_RO = 1 << 2;
    public static final int XML_PRIMITIVE_PI_RO = 1 << 3;
    public static final int XML_PRIMITIVE_COMMENT_RO = 1 << 4;
    public static final int XML_PRIMITIVE_ELEMENT_RW = 1 << 5;
    public static final int XML_PRIMITIVE_PI_RW = 1 << 6;
    public static final int XML_PRIMITIVE_COMMENT_RW = 1 << 7;

    public static final int XML_PRIMITIVE_RO_SINGLETON = XML_PRIMITIVE_TEXT | XML_PRIMITIVE_ELEMENT_RO
            | XML_PRIMITIVE_PI_RO | XML_PRIMITIVE_COMMENT_RO;
    public static final int XML_PRIMITIVE_RO_MASK = XML_PRIMITIVE_NEVER | XML_PRIMITIVE_RO_SINGLETON;
    public static final int XML_PRIMITIVE_RW_MASK = XML_PRIMITIVE_ELEMENT_RW | XML_PRIMITIVE_PI_RW
            | XML_PRIMITIVE_COMMENT_RW;
    public static final int XML_PRIMITIVE_SINGLETON = XML_PRIMITIVE_RO_SINGLETON | XML_PRIMITIVE_RW_MASK;
    public static final int XML_PRIMITIVE_ALL_MASK = XML_PRIMITIVE_RO_MASK | XML_PRIMITIVE_RW_MASK;

    public static final SubTypeData XML_SUBTYPE_TOP = from(XML_PRIMITIVE_ALL_MASK, BddAllOrNothing.ALL);
    public static final SubType XML_SUBTYPE_RO =
            BXmlSubType.createDelegate(XML_PRIMITIVE_RO_MASK,
                    bddAtom(RecAtom.createRecAtom(XML_PRIMITIVE_RO_SINGLETON)));

    private XmlUtils() {
    }

    public static SemType xmlSingleton(int primitive) {
        if (XmlSingletonCache.isCached(primitive)) {
            return XmlSingletonCache.get(primitive);
        }
        return createXmlSingleton(primitive);
    }

    private static SemType createXmlSingleton(int primitive) {
        return createXmlSemtype(createXmlSubtype(primitive, BddAllOrNothing.NOTHING));
    }

    private static SemType createXmlSemtype(SubTypeData xmlSubtype) {
        if (xmlSubtype instanceof AllOrNothing) {
            return xmlSubtype == AllOrNothing.ALL ? Builder.xmlType() : Builder.neverType();
        }
        assert xmlSubtype instanceof BXmlSubType : "subtype must be wrapped by delegate by now";
        return Builder.basicSubType(BasicTypeCode.BT_XML, (SubType) xmlSubtype);
    }

    private static SubTypeData createXmlSubtype(int primitives, Bdd sequence) {
        int p = primitives & XML_PRIMITIVE_ALL_MASK;
        if (primitiveShouldIncludeNever(p)) {
            p |= XML_PRIMITIVE_NEVER;
        }
        if (sequence == BddAllOrNothing.ALL && p == XML_PRIMITIVE_ALL_MASK) {
            return AllOrNothing.ALL;
        } else if (sequence == BddAllOrNothing.NOTHING && p == 0) {
            return AllOrNothing.NOTHING;
        }
        return from(p, sequence);
    }

    private static boolean primitiveShouldIncludeNever(int primitive) {
        return (primitive & XML_PRIMITIVE_TEXT) == XML_PRIMITIVE_TEXT;
    }

    public static SubTypeData from(int primitives, Bdd sequence) {
        return BXmlSubType.createDelegate(primitives, sequence);
    }

    public static SemType xmlSequence(SemType constituentType) {
        assert Core.isSubtypeSimple(constituentType, Builder.xmlType()) :
                "It is a precondition that constituentType is a subtype of XML";
        if (Core.isNever(constituentType)) {
            return xmlSequence(xmlSingleton(XML_PRIMITIVE_NEVER));
        } else if (constituentType.some() == 0) {
            assert Core.isNever(Core.diff(Builder.xmlType(), constituentType));
            return constituentType;
        } else {
            SubType xmlSubType =
                    Core.getComplexSubtypeData(constituentType, BasicTypeCode.BT_XML);
            if (!xmlSubType.isAll() && !xmlSubType.isNothing()) {
                xmlSubType = makeXmlSequence((BXmlSubType) xmlSubType);
            }
            return createXmlSemtype((SubTypeData) xmlSubType);
        }
    }

    private static SubType makeXmlSequence(BXmlSubType xmlSubType) {
        int primitives = xmlSubType.primitives() | XML_PRIMITIVE_NEVER;
        int atom = xmlSubType.primitives() & XML_PRIMITIVE_SINGLETON;
        Bdd sequence = (Bdd) xmlSubType.bdd().union(bddAtom(RecAtom.createRecAtom(atom)));
        return BXmlSubType.createDelegate(primitives, sequence);
    }

    private static final class XmlSingletonCache {

        private static final Map<Integer, SemType> CACHE = new ConcurrentHashMap<>();

        private static boolean isCached(int primitive) {
            return Integer.bitCount(primitive) < 3;
        }

        private static SemType get(int primitive) {
            return CACHE.computeIfAbsent(primitive, XmlUtils::createXmlSingleton);
        }

    }
}
