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
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Proxy implementation for JDI object reference.
 *
 * @since 2.0.0
 */
public class ObjectReferenceProxyImpl extends JdiProxy {
    private final ObjectReference myObjectReference;

    //caches
    private ReferenceType myReferenceType;
    private Type myType;
    private ThreeState myIsCollected = ThreeState.UNSURE;

    public ObjectReferenceProxyImpl(VirtualMachineProxyImpl virtualMachineProxy, ObjectReference objectReference) {
        super(virtualMachineProxy);
        myObjectReference = objectReference;
    }

    public ObjectReference getObjectReference() {
        checkValid();
        return myObjectReference;
    }

    public VirtualMachineProxyImpl getVirtualMachineProxy() {
        return (VirtualMachineProxyImpl) myTimer;
    }

    public ReferenceType referenceType() {
        checkValid();
        if (myReferenceType == null) {
            myReferenceType = getObjectReference().referenceType();
        }
        return myReferenceType;
    }

    public Type type() {
        checkValid();
        if (myType == null) {
            myType = getObjectReference().type();
        }
        return myType;
    }

    public String toString() {
        final ObjectReference objectReference = getObjectReference();
        final String objRefStr = objectReference != null ? objectReference.toString() : "[referenced object collected]";
        return "ObjectReferenceProxyImpl: " + objRefStr + " " + super.toString();
    }

    public Map<Field, Value> getValues(List<? extends Field> list) {
        return getObjectReference().getValues(list);
    }

    public void setValue(Field field, Value value) throws InvalidTypeException, ClassNotLoadedException {
        getObjectReference().setValue(field, value);
    }

    public boolean isCollected() {
        checkValid();
        if (myIsCollected != ThreeState.YES) {
            try {
                myIsCollected = ThreeState.fromBoolean(VirtualMachineProxyImpl.isCollected(myObjectReference));
            } catch (VMDisconnectedException ignored) {
                myIsCollected = ThreeState.YES;
            }
        }
        return myIsCollected.toBoolean();
    }

    public long uniqueID() {
        return getObjectReference().uniqueID();
    }

    /**
     * @return a list of waiting ThreadReferenceProxies
     * @throws IncompatibleThreadStateException
     */
    public List<ThreadReferenceProxyImpl> waitingThreads() throws IncompatibleThreadStateException {
        return getObjectReference().waitingThreads().stream().map(getVirtualMachineProxy()::getThreadReferenceProxy)
                .collect(Collectors.toList());
    }

    public ThreadReferenceProxyImpl owningThread() throws IncompatibleThreadStateException {
        return getVirtualMachineProxy().getThreadReferenceProxy(getObjectReference().owningThread());
    }

    public int entryCount() throws IncompatibleThreadStateException {
        return getObjectReference().entryCount();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ObjectReferenceProxyImpl proxy)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        return myObjectReference.equals(proxy.myObjectReference);
    }

    public int hashCode() {
        return myObjectReference.hashCode();
    }

    /**
     * The advice to the proxy to clear cached data.
     */
    @Override
    protected void clearCaches() {
        if (myIsCollected == ThreeState.NO) {
            // clearing cache makes sense only if the object has not been collected yet
            myIsCollected = ThreeState.UNSURE;
        }
    }
}
