/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
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

package io.ballerina.runtime.api.values;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;

import java.util.Map;

/**
 * Ballerina runtime value representation of a singleton never value.
 * @since 2201.11.0
 */
public class BNever implements BValue {

    private static final BNever NEVER = new BNever();

    private BNever() {
    }

    public static Object getValue() {
        return NEVER;
    }

    @Override
    public String toString() {
        return "()";
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public String stringValue(BLink parent) {
        return "";
    }

    @Override
    public String expressionStringValue(BLink parent) {
        return "";
    }

    @Override
    public Type getType() {
        return PredefinedTypes.TYPE_NEVER;
    }
}
