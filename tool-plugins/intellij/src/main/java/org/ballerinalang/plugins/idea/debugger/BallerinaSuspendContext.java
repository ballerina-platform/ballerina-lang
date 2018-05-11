/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.ballerinalang.plugins.idea.debugger;

import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.ballerinalang.plugins.idea.debugger.dto.Frame;
import org.ballerinalang.plugins.idea.debugger.dto.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Represent a Ballerina suspended context. Created in debug hits.
 */
public class BallerinaSuspendContext extends XSuspendContext {

    private List<BallerinaExecutionStack> myExecutionStacks = new LinkedList<>();
    private BallerinaExecutionStack myActiveStack;

    public BallerinaSuspendContext(@NotNull BallerinaDebugProcess process, @NotNull Message message) {
        addToExecutionStack(process, message);
    }

    public void addToExecutionStack(@NotNull BallerinaDebugProcess process, @NotNull Message message) {
        BallerinaExecutionStack stack = new BallerinaExecutionStack(process, this, message.getThreadId(),
                message.getFrames());
        myExecutionStacks.add(stack);
        myActiveStack = stack;
    }

    @Nullable
    @Override
    public BallerinaExecutionStack getActiveExecutionStack() {
        return myActiveStack;
    }

    public void setMyActiveStack(BallerinaExecutionStack stack) {
        myActiveStack = stack;
    }

    @NotNull
    @Override
    public XExecutionStack[] getExecutionStacks() {
        return myExecutionStacks.toArray(new BallerinaExecutionStack[myExecutionStacks.size()]);
    }

    static class BallerinaExecutionStack extends XExecutionStack {

        private final String myWorkerID;
        @NotNull
        private final BallerinaDebugProcess myProcess;
        @NotNull
        private final List<BallerinaStackFrame> myStacks;

        private final BallerinaSuspendContext myContext;

        public BallerinaExecutionStack(@NotNull BallerinaDebugProcess process, BallerinaSuspendContext context,
                                       String myWorkerID, List<Frame> frames) {
            super(" Worker #" + myWorkerID);
            this.myWorkerID = myWorkerID;
            this.myContext = context;
            this.myProcess = process;
            this.myStacks = ContainerUtil.newArrayListWithCapacity(frames.size());
            for (Frame frame : frames) {
                myStacks.add(new BallerinaStackFrame(myProcess, frame));
            }
        }

        @Nullable
        @Override
        public XStackFrame getTopFrame() {
            return ContainerUtil.getFirstItem(myStacks);
        }

        @Override
        public void computeStackFrames(int firstFrameIndex, @NotNull XStackFrameContainer container) {
            // Note - Need to add an empty list if the index is not 0. Otherwise will not work properly.
            if (firstFrameIndex == 0) {
                container.addStackFrames(myStacks, true);
            } else {
                container.addStackFrames(new LinkedList<>(), true);
            }
            myContext.setMyActiveStack(this);
        }

        public String getMyWorkerID() {
            return myWorkerID;
        }
    }
}
