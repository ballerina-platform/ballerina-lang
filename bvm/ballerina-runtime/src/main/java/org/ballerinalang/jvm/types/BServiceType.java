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
package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.AnnotationUtils;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.MapValue;

import java.util.ArrayList;
import java.util.Map;

/**
 * {@code BServiceType} represents a service in Ballerina.
 *
 * @since 0.995.0
 */
public class BServiceType extends BObjectType {

    private ResourceFunction[] resourceFunctions;
    private volatile AttachedFunction[] remoteFunctions;
    private volatile String[] storedResourceNames;

    public BServiceType(String typeName, BPackage pkg, int flags) {
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

    public void setResourceFunctions(ResourceFunction[] resourceFunctions) {
        this.resourceFunctions = resourceFunctions;
    }

    public AttachedFunction[] getRemoteFunctions() {
        if (remoteFunctions == null) {
            AttachedFunction[] funcs = getRemoteFunctions(getAttachedFunctions());
            synchronized (this) {
                if (remoteFunctions == null) {
                    remoteFunctions = funcs;
                }
            }
        }
        return remoteFunctions;
    }

    private AttachedFunction[] getRemoteFunctions(AttachedFunction[] attachedFunctions) {
        ArrayList<AttachedFunction> functions = new ArrayList<>();
        for (AttachedFunction attachedFunction : attachedFunctions) {
            if ((attachedFunction.flags & Flags.REMOTE) == Flags.REMOTE) {
                functions.add(attachedFunction);
            }
        }
        return functions.toArray(new AttachedFunction[]{});
    }

    public ResourceFunction[] getResourceFunctions() {
        return resourceFunctions;
    }

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
        for (Map.Entry<String, BField> entry : this.fields.entrySet()) {
            BField field = entry.getValue();
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
