/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a stack of execution frames usually corresponding to a thread. It is shown in 'Frames' panel of
 * 'Debug' tool window.
 */
public class BallerinaExecutionStack extends XExecutionStack {

    private final Long myWorkerID;
    @NotNull
    private final BallerinaDebugProcess myProcess;
    @NotNull
    private final List<BallerinaStackFrame> myStacks;

    private final BallerinaSuspendContext myContext;

    BallerinaExecutionStack(@NotNull BallerinaDebugProcess process, BallerinaSuspendContext context, Long myWorkerID,
                            @NotNull List<BallerinaStackFrame> frames) {
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
