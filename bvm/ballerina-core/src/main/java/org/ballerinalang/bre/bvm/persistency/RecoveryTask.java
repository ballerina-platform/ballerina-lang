/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.bre.bvm.persistency;

import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.bre.bvm.BLangScheduler;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.persistence.State;
import org.ballerinalang.persistence.StateStore;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;

import java.util.List;

public class RecoveryTask implements Runnable {

    private ProgramFile programFile;

    public RecoveryTask(ProgramFile programFile) {
        this.programFile = programFile;
    }

    @Override
    public void run() {

        System.out.println("RecoveryTask: Waiting for runtime startup...");
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("RecoveryTask: Starting saved states...");
        Debugger debugger = new Debugger(programFile);
        initDebugger(programFile, debugger);
        List<State> states = StateStore.getInstance().getStates(programFile);
        if (states == null) {
            return;
        }
        for (State state : states) {
            WorkerExecutionContext context = state.getContext();
            // As we don't have any running context at this point, none of the contexts can run in caller.
            // Even though sync functions run in caller under normal conditions, we have to override
            // that to start a new thread for the function.
            context.runInCaller = false;
            BLangScheduler.schedule(context);
        }

    }

    private void initDebugger(ProgramFile programFile, Debugger debugger) {
        if (programFile.getDebugger() != null) {
            return;
        }
        programFile.setDebugger(debugger);
        if (debugger.isDebugEnabled()) {
            debugger.init();
            debugger.waitTillDebuggeeResponds();
        }
    }
}
