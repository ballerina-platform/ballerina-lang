/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
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
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.RemoteMethodType;
import io.ballerina.runtime.api.types.ResourceMethodType;
import io.ballerina.runtime.api.types.ServiceType;

import java.util.ArrayList;

/**
 * {@code BServiceType} represents a service in Ballerina.
 *
 * @since 0.995.0
 */
public class BServiceType extends BObjectType implements ServiceType {

    private ResourceMethodType[] resourceMethods;
    private volatile RemoteMethodType[] remoteMethods;

    public BServiceType(String typeName, Module pkg, long flags) {
        super(typeName, pkg, flags);
    }

    public void setResourceMethods(ResourceMethodType[] resourceMethods) {
        this.resourceMethods = resourceMethods;
    }

    /**
     * Gen an array of remote functions defined in the service object.
     *
     * @return array of remote functions
     */
    @Override
    public RemoteMethodType[] getRemoteMethods() {
        if (remoteMethods == null) {
            RemoteMethodType[] funcs = getRemoteFunctions(getMethods());
            synchronized (this) {
                if (remoteMethods == null) {
                    remoteMethods = funcs;
                }
            }
        }
        return remoteMethods;
    }

    private RemoteMethodType[] getRemoteFunctions(MethodType[] attachedFunctions) {
        ArrayList<MethodType> functions = new ArrayList<>();
        for (MethodType funcType : attachedFunctions) {
            if (SymbolFlags.isFlagOn(((BMethodType) funcType).flags, SymbolFlags.REMOTE)) {
                functions.add(funcType);
            }
        }
        return functions.toArray(new RemoteMethodType[]{});
    }

    /**
     * Get array containing resource functions defined in service object.
     *
     * @return resource functions
     */
    @Override
    public ResourceMethodType[] getResourceMethods() {
        return resourceMethods;
    }

    @Override
    public int getTag() {
        return TypeTags.SERVICE_TAG;
    }

    @Override
    public BObjectType duplicate() {
        BServiceType type = new BServiceType(this.typeName, this.pkg, this.flags);
        type.setFields(fields);
        type.setMethods(duplicateArray(getMethods()));
        type.immutableType = this.immutableType;
        type.typeIdSet = this.typeIdSet;
        type.setResourceMethods(duplicateArray(resourceMethods));
        return type;
    }
}
