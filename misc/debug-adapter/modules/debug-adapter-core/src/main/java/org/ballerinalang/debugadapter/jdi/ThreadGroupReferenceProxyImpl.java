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

import com.sun.jdi.ThreadGroupReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Proxy implementation for JDI thread group.
 *
 * @since 2.0.0
 */
public class ThreadGroupReferenceProxyImpl extends ObjectReferenceProxyImpl implements ThreadGroupReferenceProxy {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadGroupReferenceProxyImpl.class);
    //caches
    private ThreadGroupReferenceProxyImpl myParentThreadGroupProxy;
    private boolean myIsParentGroupCached = false;
    private String myName;

    public ThreadGroupReferenceProxyImpl(VirtualMachineProxyImpl virtualMachineProxy,
                                         ThreadGroupReference threadGroupReference) {
        super(virtualMachineProxy, threadGroupReference);
    }

    @Override
    public ThreadGroupReference getThreadGroupReference() {
        return (ThreadGroupReference) getObjectReference();
    }

    public String name() {
        checkValid();
        if (myName == null) {
            myName = getThreadGroupReference().name();
        }
        return myName;
    }

    public ThreadGroupReferenceProxyImpl parent() {
        checkValid();
        if (!myIsParentGroupCached) {
            myParentThreadGroupProxy =
                    getVirtualMachineProxy().getThreadGroupReferenceProxy(getThreadGroupReference().parent());
            myIsParentGroupCached = true;
        }
        return myParentThreadGroupProxy;
    }

    public String toString() {
        return "ThreadGroupReferenceProxy: " + getThreadGroupReference().toString();
    }

    public void suspend() {
        getThreadGroupReference().suspend();
    }

    public void resume() {
        getThreadGroupReference().resume();
    }

    public List<ThreadReferenceProxyImpl> threads() {
        return getThreadGroupReference().threads().stream().map(getVirtualMachineProxy()::getThreadReferenceProxy)
                .collect(Collectors.toList());
    }

    public List<ThreadGroupReferenceProxyImpl> threadGroups() {
        return getThreadGroupReference().threadGroups().stream().map(getVirtualMachineProxy()
                ::getThreadGroupReferenceProxy).collect(Collectors.toList());
    }

    @Override
    public void clearCaches() {
        // myIsParentGroupCached = false;
        // myName = null;
        // myParentThreadGroupProxy = null;
        super.clearCaches();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
