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
package io.ballerina.runtime.types;

import io.ballerina.runtime.AnnotationUtils;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.AttachedFunctionType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.ResourceFunctionType;
import io.ballerina.runtime.api.types.ServiceType;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.util.Flags;
import io.ballerina.runtime.values.MapValue;

import java.util.ArrayList;
import java.util.Map;

/**
 * {@code BServiceType} represents a service in Ballerina.
 *
 * @since 0.995.0
 */
public class BServiceType extends BObjectType implements ServiceType {

    private ResourceFunctionType[] resourceFunctions;
    private volatile AttachedFunctionType[] remoteFunctions;
    private volatile String[] storedResourceNames;

    public BServiceType(String typeName, Module pkg, int flags) {
        super(typeName, pkg, flags);
    }

    public void setAttachedFuncsAndProcessAnnots(MapValue globalAnnotationMap, Strand strand,
                                                         BServiceType originalType,
                                                         AttachedFunction[] attachedFunctions) {
        this.setAttachedFunctions(attachedFunctions);
        this.setFields(originalType.getFields());
        this.initializer = originalType.initializer;
        this.generatedInitializer = originalType.generatedInitializer;

        AnnotationUtils.processServiceAnnotations(globalAnnotationMap, this, strand);
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
    public AttachedFunctionType[] getRemoteFunctions() {
        if (remoteFunctions == null) {
            AttachedFunctionType[] funcs = getRemoteFunctions(getAttachedFunctions());
            synchronized (this) {
                if (remoteFunctions == null) {
                    remoteFunctions = funcs;
                }
            }
        }
        return remoteFunctions;
    }

    private AttachedFunctionType[] getRemoteFunctions(AttachedFunctionType[] attachedFunctions) {
        ArrayList<AttachedFunctionType> functions = new ArrayList<>();
        for (AttachedFunctionType funcType : attachedFunctions) {
            if ((((AttachedFunction) funcType).flags & Flags.REMOTE) == Flags.REMOTE) {
                functions.add(funcType);
            }
        }
        return functions.toArray(new AttachedFunctionType[]{});
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

    /**
     * Get array containing names of stored resource stored in the service object.
     *
     * @return list of stored resource names
     */
    @Override
    public String[] getStoredResourceNames() {
        if (storedResourceNames == null) {
            String[] list = getStoredResourceNamesInternal();
            synchronized (this) {
                if (storedResourceNames == null) {
                    storedResourceNames = list;
                }
            }
        }
        return this.storedResourceNames;
    }

    private String[] getStoredResourceNamesInternal() {
        ArrayList<String> names = new ArrayList<>();
        for (Map.Entry<String, Field> entry : this.fields.entrySet()) {
            BField field = (BField) entry.getValue();
            if ((field.flags & Flags.RESOURCE) == Flags.RESOURCE) {
                names.add(field.name);
            }
        }
        return names.toArray(new String[0]);
    }

    @Override
    public int getTag() {
        return TypeTags.SERVICE_TAG;
    }
}
