/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http;

import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.List;

/**
 * Resource validator for WebSub Subscriber Services.
 *
 * @since 0.965.0
 */
class WebSubSubscriberServiceValidator {

    static void validateResources(HttpService service) {
        validateResources(service.getResources());
    }

    private static void validateResources(List<HttpResource> resources) {
        for (HttpResource resource : resources) {
            String resourceName = resource.getName();
            switch (resourceName) {
                case WebSubSubscriberConstants.RESOURCE_NAME_VERIFY_INTENT:
                case WebSubSubscriberConstants.RESOURCE_NAME_ON_NOTIFICATION:
                    break;
                default:
                    throw new BallerinaException(String.format("Invalid resource name %s for WebSubSubscriberService. "
                                                        + "Allowed resource names [%s, %s]", resourceName,
                                                        WebSubSubscriberConstants.RESOURCE_NAME_VERIFY_INTENT,
                                                        WebSubSubscriberConstants.RESOURCE_NAME_ON_NOTIFICATION));
            }
        }
    }

}
