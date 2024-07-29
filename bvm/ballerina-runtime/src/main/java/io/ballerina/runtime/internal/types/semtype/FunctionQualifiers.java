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

import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.CellAtomicType;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;

public final class FunctionQualifiers {

    private static final FunctionQualifiers DEFAULT = new FunctionQualifiers(false, false);
    private final boolean isolated;
    private final boolean transactional;
    private SemType semType;

    private FunctionQualifiers(boolean isolated, boolean transactional) {
        this.isolated = isolated;
        this.transactional = transactional;
    }

    public static FunctionQualifiers create(boolean isolated, boolean transactional) {
        if (!isolated && !transactional) {
            return DEFAULT;
        }
        return new FunctionQualifiers(isolated, transactional);
    }

    synchronized SemType toSemType(Env env) {
        if (semType == null) {
            ListDefinition ld = new ListDefinition();
            SemType[] members = {
                    isolated ? Builder.booleanConst(true) : Builder.booleanType(),
                    transactional ? Builder.booleanType() : Builder.booleanConst(false)
            };
            semType = ld.defineListTypeWrapped(env, members, 2, Builder.neverType(),
                    CellAtomicType.CellMutability.CELL_MUT_NONE);
        }
        return semType;
    }

    public boolean isolated() {
        return isolated;
    }

    public boolean transactional() {
        return transactional;
    }

    @Override
    public String toString() {
        return "FunctionQualifiers[" +
                "isolated=" + isolated + ", " +
                "transactional=" + transactional + ']';
    }

}
