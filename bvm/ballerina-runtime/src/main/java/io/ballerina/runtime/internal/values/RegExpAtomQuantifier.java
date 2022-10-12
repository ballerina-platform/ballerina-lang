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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BLink;

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
public class RegExpAtomQuantifier extends RegExpCommonValue implements RegExpTerm {
    private Object reAtom;
    private RegExpQuantifier reQuantifier;

    public RegExpAtomQuantifier(Object reAtom, RegExpQuantifier reQuantifier) {
        this.reAtom = reAtom;
        this.reQuantifier = reQuantifier;
    }

    public Object getReAtom() {
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
        return StringUtils.getStringValue(this.reAtom, parent) + this.reQuantifier.stringValue(parent);
    }
}
