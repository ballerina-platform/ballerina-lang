/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BLink;
import io.ballerina.runtime.api.values.BTypedesc;

import java.util.Map;

/**
 * <p>
 * Represents ReAtom [ReQuantifier] in regular expression.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *
 * @since 2201.3.0
 */
public class RegExpAtomQuantifier implements RegExpTerm {
    private RegExpAtom reAtom;
    private RegExpQuantifier reQuantifier;

    public RegExpAtomQuantifier(RegExpAtom reAtom, RegExpQuantifier reQuantifier) {
        this.reAtom = reAtom;
        this.reQuantifier = reQuantifier;
    }

    public RegExpAtom getReAtom() {
        return this.reAtom;
    }

    public RegExpQuantifier getReQuantifier() {
        return this.reQuantifier;
    }

    public void setReAtom(RegExpAtom reAtom) {
        this.reAtom = reAtom;
    }

    public void setReQuantifier(RegExpQuantifier reQuantifier) {
        this.reQuantifier = reQuantifier;
    }

    @Override
    public String stringValue(BLink parent) {
        return this.reAtom.stringValue(parent) + this.reQuantifier.stringValue(parent);
    }

    @Override
    public String expressionStringValue(BLink parent) {
        return stringValue(parent);
    }

    @Override
    public String informalStringValue(BLink parent) {
        return stringValue(parent);
    }

    @Override
    public Type getType() {
        return PredefinedTypes.TYPE_ANYDATA;
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
    public BTypedesc getTypedesc() {
        throw new UnsupportedOperationException();
    }
}
