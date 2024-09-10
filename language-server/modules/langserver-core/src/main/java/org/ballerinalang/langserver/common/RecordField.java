/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.common;

import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.ballerinalang.langserver.common.utils.RawTypeSymbolWrapper;

import java.util.Objects;

/**
 * Represents a record field.
 *
 * @since 2201.8.0
 */
public class RecordField {
    String name;
    RecordFieldSymbol fieldSymbol;
    RawTypeSymbolWrapper<RecordTypeSymbol> typeSymbolWrapper;

    public RecordField(String name, RecordFieldSymbol fieldSymbol,
                       RawTypeSymbolWrapper<RecordTypeSymbol> typeSymbolWrapper) {
        this.name = name;
        this.fieldSymbol = fieldSymbol;
        this.typeSymbolWrapper = typeSymbolWrapper;
    }

    public RawTypeSymbolWrapper<RecordTypeSymbol> getTypeSymbolWrapper() {
        return typeSymbolWrapper;
    }

    public String getName() {
        return name;
    }

    public RecordFieldSymbol getFieldSymbol() {
        return fieldSymbol;
    }

    /**
     * Record Field Identifier.
     *
     * @since 2201.8.0
     */
    public static class RecordFieldIdentifier {
        private String name;
        private TypeSymbol typeSymbol;

        public RecordFieldIdentifier(String name, TypeSymbol typeSymbol) {
            this.name = name;
            this.typeSymbol = typeSymbol;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, typeSymbol.signature());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof RecordFieldIdentifier other)) {
                return false;
            }
            return other.name.equals(this.name)
                    && other.typeSymbol.signature().equals(this.typeSymbol.signature());
        }

        public String getName() {
            return name;
        }
    }
}
