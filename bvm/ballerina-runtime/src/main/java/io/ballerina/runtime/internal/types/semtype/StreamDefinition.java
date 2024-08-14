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
import io.ballerina.runtime.api.types.semtype.CellAtomicType;
import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;

import static io.ballerina.runtime.api.types.semtype.Core.createBasicSemType;
import static io.ballerina.runtime.api.types.semtype.Core.subTypeData;

public class StreamDefinition implements Definition {

    private final ListDefinition listDefinition = new ListDefinition();

    @Override
    public SemType getSemType(Env env) {
        return streamContaining(listDefinition.getSemType(env));
    }

    public SemType define(Env env, SemType valueType, SemType completionType) {
        if (Builder.valType() == completionType && Builder.valType() == valueType) {
            return Builder.streamType();
        }
        SemType tuple = listDefinition.defineListTypeWrapped(env, new SemType[]{valueType, completionType}, 2,
                Builder.neverType(), CellAtomicType.CellMutability.CELL_MUT_LIMITED);
        return streamContaining(tuple);
    }

    private SemType streamContaining(SemType tupleType) {
        SubTypeData bdd = subTypeData(tupleType, BasicTypeCode.BT_LIST);
        assert bdd instanceof Bdd;
        return createBasicSemType(BasicTypeCode.BT_STREAM, (Bdd) bdd);
    }
}
