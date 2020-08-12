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

import com.sun.jdi.StringReference;

/**
 * Proxy implementation for JDI strings.
 */
public class StringReferenceProxyImpl extends ObjectReferenceProxyImpl {
    private String myStringValue;

    public StringReferenceProxyImpl(VirtualMachineProxyImpl virtualMachineProxy, StringReference objectReference) {
        super(virtualMachineProxy, objectReference);
    }

    public StringReference getStringReference() {
        return (StringReference) getObjectReference();
    }

    public String value() {
        checkValid();
        if (myStringValue == null) {
            myStringValue = getStringReference().value();
        }
        return myStringValue;
    }

    @Override
    public void clearCaches() {
        myStringValue = null;
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
