/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.variable;

import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

/**
 * Debug context information for ballerina debug variables.
 */
public class VariableContext {
    private final StackFrame frame;
    private final ThreadReference owningThread;

    public VariableContext(StackFrame frame) {
        this(frame, frame.thread());
    }

    public VariableContext(StackFrame frame, ThreadReference threadRef) {
        this.frame = frame;
        this.owningThread = threadRef;
    }

    public ThreadReference getOwningThread() {
        return owningThread;
    }

    public StackFrame getFrame() {
        return frame;
    }
}
