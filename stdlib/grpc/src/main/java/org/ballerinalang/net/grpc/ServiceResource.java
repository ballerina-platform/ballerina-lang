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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.grpc;

import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ObjectValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.net.grpc.MessageUtils.headersRequired;

/**
 * gRPC service resource class containing metadata to dispatch service request.
 *
 * @since 0.990.4
 */
public class ServiceResource {

    private final ObjectValue service;
    private final String functionName;
    private final BType[] paramTypes;
    private final boolean headerRequired;
    private final BRuntime runtime;

    public ServiceResource(BRuntime runtime, ObjectValue service, AttachedFunction function) {
        this.service = service;
        this.functionName = function.funcName;
        paramTypes = function.getParameterType();
        this.headerRequired = headersRequired(function);
        this.runtime = runtime;
    }

    public ObjectValue getService() {
        return service;
    }

    public List<BType> getParamTypes() {
        return Collections.unmodifiableList(Arrays.asList(paramTypes));
    }

    public boolean isHeaderRequired() {
        return headerRequired;
    }

    public String getFunctionName() {
        return functionName;
    }

    public BRuntime getRuntime() {
        return runtime;
    }
}
