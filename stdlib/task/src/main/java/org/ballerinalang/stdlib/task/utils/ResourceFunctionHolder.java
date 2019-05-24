/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
*/

package org.ballerinalang.stdlib.task.utils;

import org.ballerinalang.connector.api.Service;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.util.codegen.FunctionInfo;

import java.util.Objects;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.RESOURCE_ON_TRIGGER;

/**
 * Class to extract the resource functions from a service.
 *
 * @since 0.995.0
 */
public class ResourceFunctionHolder {

    private FunctionInfo onTriggerFunction;
    private AttachedFunction onTriggerResource;

    /**
     * Creates a resource function holder from a service object.
     *
     * @param service Ballerina service object from which the resource functions should be extracted.
     */
    //TODO Remove after migration : implemented using bvm values/types
    ResourceFunctionHolder(Service service) {
        String onTriggerResourceFullName = service.getBValue().getType().getName() + "." + RESOURCE_ON_TRIGGER;
        onTriggerFunction = null;
        if (Objects.nonNull(service.getServiceInfo().getResourceInfo(onTriggerResourceFullName))) {
            onTriggerFunction = service.getServiceInfo().getResourceInfo(onTriggerResourceFullName);
        }
    }

    /**
     * Creates a resource function holder from a service object.
     *
     * @param service Ballerina service object from which the resource functions should be extracted.
     */
    ResourceFunctionHolder(ObjectValue service) {
        String onTriggerResourceFullName = service.getType().getName() + "." + RESOURCE_ON_TRIGGER;
        onTriggerResource = null;
        for (AttachedFunction resource : service.getType().getAttachedFunctions()) {
            //TODO test the name of resource
            if (resource.getName().equals(onTriggerResourceFullName)) {
                onTriggerResource = resource;
            }
        }
    }

    /**
     * Get the <code>onTrigger</code> function.
     *
     * @return onTrigger function related to the service.
     */
    //TODO Remove after migration : implemented using bvm values/types
    FunctionInfo getOnTriggerFunction() {
        return this.onTriggerFunction;
    }

    /**
     * Get the <code>onTrigger</code> function.
     *
     * @return onTrigger function related to the service.
     */
    AttachedFunction getOnTriggerResource() {
        return this.onTriggerResource;
    }
}
