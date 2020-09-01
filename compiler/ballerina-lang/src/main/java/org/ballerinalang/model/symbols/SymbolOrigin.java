/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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

package org.ballerinalang.model.symbols;

/**
 * Represents the various origination points of a symbol.
 *
 * @since 2.0.0
 */
public enum SymbolOrigin {

    /**
     * These are symbols which are defined internally by the compiler. e.g., symbols defined in the symbol table,
     * symbols defined in lang lib
     */
    BUILTIN {
        @Override
        public SymbolOrigin toBIROrigin() {
            return BUILTIN;
        }

        @Override
        public byte value() {
            return 1;
        }
    },

    /**
     * These are symbols for which there is a corresponding construct in the source file written by the user.
     */
    SOURCE {
        @Override
        public SymbolOrigin toBIROrigin() {
            return COMPILED_SOURCE;
        }

        @Override
        public byte value() {
            return 2;
        }
    },

    /**
     * These are symbols which are coming from a compiled source. i.e., read through the BIRPackageSymbolEnter
     */
    COMPILED_SOURCE {
        @Override
        public SymbolOrigin toBIROrigin() {
            return COMPILED_SOURCE;
        }

        @Override
        public byte value() {
            return 3;
        }
    },

    /**
     * Represents symbols created for desugared constructs or internal symbols defined for the use of the compiler.
     * These symbols should not be exposed to the public through the semantic API.
     */
    VIRTUAL {
        @Override
        public SymbolOrigin toBIROrigin() {
            return VIRTUAL;
        }

        @Override
        public byte value() {
            return 4;
        }
    };

    public abstract SymbolOrigin toBIROrigin();

    public abstract byte value();

    public static SymbolOrigin toOrigin(byte value) {
        switch (value) {
            case 1:
                return BUILTIN;
            case 2:
                return SOURCE;
            case 3:
                return COMPILED_SOURCE;
            case 4:
                return VIRTUAL;
            default:
                throw new IllegalStateException("Invalid symbol origin value: " + value);
        }
    }
}
