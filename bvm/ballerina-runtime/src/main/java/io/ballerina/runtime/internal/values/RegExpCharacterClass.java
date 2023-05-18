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

import io.ballerina.runtime.api.values.BLink;

/**
 * <p>
 * Represents a character class, "[" ["^"] [ReCharSet] "]" in a regular expression.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *
 * @since 2201.3.0
 */
public class RegExpCharacterClass extends RegExpCommonValue implements RegExpAtom {
    private String characterClassStart;
    private String negation;
    private RegExpCharSet reCharSet;
    private String characterClassEnd;

    public RegExpCharacterClass(String characterClassStart, String negation, RegExpCharSet reCharSet,
                                String characterClassEnd) {
        this.characterClassStart = characterClassStart;
        this.negation = negation;
        this.reCharSet = reCharSet;
        this.characterClassEnd = characterClassEnd;
    }

    public RegExpCharSet getReCharSet() {
        return reCharSet;
    }

    public void setCharacterClassStart(String characterClassStart) {
        this.characterClassStart = characterClassStart;
    }

    public void setReCharSet(RegExpCharSet reCharSet) {
        this.reCharSet = reCharSet;
    }

    public void setCharacterClassEnd(String characterClassEnd) {
        this.characterClassEnd = characterClassEnd;
    }

    @Override
    public String stringValue(BLink parent) {
        return this.characterClassStart + this.negation + this.reCharSet.stringValue(parent) + this.characterClassEnd;
    }
}
