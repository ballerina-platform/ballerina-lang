/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.model.types.Type;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code Const} represent a Constant declaration
 *
 * @since 1.0.0
 */
public class Const {

    private Type type;
    private Identifier identifier;
    private BValue value;

    /**
     * Constructing a Ballerina Const Statement.
     *
     * @param type       Type of the constant
     * @param identifier Identifier of the constant
     * @param value      bValueRef of the constant
     */
    public Const(Type type, Identifier identifier, BValue value) {
        this.type = type;
        this.identifier = identifier;
        this.value = value;
    }

    /**
     * Get the type of the constant
     *
     * @return type of the constant
     */
    public Type getType() {
        return type;
    }

    /**
     * Get the identifier of the constant declaration
     *
     * @return identifier of the constant declaration
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Get the bValue of the constant
     *
     * @return bValue of the constant
     */
    public BValue getValue() {
        return value;
    }
}
