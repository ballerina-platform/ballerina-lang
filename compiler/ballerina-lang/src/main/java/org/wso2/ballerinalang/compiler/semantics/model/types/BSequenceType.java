/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model.types;

import io.ballerina.types.Env;
import io.ballerina.types.SemType;
import io.ballerina.types.definition.ListDefinition;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_LIMITED;

/**
 * Represents type for sequence variable.
 *
 * @since 2201.7.0
 */
public class BSequenceType extends BType {
    public BType elementType;
    private final Env env;

    public BSequenceType(Env env, BType elementType) {
        super(TypeTags.SEQUENCE, null);
        this.elementType = elementType;
        this.env = env;
    }

    @Override
    public String toString() {
        return "seq " + elementType;
    }

    public SemType semType() {
        if (this.semType != null) {
            return this.semType;
        }
        ListDefinition ld = new ListDefinition();
        this.semType = ld.defineListTypeWrapped(env, List.of(), 0, elementType.semType(), CELL_MUT_LIMITED);
        return this.semType;
    }
}
