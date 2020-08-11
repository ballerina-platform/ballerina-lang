/*
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.transport.http.netty.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi BundleActivator of the Netty transport component.
 */
public class HttpTransportActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) {
        HttpTransportContextHolder.getInstance().setBundleContext(bundleContext);
        HandlerExecutor handlerExecutor = new HandlerExecutor();
        HttpTransportContextHolder.getInstance().setHandlerExecutor(handlerExecutor);
    }

    @Override
    public void stop(BundleContext bundleContext) {
    }
}
