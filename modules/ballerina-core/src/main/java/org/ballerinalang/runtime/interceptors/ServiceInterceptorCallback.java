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
import org.ballerinalang.runtime.model.BLangRuntimeRegistry;
import org.ballerinalang.runtime.model.ServerConnector;
import org.ballerinalang.runtime.model.ServiceInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.List;

/**
 * {@link ServiceInterceptorCallback} Wrapper Callback for Resource's response interception.
 */
public class ServiceInterceptorCallback extends DefaultBalCallback {

    private static final Logger breLog = LoggerFactory.getLogger(ServiceInterceptorCallback.class);

    private String protocol;

    public ServiceInterceptorCallback(CarbonCallback parentCallback, String protocol) {
        super(parentCallback);
        this.protocol = protocol;
    }


    @Override
    public void done(CarbonMessage cMsg) {
        try {
            // Engage Response Interceptors.
            ServerConnector serverConnector = BLangRuntimeRegistry.getInstance().getServerConnector(protocol);
            List<ServiceInterceptor> serviceInterceptors = serverConnector.getServiceInterceptorList();
            BMessage message = new BMessage(cMsg);
            for (int i = serviceInterceptors.size() - 1; i >= 0; i--) {
                ServiceInterceptor interceptor = serviceInterceptors.get(i);
                if (interceptor.getResponseFunction() == null) {
                    continue;
                }
                ServiceInterceptor.Result result = BLangVMInterceptors.invokeResourceInterceptor(interceptor,
                        interceptor.getResponseFunction(), message);
                if (result.getMessageIntercepted() == null) {
                    // Can't Intercept null message further. Let it handle at server connector level.
                    breLog.error("error in service interception, return message null in " +
                            (".".equals(interceptor.getPackageInfo().getPkgPath()) ? "" :
                                    interceptor.getPackageInfo().getPkgPath() + ":") +
                            ServiceInterceptor.RESPONSE_INTERCEPTOR_NAME);
                    parentCallback.done(null);
                    return;
                }
                message = result.getMessageIntercepted();
                if (!result.isInvokeNext()) {
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
