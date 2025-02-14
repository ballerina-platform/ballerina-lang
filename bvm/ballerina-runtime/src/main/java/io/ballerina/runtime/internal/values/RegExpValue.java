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

import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.semtype.BasicTypeBitSet;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BRegexpValue;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.types.semtype.RegexUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.runtime.internal.utils.ValueUtils.getTypedescValue;

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
public class RegExpValue implements BRegexpValue, RefValue {

    private static final BasicTypeBitSet BASIC_TYPE = Builder.getRegexType();
    private final RegExpDisjunction regExpDisjunction;
    private BTypedesc typedesc;
    private static final Type type = PredefinedTypes.TYPE_READONLY_ANYDATA;
    private final SemType shape;

    public RegExpValue(RegExpDisjunction regExpDisjunction) {
        this.regExpDisjunction = regExpDisjunction;
        this.shape = RegexUtils.regexShape(regExpDisjunction.stringValue(null));
    }

    @Override
    public RegExpDisjunction getRegExpDisjunction() {
        return this.regExpDisjunction;
    }

    @Override
    public String stringValue(BLink parent) {
        return this.regExpDisjunction.stringValue(parent);
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
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.regExpDisjunction);
    }

    @Override
    public BTypedesc getTypedesc() {
        if (this.typedesc == null) {
            this.typedesc = getTypedescValue(type, this);
        }
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
    public void freezeDirect() {
        // RegExpValue is always readonly
    }

    @Override
    public String toString() {
        return this.stringValue(null);
    }

    /**
     * Deep equality check for regular expression.
     *
     * @param o The regular expression on the right hand side
     * @param visitedValues Visited values in order to break cyclic references.
     * @return True if the regular expressions are equal, else false.
     */
    @Override
    public boolean equals(Object o, Set<ValuePair> visitedValues) {
        if (!(o instanceof RegExpValue rhsRegExpValue)) {
            return false;
        }
        return this.stringValue(null).equals(rhsRegExpValue.stringValue(null));
    }

    @Override
    public SemType widenedType(Context cx) {
        return Builder.getRegexType();
    }

    @Override
    public Optional<SemType> shapeOf() {
        return Optional.of(this.shape);
    }

    @Override
    public Optional<SemType> inherentTypeOf(Context cx) {
        return shapeOf();
    }

    @Override
    public BasicTypeBitSet getBasicType() {
        return BASIC_TYPE;
    }
}
