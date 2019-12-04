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
package org.ballerinalang.jvm.values;

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
     public StringValue concat(StringValue str) {
         if (str instanceof BmpStringValue) {
             return new BmpStringValue(this.value + ((BmpStringValue) str).value);
         } else {
             throw new RuntimeException("not impl yet");
         }
     }
}
