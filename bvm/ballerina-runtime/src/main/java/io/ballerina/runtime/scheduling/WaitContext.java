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
 package io.ballerina.runtime.scheduling;

 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.locks.ReentrantLock;

 /**
  * This context is shared among the strands to notify that a
  * certain strand is waiting on another strand.
  *
  * @since 1.0.0
  */
 public abstract class WaitContext {

     SchedulerItem schedulerItem;
     boolean runnable;
     boolean completed;
     boolean intermediate;
     private final ReentrantLock contextLock;
     AtomicInteger waitCount;


     WaitContext(SchedulerItem schedulerItem) {
         this.schedulerItem = schedulerItem;
         this.contextLock = new ReentrantLock();
         this.waitCount = new AtomicInteger();
         this.intermediate = true;

     }

     void lock() {
         this.contextLock.lock();
     }

     void unLock() {
         this.contextLock.unlock();
     }

     abstract boolean handlePanic();

     abstract boolean waitCompleted(Object result);
 }
