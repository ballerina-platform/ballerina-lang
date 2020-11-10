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

package org.ballerinalang.stdlib.task.objects;

import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.types.AttachedFunctionType;
import io.ballerina.runtime.api.values.BObject;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.RESOURCE_ON_TRIGGER;

/**
 * Custom object to store service with the parameters to use inside the service.
 *
 * @since 0.995.0
 */
public class ServiceInformation {
    private Runtime runtime;
    private BObject service;
    private final Object[] attachments;

    public ServiceInformation(Runtime runtime, BObject service, Object... attachments) {
        this.runtime = runtime;
        this.service = service;
        this.attachments = attachments;
    }

    public BObject getService() {
        return service;
    }

    public String getServiceName() {
        return this.service.getType().getName().split("\\$\\$")[0];
    }

    public AttachedFunctionType getOnTriggerFunction() {
        for (AttachedFunctionType resource : service.getType().getAttachedFunctions()) {
            if (RESOURCE_ON_TRIGGER.equals(resource.getName())) {
                return resource;
            }
        }
        return null;
    }

    public Object[] getAttachment() {
        return this.attachments.clone();
    }

    public Runtime getRuntime() {
        return this.runtime;
    }
}
