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

 import io.ballerina.runtime.api.async.Callback;
 import io.ballerina.runtime.api.types.Type;
 import io.ballerina.runtime.api.values.BFuture;
 import io.ballerina.runtime.api.values.BLink;
 import io.ballerina.runtime.scheduling.Strand;
 import io.ballerina.runtime.types.BFutureType;

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
 public class FutureValue implements BFuture, RefValue {

     public Strand strand;

     public Object result;

     public boolean isDone;

     public Throwable panic;

     public Callback callback;

     Type type;

     @Deprecated
     public FutureValue(Strand strand, Callback callback, Type constraint) {
         this.strand = strand;
         this.callback = callback;
         this.type = new BFutureType(constraint);
     }

     @Override
     public String stringValue(BLink parent) {
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
    public String expressionStringValue(BLink parent) {
        return stringValue(parent);
    }

    @Override
     public Type getType() {
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

    /**
     * Returns the strand that the future is attached to.
     * @return {@code Strand}
     */
    public Strand getStrand() {
        return this.strand;
    }

    /**
     * Returns the result value of the future.
     * @return result value
     */
    public Object getResult() {
       return this.result;
    }

    /**
     * Returns completion status of the {@code Strand} that the future is attached.
     * @return true if future is completed
     */
    public boolean isDone() {
        return this.isDone;
    }

    /**
     * Returns {@code Throwable} if the attached strand panic.
     * @return panic error or null if not panic occurred
     */
    public Throwable getPanic() {
        return this.panic;
    }

    /**
     * {@code CallableUnitCallback} listening on the completion of this future.
     * @return registered {@code CallableUnitCallback}
     */
    public Callback getCallback() {
        return this.callback;
    }

    @Override
    public String toString() {
        return stringValue(null);
    }

}
