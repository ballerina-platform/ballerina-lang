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

import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.List;

import static org.ballerinalang.net.grpc.MessageUtils.headersRequired;

/**
 * gRPC service resource class containing metadata to dispatch service request.
 *
 * @since 0.990.4
 */
public class ServiceResource {

    private final Resource resource;
    private final List<ParamDetail> paramDetailList;
    private final boolean headerRequired;

    public ServiceResource(Resource resource) {
        this.resource = resource;
        this.paramDetailList = resource.getParamDetails();
        this.headerRequired = headersRequired(resource);
    }

    public Resource getResource() {
        return resource;
    }

    public List<ParamDetail> getParamDetailList() {
        return paramDetailList;
    }

    public boolean isHeaderRequired() {
        return headerRequired;
    }

    public ProgramFile getProgramFile() {
        return resource.getResourceInfo().getPackageInfo().getProgramFile();
    }
}
