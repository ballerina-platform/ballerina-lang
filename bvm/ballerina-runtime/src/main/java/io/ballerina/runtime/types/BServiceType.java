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
import io.ballerina.runtime.api.types.ServiceType;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.values.MapValue;

/**
 * {@code BServiceType} represents a service in Ballerina.
 *
 * @since 0.995.0
 */
public class BServiceType extends BObjectType implements ServiceType {

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

    @Override
    public int getTag() {
        return TypeTags.SERVICE_TAG;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
