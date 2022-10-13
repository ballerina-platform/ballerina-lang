/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.projectdesign;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter;

/**
 * Client Capability setter for the {@link ProjectDesignService}.
 *
 * @since 2201.2.2
 */
@JavaSPIService("org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter")
public class ProjectDesignClientCapabilitySetter extends
        BallerinaClientCapabilitySetter<ProjectDesignClientCapabilities> {

    @Override
    public String getCapabilityName() {
        return ProjectDesignConstants.CAPABILITY_NAME;
    }

    @Override
    public Class<ProjectDesignClientCapabilities> getCapability() {
        return ProjectDesignClientCapabilities.class;
    }
}
