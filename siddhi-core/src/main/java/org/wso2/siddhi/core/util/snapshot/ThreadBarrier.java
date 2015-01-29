/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.siddhi.core.util.snapshot;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class ThreadBarrier {
    private boolean open = true;
    
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition threadSwitchCondition  = lock.newCondition();
    
    public void pass() {
        lock.lock();
        if(!open){
//            threadSwitchCondition.awaitUninterruptibly();
            try {
                threadSwitchCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lock.unlock();
     }
    
    public void open(){
        lock.lock();
        if(!open){
            open = true;
            threadSwitchCondition.signalAll(); 
        }
        lock.unlock();
    }

    public void close() {
        lock.lock();
        open = false;
        lock.unlock();

    }

}
