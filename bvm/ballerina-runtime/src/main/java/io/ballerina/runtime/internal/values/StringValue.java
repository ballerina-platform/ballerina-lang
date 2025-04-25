/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.semtype.BasicTypeBitSet;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BStringType;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Class representing ballerina strings.
 *
 * @since 1.0.5
 */
public abstract class StringValue implements BString, SimpleValue {

    private static final BasicTypeBitSet BASIC_TYPE = Builder.getStringType();
    private static final BStringType STRING_TYPE =
            new BStringType(TypeConstants.STRING_TNAME, new Module(null, null, null));
    final String value;
    final boolean isNonBmp;
    private BStringType type;
    private boolean shapeCalculated = false;

    protected StringValue(String value, boolean isNonBmp) {
        this.value = value;
        this.isNonBmp = isNonBmp;
        this.type = STRING_TYPE;
    }

    @Override
    public Type getType() {
        return type;
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
    public IteratorValue<String> getIterator() {
        return new CharIterator(this);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String stringValue(BLink parent) {
        return value;
    }

    @Override
    public String informalStringValue(BLink parent) {
        return "\"" + this + "\"";
    }

    @Override
    public String expressionStringValue(BLink parent) {
        return informalStringValue(parent);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object str) {
        if (str == this) {
            return true;
        }
        if (str instanceof BString bString) {
            return bString.getValue().equals(value);
        }
        return false;
    }

    @Override
    public Optional<SemType> inherentTypeOf(Context cx) {
        if (!shapeCalculated) {
            this.type = BStringType.singletonType(value);
        }
        return Optional.of(this.type.shape());
    }

    @Override
    public BasicTypeBitSet getBasicType() {
        return BASIC_TYPE;
    }

    @Override
    public Iterator<?> getJavaIterator() {
        BIterator<String> iterator = getIterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Object next() {
                return StringUtils.fromString(iterator.next());
            }
        };
    }
}
