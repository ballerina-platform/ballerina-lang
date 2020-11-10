/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.core.model;

import org.ballerinalang.core.model.symbols.BLangSymbol;
import org.ballerinalang.core.model.types.BType;
import org.ballerinalang.core.model.types.SimpleTypeName;

/**
 * {@code VariableDef} represent a Variable definition.
 * <p>
 * Ballerina has variables of various types. The type system includes built-in primitives,
 * a collection of built-in structured types and arrays and record type constructors, and function pointers.
 *
 * @since 0.8.0
 */
public interface VariableDef extends BLangSymbol, Node {

    /**
     * Represents the variable kind.
     */
    enum Kind {
        /**
         * Local variable in a function/action/resource.
         */
        LOCAL_VAR,

        /**
         * Constant.
         */
        CONSTANT,

        /**
         * Global variable.
         */
        GLOBAL_VAR,

        /**
         * Variable defined in the service level.
         */
        SERVICE_VAR,

        /**
         * Variable defined in the connector level.
         */
        CONNECTOR_VAR,

        /**
         * Struct field.
         */
        STRUCT_FIELD
    }

    BType getType();

    void setType(BType type);

    Kind getKind();

    void setKind(Kind kind);

    int getVarIndex();

    void setVarIndex(int index);

    SimpleTypeName getTypeName();
}
