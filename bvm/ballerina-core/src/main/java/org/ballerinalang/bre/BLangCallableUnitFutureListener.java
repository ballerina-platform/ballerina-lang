/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre;

import org.ballerinalang.bre.bvm.CallableUnitFutureListener;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.threadpool.ResponseWorkerThread;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * {@code BLangCallableUnitFutureListener} Listener to listen to events sent from
 * callable units (functions and actions) and continue the execution.
 *
 * @since 0.964
 */
public class BLangCallableUnitFutureListener implements CallableUnitFutureListener {

    private Context context;
    private boolean nonBlocking = false;
    private volatile Semaphore executionWaitSem;

    public BLangCallableUnitFutureListener(Context context, boolean nonBlocking) {
        this.context = context;
        this.nonBlocking = nonBlocking;
        this.executionWaitSem = new Semaphore(0);
    }

    @Override
    public void notifySuccess() {
        done();
    }

    @Override
    public void notifyReply(BValue... response) {
        for (int i = 0; i < response.length; i++) {
            //context.getControlStack().currentFrame.returnValues[i] = response[i];
        }
        done();
    }

    @Override
    public void notifyFailure(Exception ex) {
//        BStruct err = BLangVMErrors.createError(context, context.getStartIP() - 1,
//                ex.getMessage());
//        context.setError(err);
        done();
    }

    private void done() {
        if (nonBlocking) {
            ThreadPoolFactory.getInstance().getExecutor()
                    .execute(new ResponseWorkerThread(context));
        } else {
            executionWaitSem.release();
//            synchronized (context) {
//                context.notifyAll();
//            }
        }
    }

    public boolean sync(long timeout) {
        try {
            return executionWaitSem.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            //ignore
        }
        return false;
    }
}
