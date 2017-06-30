/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * {@code ControlStack} represents function call stack.
 *
 * @since 0.8.0
 */
public class ControlStackNew {
    public static final int DEFAULT_CONTROL_STACK_SIZE = 2000;

    private StackFrame[] stackFrames;

    // Stack frame pointer;
    public int fp = -1;

    public StackFrame currentFrame;

    public ControlStackNew() {
        stackFrames = new StackFrame[DEFAULT_CONTROL_STACK_SIZE];
    }

    public StackFrame pushFrame(StackFrame frame) {
        stackFrames[++fp] = frame;
        currentFrame = frame;
        return currentFrame;
    }

    public StackFrame popFrame() {
        StackFrame poppedFrame = currentFrame;
        stackFrames[fp] = null;
        if (fp > 0) {
            currentFrame = stackFrames[--fp];
        }  else {
            currentFrame = null;
            fp--;
        }

        return poppedFrame;
    }

    public StackFrame peekFrame() {
        StackFrame peekFrame = null;
        if (fp > 0) {
            peekFrame = stackFrames[fp - 1];
        }
        return peekFrame;
    }

    public StackFrame getRootFrame() {
        return stackFrames[0];
    }

    public StackFrame getCurrentFrame() {
        return currentFrame;
    }

    public StackFrame[] getStack() {
        return stackFrames;
    }
}
