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
 *
 */

package org.ballerinalang.stdlib.task.listener.utils;

import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.codegen.FunctionInfo;

import java.util.Objects;

import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.RESOURCE_ON_ERROR;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.RESOURCE_ON_TRIGGER;

/**
 * Class to extract the resource functions from a service.
 */
public class ResourceFunctionHolder {

    private FunctionInfo onTriggerFunction = null;
    private FunctionInfo onErrorFunction = null;

    /**
     * Creates a resource function holder from a service object.
     *
     * @param service Ballerina service object from which the resource functions should be extracted.
     */
    public ResourceFunctionHolder(Service service) {
        String onErrorResourceFullName = getResourceFullName(service.getBValue().getType(), RESOURCE_ON_ERROR);
        String onTriggerResourceFullName = getResourceFullName(service.getBValue().getType(), RESOURCE_ON_TRIGGER);

        if (Objects.nonNull(service.getServiceInfo().getResourceInfo(onTriggerResourceFullName))) {
            onTriggerFunction = service.getServiceInfo().getResourceInfo(onTriggerResourceFullName);
        }
        if (Objects.nonNull(service.getServiceInfo().getResourceInfo(onErrorResourceFullName))) {
            onErrorFunction = service.getServiceInfo().getResourceInfo(onErrorResourceFullName);
        }
    }

    /**
     * Get the <code>onTrigger</code> function
     *
     * @return onTrigger function related to the service.
     */
    public FunctionInfo getOnTriggerFunction() {
        return this.onTriggerFunction;
    }

    /**
     * Get the <code>onError</code> function
     *
     * @return onError function related to the service.
     */
    public FunctionInfo getOnErrorFunction() {
        return this.onErrorFunction;
    }

    /**
     * Buildup the full name of a resource function.
     *
     * @param serviceType  Ballerina service type.
     * @param resourceName Particular resource name to build up the full name.
     * @return Full name of the resource function.
     */
    private String getResourceFullName(BType serviceType, String resourceName) {
        return serviceType.getName() + "." + resourceName;
    }
}
