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
package org.ballerinalang.bre.vm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerState;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.exceptions.BLangNullReferenceException;
import org.ballerinalang.util.program.BLangVMUtils;

import java.util.concurrent.atomic.LongAdder;

/**
 * This represents the Ballerina worker scheduling functionality. 
 * 
 * @since 0.985.0
 */
public class BVMScheduler {

    public static void schedule(Strand strand) {
        ThreadPoolFactory.getInstance().getWorkerExecutor().submit(new CallableExecutor(strand));
    }

    public static void scheduleNative(NativeCallableUnit nativeCallable, Context nativeCtx) {
        ThreadPoolFactory.getInstance().getWorkerExecutor()
                .submit(new NativeCallableExecutor(nativeCallable, nativeCtx));
    }

    /**
     * This represents the thread used to execute a runnable worker.
     */
    private static class CallableExecutor implements Runnable {

        private Strand strand;

        public CallableExecutor(Strand strand) {
            this.strand = strand;
        }
        
        @Override
        public void run() {
            BVM.execute(this.strand);
        }
        
    }
    
    /**
     * This represents the thread used to run a blocking native call in async mode.
     */
    private static class NativeCallableExecutor implements Runnable {

        private NativeCallableUnit nativeCallable;
        
        private Context nativeCtx;
        
        public NativeCallableExecutor(NativeCallableUnit nativeCallable, Context nativeCtx) {
            this.nativeCallable = nativeCallable;
            this.nativeCtx = nativeCtx;
        }
        
        @Override
        public void run() {
            WorkerExecutionContext runInCaller = null;
            BError error;
            Strand strand = this.nativeCtx.getStrand();
            CallableUnitInfo cui = this.nativeCtx.getCallableUnitInfo();
            BType retType = cui.getRetParamTypes()[0];
            try {
                this.nativeCallable.execute(this.nativeCtx, null);
                if (strand.fp > 0) {
                    strand.popFrame();
                    DataFrame retFrame = strand.currentFrame;
                    BLangVMUtils.populateWorkerDataWithValues(retFrame, this.nativeCtx.getDataFrame().retReg,
                            this.nativeCtx.getReturnValue(), retType);
                    BVM.execute(strand);
                    return;
                }
                Strand retStrand = strand.respCallback.signal();
                if (retStrand != null) {
                    BVM.execute(strand);
                }
                return;
            } catch (BLangNullReferenceException e) {
                error = BLangVMErrors.createNullRefException(this.nativeCtx.getStrand());
            } catch (Throwable e) {
                error = BLangVMErrors.createError(this.nativeCtx.getStrand(), e.getMessage());
            }
            strand.setError(error);
            strand.popFrame();
            strand = BVM.handleError(strand);
            if (strand != null) {
                BVM.execute(strand);
            }
        }
        
    }
    
    /**
     * This class represents the scheduler statistics.
     */
    public static class SchedulerStats {
        
        private LongAdder[] stateCounts;
        
        public SchedulerStats() {
            this.stateCounts = new LongAdder[6];
            for (int i = 0; i < this.stateCounts.length; i++) {
                this.stateCounts[i] = new LongAdder();
            }
        }

        public long getReadyWorkerCount() {
            return this.stateCounts[0].longValue();
        }

        public long getRunningWorkerCount() {
            return this.stateCounts[1].longValue();
        }

        public long getExceptedWorkerCount() {
            return this.stateCounts[2].longValue();
        }

        public long getWaitingForResponseWorkerCount() {
            return this.stateCounts[3].longValue();
        }

        public long getPausedWorkerCount() {
            return this.stateCounts[4].longValue();
        }

        public long getWaitingForLockWorkerCount() {
            return this.stateCounts[5].longValue();
        }
        
        public void stateTransition(WorkerExecutionContext currentCtx, WorkerState newState) {
//            if (!schedulerStatsEnabled || currentCtx.isRootContext()) {
//                return;
//            }
//            WorkerState oldState = currentCtx.state;
//            /* we are not considering CREATED state */
//            if (oldState != WorkerState.CREATED) {
//                this.stateCounts[oldState.ordinal()].decrement();
//            }
//            /* we are not counting the DONE state, since it is an ever increasing value */
//            if (newState != WorkerState.DONE) {
//                this.stateCounts[newState.ordinal()].increment();
//            }
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Worker Status:- \n");
            builder.append("\tREADY: " + this.getReadyWorkerCount() + "\n");
            builder.append("\tRUNNING: " + this.getRunningWorkerCount() + "\n");
            builder.append("\tEXCEPTED: " + this.getExceptedWorkerCount() + "\n");
            builder.append("\tWAITING FOR RESPONSE: " + this.getWaitingForResponseWorkerCount() + "\n");
            builder.append("\tPAUSED: " + this.getPausedWorkerCount() + "\n");
            builder.append("\tWAITING FOR LOCK: " + this.getWaitingForLockWorkerCount() + "\n");
            return builder.toString();
        }
        
    }
    
}
