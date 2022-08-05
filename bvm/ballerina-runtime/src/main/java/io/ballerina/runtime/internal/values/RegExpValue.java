/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;

import java.util.Map;
import java.util.Objects;

import static io.ballerina.runtime.internal.ValueUtils.getTypedescValue;

/**
 * <p>
 * Represents a regular expression in ballerina.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *
 * @since 2201.3.0
 */
public class RegExpValue implements RefValue {
    private final String pattern;
    private BTypedesc typedesc;
    private final Type type = PredefinedTypes.TYPE_STRING_REG_EXP;

    public RegExpValue(BString pattern) {
        this.pattern = pattern.getValue();
        setTypedescValue(this.type);
    }

    public String getRegExpValue() {
        return this.pattern;
    }

    public boolean isEmpty() {
        return this.pattern.isEmpty();
    }

    @Override
    public BTypedesc getTypedesc() {
        return this.typedesc;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return this;
    }

    @Override
    public String stringValue(BLink parent) {
        return this.pattern;
    }

    @Override
    public String expressionStringValue(BLink parent) {
        return "re " + "`" + stringValue(parent) + "`";
    }

    @Override
    public String informalStringValue(BLink parent) {
        return "`" + stringValue(parent) + "`";
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pattern);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    protected void setTypedescValue(Type type) {
        this.typedesc = getTypedescValue(type, this);
    }
}
