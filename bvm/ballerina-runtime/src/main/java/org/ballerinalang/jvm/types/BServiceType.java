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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;

import static org.ballerinalang.jvm.AnnotationUtils.processServiceAnnotations;

/**
 * {@code BServiceType} represents a service in Ballerina.
 *
 * @since 0.995.0
 */
public class BServiceType extends BObjectType {

    public BServiceType(String typeName, BPackage pkg, int flags) {
        super(typeName, pkg, flags);
    }

    public void setAttachedFuncsAndProcessAnnots(MapValue globalAnnotationMap, Strand strand, BServiceType originalType,
                                                 AttachedFunction[] attachedFunctions) {
        this.setAttachedFunctions(attachedFunctions);
        this.setFields(originalType.getFields());
        this.initializer = originalType.initializer;
        this.defaultsValuesInitFunc = originalType.defaultsValuesInitFunc;

        processServiceAnnotations(globalAnnotationMap, this, strand);
    }

    @Override
    public int getTag() {
        return TypeTags.SERVICE_TAG;
    }
}
