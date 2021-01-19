/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.invoker.classload;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;

import java.util.Objects;

/**
 * A symbol that can be hashed to search back.
 * The symbol name and kind is used to hash the symbol.
 * No other use than to remember the symbols that were seen earlier.
 *
 * @since 2.0.0
 */
public class HashedSymbol {
    private final String name;
    private final SymbolKind kind;

    /**
     * Wraps symbol with hashed symbol to make is hashable.
     *
     * @param symbol Symbol to wrap.
     */
    public HashedSymbol(Symbol symbol) {
        this.name = symbol.name();
        this.kind = symbol.kind();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HashedSymbol xSymbol = (HashedSymbol) o;
        return name.equals(xSymbol.name) && kind == xSymbol.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, kind);
    }

    @Override
    public String toString() {
        return String.format("%s %s", kind, name);
    }
}
