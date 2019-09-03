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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.Strand.State;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.exceptions.BLangNullReferenceException;
import org.ballerinalang.util.program.BLangVMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * This represents the Ballerina worker scheduling functionality.
 *
 * @since 0.985.0
 */
public class BVMScheduler {

    private static final Logger breLog = LoggerFactory.getLogger(BVMScheduler.class);

    private static AtomicInteger strandCount = new AtomicInteger(0);

    //TODO these are static vars, we may need to find a way to make this instance vars
    private static Semaphore strandsDoneSemaphore = new Semaphore(1);

    /**
     * Method to schedule a strand for execution.
     *
     * @param strand to be executed
     */
    public static void schedule(Strand strand) {

    }

    /**
     * Method to execute the strand in the current thread.
     *
     * @param strand to be executed
     */
    public static void execute(Strand strand) {
        try {
            BVM.execute(strand);
        } catch (Throwable e) {
            //These errors are unhandled errors in BVM, hence logging them to bre log.
            breLog.error(e.getMessage(), e);
            // Wrap the errors in a runtime exception to make sure these are logged in internal log.
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to schedule native call execution.
     *
     * @param nativeCallable    to be invoked
     * @param nativeCtx         to be used
     * @param callback          for notifications
     */
    public static void scheduleNative(NativeCallableUnit nativeCallable,
                                      Context nativeCtx, CallableUnitCallback callback) {
    }

    /**
     * Method to execute native call in the current thread.
     *
     * @param nativeCallable    to be executed
     * @param nativeCtx         to be used
     * @param callback          for notifications
     */
    public static void executeNative(NativeCallableUnit nativeCallable,
                                     Context nativeCtx, CallableUnitCallback callback) {
        nativeCallable.execute(nativeCtx, callback);
    }


    /**
     * Method to change states of the strands.
     *
     * @param strand            to be changed
     * @param expectedStates    current states
     * @param newState          new state
     */
    public static void stateChange(Strand strand, List<State> expectedStates, State newState) {
        //TODO fix
//        if (expectedStates == null || expectedStates.contains(strand.state)) {
//            strand.state = newState;
//            return;
//        }
//        throw new BLangRuntimeException("error: invalid strand state, expected "
//                + expectedStates.toString() + " found - " + strand.state.toString()); //TODO error message?
    }

    /**
     * Method to change states of the strands.
     *
     * @param strand            to be changed
     * @param expectedState    current states
     * @param newState          new state
     */
    public static void stateChange(Strand strand, State expectedState, State newState) {
        //TODO fix
//        if (expectedState == strand.state) {
//            strand.state = newState;
//            return;
//        }
//        throw new BLangRuntimeException("error: invalid strand state, expected "
//                + expectedState + " found - " + strand.state.toString()); //TODO error message?
    }


    static void strandCountUp() {
        if (strandCount.incrementAndGet() == 1) {
            try {
                strandsDoneSemaphore.acquire();
            } catch (InterruptedException e) {
                /* ignore */
            }
        }
    }

    static void strandCountDown() {
        if (strandCount.decrementAndGet() == 0) {
            strandsDoneSemaphore.release();
        }
    }

    /**
     * Method to be used for all strand's execution completion.
     */
    public static void waitForStrandCompletion() {
        try {
            strandsDoneSemaphore.acquire();
            strandsDoneSemaphore.release();
        } catch (InterruptedException e) {
            /* ignore */
        }
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
            try {
                BVM.execute(this.strand);
            } catch (Throwable e) {
                //These errors are unhandled errors in BVM, hence logging them to bre log.
                breLog.error(e.getMessage(), e);
            }
        }
    }

    /**
     * This represents the thread used to run a blocking native call in async mode.
     */
    private static class NativeCallableExecutor implements Runnable {

        private NativeCallableUnit nativeCallable;

        private Context nativeCtx;

        private CallableUnitCallback callback;

        public NativeCallableExecutor(NativeCallableUnit nativeCallable,
                                      Context nativeCtx, CallableUnitCallback callback) {
            this.nativeCallable = nativeCallable;
            this.nativeCtx = nativeCtx;
            this.callback = callback;
        }

        @Override
        public void run() {
            BError error;
            Strand strand = this.nativeCtx.getStrand();
            CallableUnitInfo cui = this.nativeCtx.getCallableUnitInfo();
            BType retType = cui.getRetParamTypes()[0];
            try {
                this.nativeCallable.execute(this.nativeCtx, callback);
                if (strand.fp > 0) {
                    // Maybe we can omit this since natives cannot have worker interactions
                    if (BVM.checkIsType(this.nativeCtx.getReturnValue(), BTypes.typeError)) {
                        strand.currentFrame.handleChannelError((BRefType) this.nativeCtx.getReturnValue(),
                                strand.peekFrame(1).wdChannels);
                    }
                    strand.popFrame();
                    StackFrame retFrame = strand.currentFrame;
                    BLangVMUtils.populateWorkerDataWithValues(retFrame, this.nativeCtx.getDataFrame().retReg,
                            this.nativeCtx.getReturnValue(), retType);
                    execute(strand);
                    return;
                }
                if (BVM.checkIsType(this.nativeCtx.getReturnValue(), BTypes.typeError)) {
                    strand.currentFrame.handleChannelError((BRefType) this.nativeCtx.getReturnValue(),
                            strand.respCallback.parentChannels);
                }
                strand.respCallback.signal();
                return;
            } catch (BLangNullReferenceException e) {
                error = BLangVMErrors.createNullRefException(this.nativeCtx.getStrand());
            } catch (Throwable e) {
                error = BLangVMErrors.createError(this.nativeCtx.getStrand(), e.getMessage());
            }
            strand.setError(error);
            if (strand.fp > 0) {
                strand.currentFrame.handleChannelPanic(error, strand.peekFrame(1).wdChannels);
                strand.popFrame();
            } else {
                strand.currentFrame.handleChannelPanic(error, strand.respCallback.parentChannels);
                strand.popFrame();
            }
            BVM.handleError(strand);
            execute(strand);
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
