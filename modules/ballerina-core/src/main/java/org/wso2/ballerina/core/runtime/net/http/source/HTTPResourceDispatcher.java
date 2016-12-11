/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.runtime.net.http.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.runtime.core.BalCallback;
import org.wso2.ballerina.core.runtime.core.dispatching.ResourceDispatcher;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Resource level dispatching handler for HTTP protocol
 */
public class HTTPResourceDispatcher implements ResourceDispatcher {

    private static final Logger log = LoggerFactory.getLogger(HTTPResourceDispatcher.class);


    @Override
    public boolean dispatch(Service service, Context context, BalCallback callback) {
        CarbonMessage cMsg = context.getCarbonMessage();

        String method = (String) cMsg.getProperty(Constants.HTTP_METHOD);
        String subPath = (String) context.getProperty(Constants.SUB_PATH);

        for (Resource resource : service.getResources()) {
            Annotation subPathAnnotation = resource.getAnnotation(Constants.ANNOTATION_NAME_PATH);
            if (subPathAnnotation == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Path not specified in the Resource");
                }
                continue;
            }

            String subPathAnnotationVal = subPathAnnotation.getValue();

            if ((subPath.startsWith(subPathAnnotationVal) || Constants.DEFAULT_SUB_PATH.equals(subPathAnnotationVal)) &&
                (resource.getAnnotation(method) != null)) {
                return resource.execute(context, callback);
            }
        }

        log.error("No matching Resource found to dispatch the request with Path : " + subPath +
                  " , Method : " + method + " in Service : " + service.getIdentifier().getName());

        return false;
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_HTTP;
    }
}
