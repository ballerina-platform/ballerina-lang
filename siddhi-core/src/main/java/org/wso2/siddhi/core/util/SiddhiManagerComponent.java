/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.util;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Siddhi Manager Service which is
 *
 * @since 4.0.0-M3-SNAPSHOT
 */
@Component(
        immediate = true
)
public class SiddhiManagerComponent {
    private ServiceRegistration serviceRegistration;

    /**
     * This is the activation method of SiddhiManagerService. This will be initilize the Siddhi Manager and register the
     * ManagerService.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        ReferenceHolder.getInstance().setBundleContext(bundleContext);
        serviceRegistration = bundleContext.registerService(SiddhiComponentActivator.class.getName(),
                new SiddhiComponentActivator(), null);
    }

    protected void stop() throws Exception {
        ReferenceHolder.getInstance().setBundleContext(null);
        serviceRegistration.unregister();
    }
}



