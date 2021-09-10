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
package io.ballerina.types;

import io.ballerina.types.definition.ListDefinition;
import io.ballerina.types.subtypedata.BooleanSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.StringSubtype;

/**
 * Public API for creating type values.
 *
 * @since 3.0.0
 */
public class SemTypes {
    public static final SemType SINT8 = IntSubtype.intWidthSigned(8);
    public static final SemType SINT16 = IntSubtype.intWidthSigned(16);
    public static final SemType SINT32 = IntSubtype.intWidthSigned(32);
    public static final SemType UINT8 = PredefinedType.BYTE;
    public static final SemType UINT16 = IntSubtype.intWidthUnsigned(16);
    public static final SemType UINT32 = IntSubtype.intWidthUnsigned(32);

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

    public static SemType union(SemType t1, SemType t2) {
        return Core.union(t1, t2);
    }

    public static SemType tuple(Env env, SemType[] members) {
        return ListDefinition.tuple(env, members);
    }

    public static boolean isSubtype(TypeCheckContext typeCheckContext, SemType t1, SemType t2) {
        return Core.isSubtype(typeCheckContext, t1, t2);
    }
}
