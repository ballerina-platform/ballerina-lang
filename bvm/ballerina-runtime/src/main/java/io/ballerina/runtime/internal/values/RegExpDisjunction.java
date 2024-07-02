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

import java.util.StringJoiner;

import static io.ballerina.runtime.internal.util.StringUtils.getStringVal;

/**
 * <p>
 * Represents an ReDisjunction in regular expression.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 *
 * @since 2201.3.0
 */
public class RegExpDisjunction extends RegExpCommonValue {
    private final Object[] seqList;

    public RegExpDisjunction(ArrayValue seqList) {
        this.seqList = seqList.getValues();
    }

    public RegExpDisjunction(Object[] seqList) {
        this.seqList = seqList;
    }

    public Object[] getRegExpSeqList() {
        return this.seqList;
    }

    @Override
    public String stringValue(BLink parent) {
        StringJoiner terms = new StringJoiner("");
        for (Object t : this.seqList) {
            if (t == null) {
                break;
            }
            if (t instanceof String s) {
                terms.add(s);
                continue;
            }
            terms.add(getStringVal(t, parent));
        }
        return terms.toString();
    }
}
