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

 import io.ballerina.runtime.api.values.BLink;
 import io.ballerina.runtime.api.values.BString;

 /**
  * Represent ballerina strings containing only unicode basic multilingual plane characters.
  *
  * @since 1.0.5
  */
 public class BmpStringValue implements StringValue {

     private final String value;

     public BmpStringValue(String value) {
         this.value = value;
     }

     @Override
     public String getValue() {
         return value;
     }

     @Override
     public int getCodePoint(int index) {
         return value.charAt(index);
     }

     @Override
     public int length() {
         return value.length();
     }

     @Override
     public BString concat(BString str) {
         if (str instanceof BmpStringValue) {
             return new BmpStringValue(this.value + ((BmpStringValue) str).value);
         } else if (str instanceof NonBmpStringValue) {
             return new NonBmpStringValue(this.value + str.getValue(), ((NonBmpStringValue) str).getSurrogates());
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

     @Override
     public int hashCode() {
         return value.hashCode();
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
     public String toString() {
         return value;
     }

     @Override
     public Long indexOf(BString str, int fromIndex) {
         long index = value.indexOf(str.getValue(), fromIndex);
         return index >= 0 ? index : null;
     }

     @Override
     public Long lastIndexOf(BString str, int fromIndex) {
         long index = value.lastIndexOf(str.getValue(), fromIndex);
         return index >= 0 ? index : null;
     }

     @Override
     public BString substring(int beginIndex, int endIndex) {
         return new BmpStringValue(value.substring(beginIndex, endIndex));
     }
 }
