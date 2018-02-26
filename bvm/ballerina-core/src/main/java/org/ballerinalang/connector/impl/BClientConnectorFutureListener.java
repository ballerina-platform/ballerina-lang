/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.connector.impl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.threadpool.ResponseWorkerThread;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.util.tracer.BallerinaTracerManager;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * {@code BClientConnectorFutureListener} Listener to listen to client side events and continue the execution.
 *
 * @since 0.94
 */
public class BClientConnectorFutureListener implements ConnectorFutureListener {

    private Context context;
    private String traceSpanId;
    private boolean nonBlocking = false;
    private volatile Semaphore executionWaitSem;

    public BClientConnectorFutureListener(Context context, boolean nonBlocking) {
        this.context = context;
        this.nonBlocking = nonBlocking;
        this.executionWaitSem = new Semaphore(0);
        this.traceSpanId = BallerinaTracerManager.getInstance().buildSpan(context);
    }

    @Override
    public void notifySuccess() {
        done();
    }

    @Override
    public void notifyReply(BValue... response) {
        for (int i = 0; i < response.length; i++) {
            context.getControlStack().currentFrame.returnValues[i] = response[i];
        }
        done();
    }

    @Override
    public void notifyFailure(BallerinaConnectorException ex) {
        BStruct err = BLangVMErrors.createError(context, context.getStartIP() - 1,
                ex.getMessage());
        context.setError(err);
        done();
    }

    private void done() {
        if (nonBlocking) {
            ThreadPoolFactory.getInstance().getExecutor()
                    .execute(new ResponseWorkerThread(context, traceSpanId));
        } else {
            executionWaitSem.release();
            BallerinaTracerManager.getInstance().finishSpan(context, traceSpanId);
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
