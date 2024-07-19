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
package io.ballerina.types;

import io.ballerina.types.definition.ObjectDefinition;
import io.ballerina.types.subtypedata.BooleanSubtype;
import io.ballerina.types.subtypedata.DecimalSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.FutureSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.StringSubtype;
import io.ballerina.types.subtypedata.TableSubtype;
import io.ballerina.types.subtypedata.TypedescSubtype;
import io.ballerina.types.subtypedata.XmlSubtype;
import io.ballerina.types.typeops.ListProj;

import java.math.BigDecimal;

/**
 * Public API for creating type values.
 *
 * @since 2201.8.0
 */
public final class SemTypes {
    public static final SemType SINT8 = IntSubtype.intWidthSigned(8);
    public static final SemType SINT16 = IntSubtype.intWidthSigned(16);
    public static final SemType SINT32 = IntSubtype.intWidthSigned(32);
    public static final SemType UINT8 = PredefinedType.BYTE;
    public static final SemType UINT16 = IntSubtype.intWidthUnsigned(16);
    public static final SemType UINT32 = IntSubtype.intWidthUnsigned(32);
    public static final SemType CHAR = PredefinedType.STRING_CHAR;
    public static final SemType XML_ELEMENT = PredefinedType.XML_ELEMENT;
    public static final SemType XML_COMMENT = PredefinedType.XML_COMMENT;
    public static final SemType XML_TEXT = PredefinedType.XML_TEXT;
    public static final SemType XML_PI = PredefinedType.XML_PI;

    public static SemType floatConst(double v) {
        return FloatSubtype.floatConst(v);
    }

    public static SemType intConst(long value) {
        return IntSubtype.intConst(value);
    }

    public static SemType stringConst(String value) {
        return StringSubtype.stringConst(value);
    }

    public static SemType booleanConst(boolean value) {
        return BooleanSubtype.booleanConst(value);
    }

    public static SemType decimalConst(String value) {
        if (value.contains("d") || value.contains("D")) {
            value = value.substring(0, value.length() - 1);
        }
        BigDecimal d = new BigDecimal(value);
        return DecimalSubtype.decimalConst(d);
    }

    public static SemType union(SemType t1, SemType t2) {
        return Core.union(t1, t2);
    }

    public static SemType union(SemType first, SemType second, SemType... rest) {
        SemType u = Core.union(first, second);
        for (SemType s : rest) {
            u = Core.union(u, s);
        }
        return u;
    }

    public static SemType intersect(SemType t1, SemType t2) {
        return Core.intersect(t1, t2);
    }

    public static SemType intersect(SemType first, SemType second, SemType... rest) {
        SemType i = Core.intersect(first, second);
        for (SemType s : rest) {
            i = Core.intersect(i, s);
        }
        return i;
    }

    public static boolean isSubtype(Context context, SemType t1, SemType t2) {
        return Core.isSubtype(context, t1, t2);
    }

    public static boolean isSubtypeSimple(SemType t1, BasicTypeBitSet t2) {
        return Core.isSubtypeSimple(t1, t2);
    }

    public static boolean isSameType(Context context, SemType t1, SemType t2) {
        return Core.isSameType(context, t1, t2);
    }

    public static SemType errorDetail(SemType detail) {
        return Error.errorDetail(detail);
    }

    public static SemType errorDistinct(int distinctId) {
        return Error.errorDistinct(distinctId);
    }

    public static SemType objectDistinct(int distinctId) {
        return ObjectDefinition.distinct(distinctId);
    }

    public static SemType tableContaining(Env env, SemType mappingType) {
        return TableSubtype.tableContaining(env, mappingType);
    }

    public static SemType futureContaining(Env env, SemType constraint) {
        return FutureSubtype.futureContaining(env, constraint);
    }

    public static SemType typedescContaining(Env env, SemType constraint) {
        return TypedescSubtype.typedescContaining(env, constraint);
    }

    public static SemType mappingMemberTypeInnerVal(Context context, SemType t, SemType m) {
        return Core.mappingMemberTypeInnerVal(context, t, m);
    }

    public static SemType listProj(Context context, SemType t, SemType key) {
        return ListProj.listProjInnerVal(context, t, key);
    }

    public static SemType listMemberType(Context context, SemType t, SemType key) {
        return Core.listMemberTypeInnerVal(context, t, key);
    }

    public static SemType xmlSequence(SemType t) {
        return XmlSubtype.xmlSequence(t);
    }
}
