/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.api.values;

import io.ballerina.runtime.api.types.Type;

import java.util.Map;

/**
 * <p>
 * Represents all the ballerina values.
 * </p>
 * 
 * @since 1.1.0
 */
public interface BValue {

    /**
     * Method to perform a deep copy, recursively copying all structural values and their members.
     *
     * @param refs The map which keep track of the references of already cloned values in cycles
     *
     * @return  A new copy of the value
     */
    Object copy(Map<Object, Object> refs);

    /**
     * Method to performs a deep copy, recursively copying all structural values and their members but the created
     * clone is a read-only value.
     *
     * @param refs The map which keep track of the references of already cloned values in cycles
     *
     * @return  A new copy of the value
     */
    Object frozenCopy(Map<Object, Object> refs);

    String stringValue(BLink parent);

    default String informalStringValue(BLink parent) {
        return toString();
    }

    String expressionStringValue(BLink parent);

    Type getType();
}
