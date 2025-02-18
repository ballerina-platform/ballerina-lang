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
package io.ballerina.types.definition;

import io.ballerina.types.Bdd;
import io.ballerina.types.Definition;
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;

import static io.ballerina.types.BasicTypeCode.BT_LIST;
import static io.ballerina.types.BasicTypeCode.BT_STREAM;
import static io.ballerina.types.Core.createBasicSemType;
import static io.ballerina.types.Core.subtypeData;

/**
 * Represent stream type desc.
 *
 * @since 2201.12.0
 */
public final class StreamDefinition implements Definition {

    private final ListDefinition listDefinition = new ListDefinition();

    @Override
    public SemType getSemType(Env env) {
        return streamContaining((listDefinition.getSemType(env)));
    }

    public SemType define(Env env, SemType valueTy, SemType completionTy) {
        if (PredefinedType.VAL.equals(completionTy) && PredefinedType.VAL.equals(valueTy)) {
            return PredefinedType.STREAM;
        }
        SemType tuple = listDefinition.tupleTypeWrapped(env, valueTy, completionTy);
        return streamContaining(tuple);
    }

    private static SemType streamContaining(SemType tupleType) {
        SubtypeData bdd = subtypeData(tupleType, BT_LIST);
        assert bdd instanceof Bdd;
        return createBasicSemType(BT_STREAM, bdd);
    }
}
