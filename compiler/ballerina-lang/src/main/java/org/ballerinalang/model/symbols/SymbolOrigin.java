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
            // Returning COMPILED_SOURCE here since once a BIR is generated for a source, it's in a compiled form.
            // i.e., it's no longer originating from a source file
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

    /**
     * This method is intended for to be used during the BIR gen phase. The purpose is to get the correct origin for the
     * symbol in question once it is in BIR form.
     *
     * @return The origin enum to be used in the BIR
     */
    public abstract SymbolOrigin toBIROrigin();

    /**
     * Returns the serializable form of the enum.
     *
     * @return The value of the enum
     */
    public abstract byte value();

    /**
     * Maps a given integer to a SymbolOrigin enum.
     *
     * @param value The value to be converted to an enum
     * @return The corresponding enum value if there's a corresponding value. If not, an exception is thrown.
     */
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
