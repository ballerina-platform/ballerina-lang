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

 import io.ballerina.runtime.api.types.Type;
 import io.ballerina.runtime.api.values.BError;
 import io.ballerina.runtime.api.values.BFuture;
 import io.ballerina.runtime.api.values.BLink;
 import io.ballerina.runtime.api.values.BTypedesc;
 import io.ballerina.runtime.internal.scheduling.AsyncUtils;
 import io.ballerina.runtime.internal.scheduling.Scheduler;
 import io.ballerina.runtime.internal.scheduling.Strand;
 import io.ballerina.runtime.internal.types.BFutureType;
 import io.ballerina.runtime.internal.util.StringUtils;

 import java.util.Map;
 import java.util.StringJoiner;
 import java.util.concurrent.CompletableFuture;
 import java.util.concurrent.atomic.AtomicBoolean;

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
     Type type;
     public final CompletableFuture<Object> completableFuture;
     private final BTypedesc typedesc;
     private final AtomicBoolean waited;

     public FutureValue(Strand strand, Type constraint) {
         this.strand = strand;
         this.type = new BFutureType(constraint);
         this.typedesc = new TypedescValueImpl(this.type);
         this.completableFuture = new CompletableFuture<>();
         this.waited = new AtomicBoolean();
     }

     @Override
     public String stringValue(BLink parent) {
         StringJoiner sj = new StringJoiner(",", "{", "}");
         boolean isDone =  completableFuture.isDone();
         sj.add("isDone:" + completableFuture.isDone());
         Object result = completableFuture.getNow(null);
         if (isDone) {
             if (result instanceof BError error) {
                 sj.add("panic:" + error.getLocalizedMessage());
             } else {
                 sj.add("result:" + StringUtils.getStringVal(completableFuture.getNow(null), parent));
             }
         }
         return "future " + sj;
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

     @Override
     public BTypedesc getTypedesc() {
         return typedesc;
     }

     @Override
     public void cancel() {
         this.strand.state = Strand.State.CANCELLED;
         if (this.strand.workerChannelMap != null) {
             this.strand.workerChannelMap.cancel();
         }
     }

     /**
      * Returns the result value of the future.
      * @return result value
      */
     @Override
     public Object get() {
         return AsyncUtils.handleWait(Scheduler.getStrand(), this.completableFuture);
     }

     /**
      * Returns completion status of the {@code Strand} that the future is attached.
      * @return true if future is completed
      */
     @Override
     public boolean isDone() {
         return completableFuture.isDone();
     }

     @Override
     public String toString() {
         return stringValue(null);
     }

     @Override
     public boolean isPanic() {
         return completableFuture.isCompletedExceptionally();
     }

     public boolean getAndSetWaited() {
         return waited.getAndSet(true);
     }
 }
