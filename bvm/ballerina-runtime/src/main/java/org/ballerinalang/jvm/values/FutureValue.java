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

 import org.ballerinalang.jvm.scheduling.Strand;
 import org.ballerinalang.jvm.types.BFutureType;
 import org.ballerinalang.jvm.types.BType;
 import org.ballerinalang.jvm.values.connector.CallableUnitCallback;

 import java.util.Map;
 import java.util.StringJoiner;

/**
 * <p>
 * Represent a Ballerina future in Java.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 * 
 * @since 0.995.0
 */
 public class FutureValue implements RefValue {

     public Strand strand;

     public Object result;

     public boolean isDone;

     public Throwable panic;

     public CallableUnitCallback callback;

     BType type;

     public FutureValue(Strand strand, CallableUnitCallback callback, BType constraint) {
         this.strand = strand;
         this.callback = callback;
         this.type = new BFutureType(constraint);
     }

     @Override
     public String stringValue(Strand strand) {
         StringJoiner sj = new StringJoiner(",", "{", "}");
         sj.add("isDone:" + isDone);
         if (isDone) {
             sj.add("result:" + result.toString());
         }
         if (panic != null) {
             sj.add("panic:" + panic.getLocalizedMessage());
         }
         return "future " + sj.toString();
     }

     @Override
     public BType getType() {
         return this.type;
     }

     @Override
     public Object copy(Map<Object, Object> refs) {
         throw new UnsupportedOperationException();
     }

     @Override
     public Object frozenCopy(Map<Object, Object> refs) {
         throw new UnsupportedOperationException();
     }

     public void cancel() {
         this.strand.cancel = true;
     }
 }
