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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BString;

import java.util.Map;

/**
 * Class representing ballerina strings.
 *
 * @since 1.0.5
 */
public abstract class StringValue implements BString, SimpleValue {

    final String value;
    final boolean isNonBmp;

    protected StringValue(String value, boolean isNonBmp) {
        this.value = value;
        this.isNonBmp = isNonBmp;
    }

    @Override
    public Type getType() {
        return PredefinedTypes.TYPE_STRING;
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
    public IteratorValue getIterator() {
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

}
