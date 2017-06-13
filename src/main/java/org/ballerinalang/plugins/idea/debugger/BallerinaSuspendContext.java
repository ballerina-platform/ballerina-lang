/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.debugger;

import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.ballerinalang.plugins.idea.debugger.protocol.BallerinaAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BallerinaSuspendContext extends XSuspendContext {

    @NotNull
    private final BallerinaExecutionStack myStack;

    public BallerinaSuspendContext(@NotNull BallerinaDebugProcess process, int threadId,
                                   @NotNull List<BallerinaAPI.Location> locations,
                                   @NotNull BallerinaCommandProcessor processor) {
        myStack = new BallerinaExecutionStack(process, threadId, locations, processor);
    }

    @Nullable
    @Override
    public XExecutionStack getActiveExecutionStack() {
        return myStack;
    }

    @NotNull
    @Override
    public XExecutionStack[] getExecutionStacks() {
        return new XExecutionStack[]{myStack};
    }

    private static class BallerinaExecutionStack extends XExecutionStack {

        @NotNull
        private final BallerinaDebugProcess myProcess;
        @NotNull
        private final List<BallerinaAPI.Location> myLocations;
        @NotNull
        private final BallerinaCommandProcessor myProcessor;
        @NotNull
        private final List<BallerinaStackFrame> myStack;

        public BallerinaExecutionStack(@NotNull BallerinaDebugProcess process, int threadId,
                                       @NotNull List<BallerinaAPI.Location> locations,
                                       @NotNull BallerinaCommandProcessor processor) {
            super("Thread #" + threadId);
            myProcess = process;
            myLocations = locations;
            myProcessor = processor;
            myStack = ContainerUtil.newArrayListWithCapacity(locations.size());
            for (int i = 0; i < myLocations.size(); i++) {
                myStack.add(new BallerinaStackFrame(myProcess, myLocations.get(i), myProcessor, i));
            }
        }

        @Nullable
        @Override
        public XStackFrame getTopFrame() {
            return ContainerUtil.getFirstItem(myStack);
        }

        @Override
        public void computeStackFrames(int firstFrameIndex, @NotNull XStackFrameContainer container) {
            container.addStackFrames(myStack, true);
        }
    }
}
