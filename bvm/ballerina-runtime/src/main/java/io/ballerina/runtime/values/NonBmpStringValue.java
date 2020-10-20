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
 package io.ballerina.runtime.values;

 import io.ballerina.runtime.api.StringUtils;
 import io.ballerina.runtime.api.values.BLink;
 import io.ballerina.runtime.api.values.BString;

 import java.util.Arrays;

 /**
  * Represent ballerina strings containing at least one non basic multilingual plane unicode character.
  *
  * @since 1.0.5
  */
 public class NonBmpStringValue implements StringValue {

     private final String value;
     private final int[] surrogates;


     public NonBmpStringValue(String value, int[] surrogatePairLocations) {
         this.value = value;
         surrogates = surrogatePairLocations;
     }

    @Override
    public String getValue() {
        return value;
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
        if (str instanceof NonBmpStringValue) {
            NonBmpStringValue other = (NonBmpStringValue) str;
            int[] both = Arrays.copyOf(surrogates, surrogates.length + other.surrogates.length);
            System.arraycopy(other.surrogates, 0, both, surrogates.length, other.surrogates.length);
            return new NonBmpStringValue(this.value + other.value, both);
        } else if (str instanceof BmpStringValue) {
            BmpStringValue other = (BmpStringValue) str;
            return new NonBmpStringValue(this.value + other.getValue(), surrogates);
        } else {
            throw new RuntimeException("not impl yet");
        }
    }

     @Override
     public String stringValue(BLink parent) {
         return value;
     }

     @Override
     public String informalStringValue(BLink parent) {
         return "\"" + toString() + "\"";
     }

     @Override
     public String expressionStringValue(BLink parent) {
         return informalStringValue(parent);
     }

     public int[] getSurrogates() {
         return surrogates.clone();
     }

     @Override
     public String toString() {
         return value;
     }

     @Override
     public boolean equals(Object str) {
         if (str == this) {
             return true;
         }
         if (str instanceof BString) {
             return ((BString) str).getValue().equals(value);
         }
         return false;
     }

     @Override
     public int hashCode() {
         return value.hashCode();
     }

     @Override
     public Long indexOf(BString str, int fromIndex) {
         int offset = getOffset(fromIndex);
         long index = value.indexOf(str.getValue(), offset);
         if (index < 0) {
             return null;
         }
         for (int i = 0; i < index; i++) {
             char c = value.charAt(i);
             if (Character.isHighSurrogate(c)) {
                 index--;
             }
         }
         return index;
     }

     @Override
     public Long lastIndexOf(BString str, int fromIndex) {
         int offset = getOffset(fromIndex);
         long index = value.lastIndexOf(str.getValue(), offset);
         if (index < 0) {
             return null;
         }
         for (int i = 0; i < index; i++) {
             char c = value.charAt(i);
             if (Character.isHighSurrogate(c)) {
                 index--;
             }
         }
         return index;
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
