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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InconsistentDebugInfoException;
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Proxy implementation for JDI stack frame.
 *
 * @since 2.0.0
 */
public class StackFrameProxyImpl extends JdiProxy implements StackFrameProxy {

    private static final Logger LOG = LoggerFactory.getLogger(StackFrameProxyImpl.class);
    private final ThreadReferenceProxyImpl myThreadProxy;
    private final int myFrameFromBottomIndex; // 1-based

    //caches
    private int myFrameIndex = -1;
    private StackFrame myStackFrame;
    private ObjectReference myThisReference;
    private ClassLoaderReference myClassLoader;
    private ThreeState myIsObsolete = ThreeState.UNSURE;
    private Map<LocalVariable, Value> myAllValues;

    public StackFrameProxyImpl(ThreadReferenceProxyImpl threadProxy, StackFrame frame, int fromBottomIndex) {
        super(threadProxy.getVirtualMachine());
        myThreadProxy = threadProxy;
        myFrameFromBottomIndex = fromBottomIndex;
        myStackFrame = frame;
    }

    public boolean isObsolete() throws JdiProxyException {
        checkValid();
        if (myIsObsolete != ThreeState.UNSURE) {
            return myIsObsolete.toBoolean();
        }
        InvalidStackFrameException error = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                Method method = getMethod(location());
                boolean isObsolete =
                        (getVirtualMachine().canRedefineClasses() && (method == null || method.isObsolete()));
                myIsObsolete = ThreeState.fromBoolean(isObsolete);
                return isObsolete;
            } catch (InvalidStackFrameException e) {
                error = e;
                clearCaches();
            } catch (InternalException e) {
                if (e.errorCode() == JvmtiError.INVALID_METHODID) {
                    myIsObsolete = ThreeState.YES;
                    return true;
                }
                throw e;
            }
        }
        throw new JdiProxyException(error.getMessage(), error);
    }

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }
        try {
            if (myStackFrame != null) {
                myStackFrame.location(); //extra check if jdi frame is valid
            }
            return true;
        } catch (InvalidStackFrameException e) {
            return false;
        }
    }

    @Override
    protected void clearCaches() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("caches cleared " + super.toString());
        }
        myFrameIndex = -1;
        myStackFrame = null;
        myIsObsolete = ThreeState.UNSURE;
        myThisReference = null;
        myClassLoader = null;
        myAllValues = null;
    }

    /**
     * Use with caution. Better access stackframe data through the Proxy's methods
     */

    @Override
    public StackFrame getStackFrame() throws JdiProxyException {
        checkValid();
        if (myStackFrame == null) {
            try {
                final ThreadReference threadRef = myThreadProxy.getThreadReference();
                myStackFrame = threadRef.frame(getFrameIndex());
            } catch (IndexOutOfBoundsException | IncompatibleThreadStateException e) {
                throw new JdiProxyException(e.getMessage(), e);
            } catch (ObjectCollectedException ignored) {
                throw new JdiProxyException("Thread has been collected");
            }
        }
        return myStackFrame;
    }

    @Override
    public int getFrameIndex() throws JdiProxyException {
        checkValid();
        if (myFrameIndex == -1) {
            int count = myThreadProxy.frameCount();
            if (myFrameFromBottomIndex > count) {
                throw new JdiProxyException(new IncompatibleThreadStateException());
            }
            myFrameIndex = count - myFrameFromBottomIndex;
        }
        return myFrameIndex;
    }

    @Override
    public VirtualMachineProxyImpl getVirtualMachine() {
        return (VirtualMachineProxyImpl) myTimer;
    }

    @Override
    public Location location() throws JdiProxyException {
        InvalidStackFrameException error = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                return getStackFrame().location();
            } catch (InvalidStackFrameException e) {
                error = e;
                clearCaches();
            }
        }
        throw new JdiProxyException(error.getMessage(), error);
    }

    @Override
    public ThreadReferenceProxyImpl threadProxy() {
        return myThreadProxy;
    }

    public String toString() {
        try {
            return "StackFrameProxyImpl: " + getStackFrame().toString();
        } catch (JdiProxyException e) {
            return String.format("StackFrameProxyImpl: %s; frameFromBottom = %d threadName = %s", e.getMessage(),
                    myFrameFromBottomIndex, threadProxy().name());
        }
    }

    public ObjectReference thisObject() throws JdiProxyException {
        checkValid();
        try {
            for (int attempt = 0; attempt < 2; attempt++) {
                try {
                    if (myThisReference == null) {
                        myThisReference = getStackFrame().thisObject();
                    }
                    break;
                } catch (InvalidStackFrameException ignored) {
                    clearCaches();
                }
            }
        } catch (InternalException e) {
            // suppress some internal errors caused by bugs in specific JDI implementations
            if (e.errorCode() != JvmtiError.INVALID_METHODID && e.errorCode() != JvmtiError.INVALID_SLOT) {
                throw new JdiProxyException(e.getMessage(), e);
            } else {
                LOG.info("Exception while getting this object", e);
            }
        } catch (IllegalArgumentException e) {
            LOG.info("Exception while getting this object", e);
        } catch (Exception e) {
            if (!getVirtualMachine().canBeModified()) { // do not care in read only vms
                LOG.debug(e.getMessage());
            } else {
                throw e;
            }
        }
        return myThisReference;
    }

    public List<LocalVariableProxyImpl> visibleVariables() throws JdiProxyException {
        RuntimeException error = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                final List<LocalVariable> list = getStackFrame().visibleVariables();
                final List<LocalVariableProxyImpl> locals = new ArrayList<>(list.size());
                for (LocalVariable localVariable : list) {
                    locals.add(new LocalVariableProxyImpl(this, localVariable));
                }
                return locals;
            } catch (InvalidStackFrameException | IllegalArgumentException e) {
                error = e;
                clearCaches();
            } catch (AbsentInformationException e) {
                throw new JdiProxyException(e);
            }
        }
        throw new JdiProxyException(error.getMessage(), error);
    }

    @Override
    public LocalVariableProxyImpl visibleVariableByName(String name) throws JdiProxyException {
        final LocalVariable variable = visibleVariableByNameInt(name);
        return variable != null ? new LocalVariableProxyImpl(this, variable) : null;
    }

    public Value visibleValueByName(String name) throws JdiProxyException {
        LocalVariable variable = visibleVariableByNameInt(name);
        return variable != null ? getValue(new LocalVariableProxyImpl(this, variable)) : null;
    }

    protected LocalVariable visibleVariableByNameInt(String name) throws JdiProxyException {
        InvalidStackFrameException error = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                try {
                    return getStackFrame().visibleVariableByName(name);
                } catch (InvalidStackFrameException e) {
                    error = e;
                    clearCaches();
                }
            } catch (InvalidStackFrameException | AbsentInformationException e) {
                throw new JdiProxyException(e);
            }
        }
        throw new JdiProxyException(error.getMessage(), error);
    }

    public Value getValue(LocalVariableProxyImpl localVariable) throws JdiProxyException {
        Exception error = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                Map<LocalVariable, Value> values = getAllValues();
                LocalVariable variable = localVariable.getVariable();
                if (values.containsKey(variable)) {
                    return values.get(variable);
                } else { // try direct get
                    return getStackFrame().getValue(variable);
                }
            } catch (InvalidStackFrameException e) {
                error = e;
                clearCaches();
            } catch (InconsistentDebugInfoException ignored) {
                clearCaches();
                throw new JdiProxyException("Debug information is inconsistent");
            } catch (InternalException e) {
                if (e.errorCode() == JvmtiError.INVALID_SLOT || e.errorCode() == JvmtiError.ABSENT_INFORMATION) {
                    throw new JdiProxyException("Debug info might be corrupt", e);
                } else {
                    throw e;
                }
            } catch (Exception e) {
                if (!getVirtualMachine().canBeModified()) { // do not care in read only vms
                    LOG.debug(e.getMessage());
                    throw new JdiProxyException("Debug data corrupted");
                } else {
                    throw e;
                }
            }
        }
        throw new JdiProxyException(error.getMessage(), error);
    }

    private Map<LocalVariable, Value> getAllValues() throws JdiProxyException {
        checkValid();
        if (myAllValues == null) {
            try {
                StackFrame stackFrame = getStackFrame();
                myAllValues = new HashMap<>(stackFrame.getValues(stackFrame.visibleVariables()));
            } catch (AbsentInformationException e) {
                throw new JdiProxyException(e);
            } catch (InternalException e) {
                // extra logging for IDEA-141270
                if (e.errorCode() == JvmtiError.INVALID_SLOT || e.errorCode() == JvmtiError.ABSENT_INFORMATION) {
                    LOG.info(e.getMessage());
                    myAllValues = new HashMap<>();
                } else {
                    throw e;
                }
            } catch (Exception e) {
                if (!getVirtualMachine().canBeModified()) { // do not care in read only vms
                    LOG.debug(e.getMessage());
                    myAllValues = new HashMap<>();
                } else {
                    throw e;
                }
            }
        }
        return myAllValues;
    }

    public void setValue(LocalVariableProxyImpl localVariable, Value value)
            throws JdiProxyException, ClassNotLoadedException, InvalidTypeException {
        InvalidStackFrameException error = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                final LocalVariable variable = localVariable.getVariable();
                final StackFrame stackFrame = getStackFrame();
                stackFrame.setValue(variable, value);
                if (myAllValues != null) {
                    // update cached data if any
                    // re-read the value just set from the stackframe to be 100% sure
                    myAllValues.put(variable, stackFrame.getValue(variable));
                }
                return;
            } catch (InvalidStackFrameException e) {
                error = e;
                clearCaches();
            }
        }
        throw new JdiProxyException(error.getMessage(), error);
    }

    public int hashCode() {
        return 31 * myThreadProxy.hashCode() + myFrameFromBottomIndex;
    }

    public boolean equals(final Object obj) {
        if (!(obj instanceof StackFrameProxyImpl frameProxy)) {
            return false;
        }
        if (frameProxy == this) {
            return true;
        }

        return (myFrameFromBottomIndex == frameProxy.myFrameFromBottomIndex) &&
                (myThreadProxy.equals(frameProxy.myThreadProxy));
    }

    public boolean isLocalVariableVisible(LocalVariableProxyImpl var) throws JdiProxyException {
        try {
            return var.getVariable().isVisible(getStackFrame());
        } catch (IllegalArgumentException ignored) {
            // can be thrown if frame's method is different than variable's method
            return false;
        }
    }

    @Override
    public ClassLoaderReference getClassLoader() throws JdiProxyException {
        if (myClassLoader == null) {
            myClassLoader = location().declaringType().classLoader();
        }
        return myClassLoader;
    }

    public boolean isBottom() {
        return myFrameFromBottomIndex == 1;
    }

    public int getIndexFromBottom() {
        return myFrameFromBottomIndex;
    }

    private static Method getMethod(Location location) {
        try {
            return location.method();
        } catch (IllegalArgumentException e) { // Invalid method id
            LOG.info(e.getMessage());
        }
        return null;
    }
}
