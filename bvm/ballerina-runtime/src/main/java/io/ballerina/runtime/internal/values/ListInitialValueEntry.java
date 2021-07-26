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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.values.BListInitialValueEntry;

/**
 * Represents an initial value entry in a list constructor expression.
 *
 * @since 1.3.0
 */
public abstract class ListInitialValueEntry implements BListInitialValueEntry {

    /**
     * Represents an initial expression entry in a list constructor expression.
     *
     * @since 1.3.0
     */
    public static class ExpressionEntry extends ListInitialValueEntry {

        public Object value;

        public ExpressionEntry(Object value) {
            this.value = value;
        }
    }
}
