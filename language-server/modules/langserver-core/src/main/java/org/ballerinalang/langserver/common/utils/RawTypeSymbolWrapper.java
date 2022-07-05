/*
 *  Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.common.utils;

import io.ballerina.compiler.api.symbols.TypeSymbol;

/**
 * A utility bean to hold a record's raw type symbol and broader type symbol (type reference, etc.).
 *
 * @param <T> Raw TypeSymbol type
 * @since 2201.1.1
 */
public class RawTypeSymbolWrapper<T extends TypeSymbol> {

    private final TypeSymbol broaderType;

    private final T rawType;

    private RawTypeSymbolWrapper(TypeSymbol broaderType, T rawType) {
        this.broaderType = broaderType;
        this.rawType = rawType;
    }

    public TypeSymbol getBroaderType() {
        return broaderType;
    }

    public T getRawType() {
        return rawType;
    }

    public static <T extends TypeSymbol> RawTypeSymbolWrapper<T> from(TypeSymbol broaderType, T typeSymbol) {
        return new RawTypeSymbolWrapper<T>(broaderType, typeSymbol);
    }
}
