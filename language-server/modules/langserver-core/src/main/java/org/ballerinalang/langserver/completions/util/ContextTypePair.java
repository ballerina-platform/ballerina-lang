/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.completions.util;

import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.apache.commons.lang3.tuple.Pair;

/**
 * A type holder which holds a pair of types which is resolved by the ContextTypeResolver.
 * Captures the raw type and the associated broader type.
 * The values of the raw and the broader types are strictly bound to the resolved context.
 *
 * @since 2.0.0
 */
public class ContextTypePair {
    Pair<TypeSymbol, TypeSymbol> pair;

    private ContextTypePair(Pair<TypeSymbol, TypeSymbol> pair) {
        this.pair = pair;
    }

    public static ContextTypePair of(final TypeSymbol rawType, final TypeSymbol broaderType) {
        if (rawType == null || broaderType == null) {
            throw new IllegalArgumentException("A pair cannot be constructed with null types");
        }
        return new ContextTypePair(Pair.of(rawType, broaderType));
    }

    /**
     * Get the associated raw type.
     *
     * @return {@link TypeSymbol}
     */
    public TypeSymbol getRawType() {
        return pair.getLeft();
    }

    /**
     * Get the associated broader type.
     *
     * @return {@link TypeSymbol}
     */
    public TypeSymbol getBroaderType() {
        return pair.getRight();
    }
}
