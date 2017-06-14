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
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.List;

/**
 * {@link ResourceInterceptorCallback} Wrapper Callback for Resource's response interception
 */
public class ResourceInterceptorCallback extends DefaultBalCallback {

    private String protocol;

    public ResourceInterceptorCallback(CarbonCallback parentCallback, String protocol) {
        super(parentCallback);
        this.protocol = protocol;
    }


    @Override
    public void done(CarbonMessage cMsg) {
        try {
            if (!ServiceInterceptors.getInstance().isEnabled(protocol)) {
                parentCallback.done(cMsg);
                return;
            }
            ServerConnectorInterceptorInfo interceptorInfo = ServiceInterceptors.getInstance()
                    .getServerConnectorInterceptorInfo(protocol);
            if (interceptorInfo == null) {
                parentCallback.done(cMsg);
                return;
            }
            // Engage Response Interceptors.
            List<ResourceInterceptor> responseChain = interceptorInfo.getResponseChain();
            BMessage message = new BMessage(cMsg);
            for (ResourceInterceptor interceptor : responseChain) {
                ResourceInterceptor.Result result = BLangVMInterceptors.invokeResourceInterceptor(interceptor, message);
                if (result.messageIntercepted == null) {
                    message = new BMessage(null);
                } else {
                    message = result.messageIntercepted;
                }
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
