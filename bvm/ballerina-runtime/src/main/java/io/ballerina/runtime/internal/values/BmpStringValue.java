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

import io.ballerina.runtime.api.values.BString;

 /**
  * Represent ballerina strings containing only unicode basic multilingual plane characters.
  *
  * @since 1.0.5
  */
 public class BmpStringValue extends StringValue {

     public BmpStringValue(String value) {
         super(value, false);
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
         StringValue stringValue = (StringValue) str;
         if (stringValue.isNonBmp) {
             int[] otherSurrogates = ((NonBmpStringValue) str).getSurrogates();
             int[] newSurrogates = new int[otherSurrogates.length];
             int length = length();
             for (int i = 0; i < otherSurrogates.length; i++) {
                 newSurrogates[i] = otherSurrogates[i] + length;
             }
             return new NonBmpStringValue(this.value + str.getValue(), newSurrogates);
         }
         return new BmpStringValue(this.value + str.getValue());
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
