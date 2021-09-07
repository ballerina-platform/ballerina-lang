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

import io.ballerina.types.PredefinedType;
import io.ballerina.types.ProperSubtypeData;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.UniformTypeCode;

import java.util.Optional;

/**
 * Represent BooleanSubtype.
 *
 * @since 2.0.0
 */
public class BooleanSubtype implements ProperSubtypeData {
    public final boolean value;

    private BooleanSubtype(boolean value) {
        this.value = value;
    }

    public static BooleanSubtype from(boolean value) {
        return new BooleanSubtype(value);
    }

    public static boolean booleanSubtypeContains(SubtypeData d, boolean b) {
        if (d instanceof AllOrNothingSubtype) {
            return ((AllOrNothingSubtype) d).isAllSubtype();
        }
        BooleanSubtype r = (BooleanSubtype) d;
        return r.value == b;
    }

    public static SemType booleanConst(boolean value)  {
        BooleanSubtype t = BooleanSubtype.from(value);
        return PredefinedType.uniformSubtype(UniformTypeCode.UT_BOOLEAN, t);
    }

    public static Optional<Boolean> booleanSubtypeSingleValue(SubtypeData d) {
        if (d instanceof AllOrNothingSubtype) {
            return Optional.empty();
        }
        BooleanSubtype b = (BooleanSubtype) d;
        return Optional.of(b.value);
    }
}
