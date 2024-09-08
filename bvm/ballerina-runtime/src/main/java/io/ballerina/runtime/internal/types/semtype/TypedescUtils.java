/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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
 *
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.BasicTypeCode;
import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;

import static io.ballerina.runtime.api.types.semtype.Core.createBasicSemType;
import static io.ballerina.runtime.api.types.semtype.Core.subTypeData;

public final class TypedescUtils {

    private static final MappingDefinition.Field[] EMPTY_FIELDS = new MappingDefinition.Field[0];

    private TypedescUtils() {

    }

    public static SemType typedescContaining(Env env, SemType constraint) {
        if (constraint == Builder.valType()) {
            return Builder.typeDescType();
        }
        MappingDefinition md = new MappingDefinition();
        SemType mappingType = md.defineMappingTypeWrapped(env, EMPTY_FIELDS, constraint,
                CellAtomicType.CellMutability.CELL_MUT_NONE);
        Bdd bdd = (Bdd) subTypeData(mappingType, BasicTypeCode.BT_MAPPING);
        return createBasicSemType(BasicTypeCode.BT_TYPEDESC, bdd);
    }
}
