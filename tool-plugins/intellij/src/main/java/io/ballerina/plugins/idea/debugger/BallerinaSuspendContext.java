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
 */

package io.ballerina.plugins.idea.debugger;

import com.google.common.base.Strings;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.eclipse.lsp4j.debug.StackFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represent a Ballerina suspended context. Created in debug hits.
 */
public class BallerinaSuspendContext extends XSuspendContext {

    private List<BallerinaExecutionStack> myExecutionStacks = new LinkedList<>();
    private BallerinaExecutionStack myActiveStack;
    private BallerinaDebugProcess myProcess;

    BallerinaSuspendContext(@NotNull BallerinaDebugProcess process) {
        myProcess = process;
    }

    void addToExecutionStack(Long threadId, StackFrame[] stackFrames) {
        List<BallerinaStackFrame> balStackFrames = toBalStackFrames(stackFrames);
        BallerinaExecutionStack stack = new BallerinaExecutionStack(myProcess, this, threadId,
                balStackFrames);
        myExecutionStacks.add(stack);
        myActiveStack = stack;
    }

    @Nullable
    @Override
    public BallerinaExecutionStack getActiveExecutionStack() {
        return myActiveStack;
    }

    private void setMyActiveStack(BallerinaExecutionStack stack) {
        myActiveStack = stack;
    }

    @NotNull
    @Override
    public XExecutionStack[] getExecutionStacks() {
        return myExecutionStacks.toArray(new BallerinaExecutionStack[0]);
    }

    private List<BallerinaStackFrame> toBalStackFrames(StackFrame[] frames) {
        List<BallerinaStackFrame> balStackFrames = new ArrayList<>();
        for (StackFrame frame : frames) {
            // Todo - Enable java stack frames
            if (isBallerinaSource(frame)) {
                balStackFrames.add(new BallerinaStackFrame(myProcess, frame));
            }
        }
        return balStackFrames;
    }

    private boolean isBallerinaSource(StackFrame frame) {
        if (frame == null || frame.getSource() == null || frame.getSource().getName() == null) {
            return false;
        }
        String fileName = frame.getSource().getName();
        if (Strings.isNullOrEmpty(fileName) || fileName.split("\\.").length <= 1) {
            return false;
        }
        return fileName.split("\\.")[fileName.split("\\.").length - 1].equals("bal");
    }

    static class BallerinaExecutionStack extends XExecutionStack {

        private final Long myWorkerID;
        @NotNull
        private final BallerinaDebugProcess myProcess;
        @NotNull
        private final List<BallerinaStackFrame> myStacks;

        private final BallerinaSuspendContext myContext;

        BallerinaExecutionStack(@NotNull BallerinaDebugProcess process, BallerinaSuspendContext context,
                                Long myWorkerID, @NotNull List<BallerinaStackFrame> frames) {
            super(" Worker #" + myWorkerID);
            this.myWorkerID = myWorkerID;
            this.myContext = context;
            this.myProcess = process;
            this.myStacks = frames;
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

        Long getMyWorkerID() {
            return myWorkerID;
        }
    }
}
