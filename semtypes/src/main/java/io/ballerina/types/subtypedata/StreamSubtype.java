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
package io.ballerina.types.subtypedata;

import io.ballerina.types.Bdd;
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.definition.ListDefinition;

import static io.ballerina.types.BasicTypeCode.BT_LIST;
import static io.ballerina.types.BasicTypeCode.BT_STREAM;
import static io.ballerina.types.Core.createBasicSemType;
import static io.ballerina.types.Core.subtypeData;

/**
 * Represent stream subtype.
 *
 * @since 2201.10.0
 */
public final class StreamSubtype {

    private StreamSubtype() {
    }

    public static SemType streamContaining(Env env, SemType valueType) {
        return streamContaining(env, valueType, PredefinedType.NIL);
    }

    public static SemType streamContaining(Env env, SemType valueType, SemType completionType) {
        if (PredefinedType.VAL.equals(completionType) && PredefinedType.VAL.equals(valueType)) {
            return PredefinedType.STREAM;
        }

        ListDefinition listDef = new ListDefinition();
        SemType mappingType = listDef.tupleTypeWrapped(env, valueType, completionType);
        Bdd bdd = (Bdd) subtypeData(mappingType, BT_LIST);
        return createBasicSemType(BT_STREAM, bdd);
    }
}
