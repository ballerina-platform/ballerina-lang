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

import io.ballerina.types.Bdd;
import io.ballerina.types.Context;
import io.ballerina.types.RecAtom;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.UniformTypeOps;
import io.ballerina.types.XmlPrimitive;
import io.ballerina.types.subtypedata.XmlSubtype;

/**
 * Uniform subtype ops for xml type.
 *
 * @since 3.0.0
 */
public abstract class XmlCommonOps implements UniformTypeOps {

    private static final XmlSubtype xmlRoTop = XmlSubtype.from(XmlPrimitive.XML_PRIMITIVE_RO_MASK,
            BddCommonOps.bddAtom(RecAtom.createRecAtom(XmlPrimitive.XML_PRIMITIVE_RO_SINGLETON)));
    private static final XmlSubtype xmlRwTop = XmlSubtype.from(XmlPrimitive.XML_PRIMITIVE_RW_MASK,
            BddCommonOps.bddAtom(RecAtom.createRecAtom(XmlPrimitive.XML_PRIMITIVE_SINGLETON)));

    public SubtypeData commonUnion(boolean isRo, SubtypeData d1, SubtypeData d2) {
        XmlSubtype v1 = (XmlSubtype) d1;
        XmlSubtype v2 = (XmlSubtype) d2;
        int primitives = v1.primitives | v2.primitives;
        return XmlSubtype.createXmlSubtype(isRo, primitives, BddCommonOps.bddUnion(v1.sequence, v2.sequence));
    }


    public SubtypeData commonComplement(boolean isRo, SubtypeData d) {
        XmlSubtype top = isRo ? xmlRoTop : xmlRwTop;
        return diff(top, d);
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
    public boolean isEmpty(Context cx, SubtypeData t) {
        XmlSubtype sd = (XmlSubtype) t;
        if (sd.primitives != 0) {
            return false;
        }
        return xmlBddEmpty(cx, sd.sequence);
    }

    abstract boolean xmlBddEmpty(Context cx, Bdd sequence);
}
