/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.runtime.interceptors;

import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.runtime.DefaultBalCallback;
import org.ballerinalang.runtime.ServerConnectorMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.List;

/**
 * {@link ResourceInterceptorCallback} Wrapper Callback for Resource's response interception
 */
public class ResourceInterceptorCallback extends DefaultBalCallback {

    private static final Logger breLog = LoggerFactory.getLogger(ResourceInterceptorCallback.class);

    private String protocol;

    public ResourceInterceptorCallback(CarbonCallback parentCallback, String protocol) {
        super(parentCallback);
        this.protocol = protocol;
    }


    @Override
    public void done(CarbonMessage cMsg) {
        try {
            // An instance of ResourceInterceptorCallback is created only when
            // BLangServiceInterceptors.getInstance().isEnabled(protocol) is true and
            // BLangServiceInterceptors.getInstance().getServerConnectorInterceptorInfo(protocol) != null.
            ServerConnectorInterceptorInfo interceptorInfo = BLangServiceInterceptors.getInstance()
                    .getServerConnectorInterceptorInfo(protocol);
            // Engage Response Interceptors.
            List<ResourceInterceptor> responseChain = interceptorInfo.getResponseChain();
            BMessage message = new BMessage(cMsg);
            for (ResourceInterceptor interceptor : responseChain) {
                ResourceInterceptor.Result result = BLangVMInterceptors.invokeResourceInterceptor(interceptor, message);
                if (result.messageIntercepted == null) {
                    // Can't Intercept null message further. Let it handle at server connector level.
                    breLog.error("error in service interception, return message null in " +
                            (".".equals(interceptor.getPackageInfo().getPkgPath()) ? "" :
                                    interceptor.getPackageInfo().getPkgPath() + ":") +
                            interceptor.getInterceptorFunction().getName());
                    parentCallback.done(null);
                    return;
                }

                message = result.messageIntercepted;
                if (!result.invokeNext) {
                    parentCallback.done(message.value());
                    return;
                }
            }
            parentCallback.done(message.value());
        } catch (Throwable throwable) {
            ServerConnectorMessageHandler.handleError(cMsg, parentCallback, throwable);
        }
    }
}
