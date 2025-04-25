/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types.subtypedata;

import io.ballerina.types.Bdd;
import io.ballerina.types.CellAtomicType;
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.definition.MappingDefinition;

import java.util.List;

import static io.ballerina.types.BasicTypeCode.BT_MAPPING;
import static io.ballerina.types.BasicTypeCode.BT_TYPEDESC;
import static io.ballerina.types.Core.createBasicSemType;
import static io.ballerina.types.Core.subtypeData;

/**
 * Represent typedesc subtype.
 *
 * @since 2201.12.0
 */
public final class TypedescSubtype {

    private TypedescSubtype() {
    }

    public static SemType typedescContaining(Env env, SemType constraint) {
        if (PredefinedType.VAL.equals(constraint)) {
            return PredefinedType.TYPEDESC;
        }

        MappingDefinition mappingDef = new MappingDefinition();
        SemType mappingType = mappingDef.defineMappingTypeWrapped(env, List.of(), constraint,
                CellAtomicType.CellMutability.CELL_MUT_NONE);
        Bdd bdd = (Bdd) subtypeData(mappingType, BT_MAPPING);
        return createBasicSemType(BT_TYPEDESC, bdd);
    }
}
