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

package org.ballerinalang.debugadapter.jdi;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Type;

/**
 * Proxy implementation for JDI local variable.
 *
 * @since 2.0.0
 */
public class LocalVariableProxyImpl extends JdiProxy implements LocalVariableProxy {
    private final StackFrameProxyImpl myFrame;
    private final String myVariableName;
    private final String myTypeName;

    private LocalVariable myVariable;
    private Type myVariableType;

    public LocalVariableProxyImpl(StackFrameProxyImpl frame, LocalVariable variable) {
        super(frame.myTimer);
        myFrame = frame;
        myVariableName = variable.name();
        myTypeName = variable.typeName();
        myVariable = variable;
    }

    @Override
    protected void clearCaches() {
        myVariable = null;
        myVariableType = null;
    }

    public LocalVariable getVariable() throws JdiProxyException {
        checkValid();
        if (myVariable == null) {
            myVariable = myFrame.visibleVariableByNameInt(myVariableName);
            if (myVariable == null) {
                // myFrame is not this variable's frame
                throw new JdiProxyException(new IncompatibleThreadStateException());
            }
        }
        return myVariable;
    }

    public Type getType() throws JdiProxyException, ClassNotLoadedException {
        if (myVariableType == null) {
            myVariableType = getVariable().type();
        }
        return myVariableType;
    }

    public StackFrameProxyImpl getFrame() {
        return myFrame;
    }

    public int hashCode() {
        return 31 * myFrame.hashCode() + myVariableName.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof LocalVariableProxyImpl) {
            LocalVariableProxyImpl proxy = (LocalVariableProxyImpl) o;
            return proxy.myFrame.equals(myFrame) && myVariableName.equals(proxy.myVariableName);
        }
        return false;
    }

    public String name() {
        return myVariableName;
    }

    public String typeName() {
        return myTypeName;
    }

    @Override
    public String toString() {
        return myVariableName;
    }
}
