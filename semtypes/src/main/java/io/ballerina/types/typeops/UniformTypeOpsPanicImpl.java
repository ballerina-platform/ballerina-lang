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
package io.ballerina.types.typeops;

import io.ballerina.types.SubtypeData;
import io.ballerina.types.TypeCheckContext;
import io.ballerina.types.UniformTypeOps;

/**
 * Default implementation for uniform subtypes that does not need type-ops.
 *
 * @since 2.0.0
 */
public class UniformTypeOpsPanicImpl implements UniformTypeOps {
    @Override
    public SubtypeData union(SubtypeData t1, SubtypeData t2) {
        throw new IllegalStateException("Binary operation should not be called");
    }

    @Override
    public SubtypeData intersect(SubtypeData t1, SubtypeData t2) {
        throw new IllegalStateException("Binary operation should not be called");
    }

    @Override
    public SubtypeData diff(SubtypeData t1, SubtypeData t2) {
        throw new IllegalStateException("Binary operation should not be called");
    }

    @Override
    public SubtypeData complement(SubtypeData t) {
        throw new IllegalStateException("Unary operation should not be called");
    }

    @Override
    public boolean isEmpty(TypeCheckContext tc, SubtypeData t) {
        throw new IllegalStateException("Unary boolean operation should not be called");
    }
}
