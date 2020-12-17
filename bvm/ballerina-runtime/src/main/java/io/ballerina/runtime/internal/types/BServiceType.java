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
import io.ballerina.runtime.api.types.MemberFunctionType;
import io.ballerina.runtime.api.types.ResourceFunctionType;
import io.ballerina.runtime.api.types.ServiceType;

import java.util.ArrayList;

/**
 * {@code BServiceType} represents a service in Ballerina.
 *
 * @since 0.995.0
 */
public class BServiceType extends BObjectType implements ServiceType {

    private ResourceFunctionType[] resourceFunctions;
    private volatile MemberFunctionType[] remoteFunctions;

    public BServiceType(String typeName, Module pkg, long flags) {
        super(typeName, pkg, flags);
    }

    public void setResourceFunctions(ResourceFunctionType[] resourceFunctions) {
        this.resourceFunctions = resourceFunctions;
    }

    /**
     * Gen an array of remote functions defined in the service object.
     *
     * @return array of remote functions
     */
    @Override
    public MemberFunctionType[] getRemoteFunctions() {
        if (remoteFunctions == null) {
            MemberFunctionType[] funcs = getRemoteFunctions(getAttachedFunctions());
            synchronized (this) {
                if (remoteFunctions == null) {
                    remoteFunctions = funcs;
                }
            }
        }
        return remoteFunctions;
    }

    private MemberFunctionType[] getRemoteFunctions(MemberFunctionType[] attachedFunctions) {
        ArrayList<MemberFunctionType> functions = new ArrayList<>();
        for (MemberFunctionType funcType : attachedFunctions) {
            if (SymbolFlags.isFlagOn(((BMemberFunctionType) funcType).flags, SymbolFlags.REMOTE)) {
                functions.add(funcType);
            }
        }
        return functions.toArray(new MemberFunctionType[]{});
    }

    /**
     * Get array containing resource functions defined in service object.
     *
     * @return resource functions
     */
    @Override
    public ResourceFunctionType[] getResourceFunctions() {
        return resourceFunctions;
    }

    @Override
    public int getTag() {
        return TypeTags.SERVICE_TAG;
    }
}
