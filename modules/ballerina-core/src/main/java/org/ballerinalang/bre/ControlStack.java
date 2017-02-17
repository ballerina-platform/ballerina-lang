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
package org.ballerinalang.bre;

import org.ballerinalang.model.values.BValue;

import java.util.Iterator;
import java.util.Stack;

/**
 * {@code ControlStack} represents function call stack.
 *
 * @since 0.8.0
 */
public class ControlStack {

    private Stack<StackFrame> stack;
    private StackFrame currentFrame;

    public ControlStack() {
        stack = new Stack<>();
    }

    public StackFrame pushFrame(StackFrame frame) {
        currentFrame = stack.push(frame);
        return currentFrame;
    }

    public StackFrame popFrame() {
        StackFrame poppedFrame = stack.pop();
        currentFrame = (stack.isEmpty()) ? null : stack.peek();
        return poppedFrame;
    }

    public StackFrame getCurrentFrame() {
        return currentFrame;
    }

    public BValue getValue(int offset) {
        return currentFrame.values[offset];
    }

    public void setValue(int offset, BValue bValue) {
        currentFrame.values[offset] = bValue;
    }

    public void setReturnValue(int offset, BValue bValue) {
        currentFrame.returnValues[offset] = bValue;
    }
    
    public Iterator<StackFrame> iterator() {
        return this.stack.iterator();
    }
    
    public Stack<StackFrame> getStack() {
        return this.stack;
    }
}
