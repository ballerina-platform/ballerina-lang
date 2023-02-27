/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * {@code {@link CharIterator }} provides iterator implementation for Ballerina string values.
 *
 * @since 2.0
 */
public class CharIterator implements IteratorValue {

    StringValue value;
    long cursor = 0;
    long length;
    String stringValue;

    CharIterator(StringValue value) {
        this.value = value;
        this.length = value.length();
        this.stringValue = value.getValue();
    }

    @Override
    public Object next() {
        long currentIndex = this.cursor++;
        if (value.isNonBmp) {
            return getNonBmpCharWithSurrogates(currentIndex);
        }
        return String.valueOf(stringValue.charAt((int) currentIndex));
    }

    private String getNonBmpCharWithSurrogates(long currentIndex) {
        int offset = (int) currentIndex;
        for (int surrogate : ((NonBmpStringValue) value).getSurrogates()) {
            if (surrogate < currentIndex) {
                offset++;
            } else if (surrogate > currentIndex) {
                break;
            } else {
                return new String(new char[]{stringValue.charAt(offset), stringValue.charAt(offset + 1)});
            }
        }
        return String.valueOf(stringValue.charAt(offset));
    }

    @Override
    public boolean hasNext() {
        return cursor < length;
    }
}
