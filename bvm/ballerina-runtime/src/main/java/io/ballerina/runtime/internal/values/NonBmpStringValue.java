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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

import java.util.Arrays;

/**
 * Represent ballerina strings containing at least one non basic multilingual plane unicode character.
 *
 * @since 1.0.5
 */
public class NonBmpStringValue extends StringValue {

    private final int[] surrogates;

    public NonBmpStringValue(String value, int[] surrogatePairLocations) {
        super(value, true);
        surrogates = surrogatePairLocations;
    }

   @Override
   public int getCodePoint(int index) {
       if ((index < 0) || (index >= value.length() - surrogates.length)) {
           throw new StringIndexOutOfBoundsException(index);
       }

       int offset = index;
       for (int surrogate : surrogates) {
           if (surrogate < index) {
               offset++;
           } else if (surrogate > index) {
               break;
           } else {
               return Character.toCodePoint(value.charAt(offset), value.charAt(offset + 1));
           }
       }
       return value.charAt(offset);
   }

   @Override
   public int length() {
       return value.length() - surrogates.length;
   }

   @Override
   public BString concat(BString str) {
       StringValue stringValue = (StringValue) str;
       if (stringValue.isNonBmp) {
           NonBmpStringValue other = (NonBmpStringValue) str;
           int[] both = Arrays.copyOf(surrogates, surrogates.length + other.surrogates.length);
           int length = length();
           for (int i = 0; i < other.surrogates.length; i++) {
               both[i + surrogates.length] = other.surrogates[i] + length;
           }
           return new NonBmpStringValue(this.value + other.value, both);
       }
       return new NonBmpStringValue(this.value + str.getValue(), surrogates);
   }

    public int[] getSurrogates() {
        return surrogates.clone();
    }

    @Override
    public Long indexOf(BString str, int fromIndex) {
        int offset = getOffset(fromIndex);
        int utf16Index = value.indexOf(str.getValue(), offset);
        if (utf16Index < 0) {
            return null;
        }
        return toCodePointIndex(utf16Index);
    }

    @Override
    public Long lastIndexOf(BString str, int fromIndex) {
        int offset = getOffset(fromIndex);
        int utf16Index = value.lastIndexOf(str.getValue(), offset);
        if (utf16Index < 0) {
            return null;
        }
        return toCodePointIndex(utf16Index);
    }

    private long toCodePointIndex(int utf16Index) {
        // The j-th surrogate pair starts at UTF-16 position surrogates[j] + j.
        int count = 0;
        for (int j = 0; j < surrogates.length && surrogates[j] + j < utf16Index; j++) {
            count++;
        }
        return (long) utf16Index - count;
    }

    @Override
    public BString substring(int beginIndex, int endIndex) {
        int beginOffset = getOffset(beginIndex);
        int endOffset = getOffset(endIndex);
        return StringUtils.fromString(value.substring(beginOffset, endOffset));
    }

    private int getOffset(int fromIndex) {
        int offset = fromIndex;
        for (int surrogate : surrogates) {
            if (surrogate < fromIndex) {
                offset++;
            } else {
                break;
            }
        }
        return offset;
    }
}
