/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.runtime.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.runtime.registry.PackageRegistry;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

/**
 * Service component for Ballerina native construct providers.
 *
 * This will wait until all the Ballerina Native Construct providers availability and register each native
 * construct in PackageRegistry.
 */
@Component(
        name = "org.wso2.ballerina.core.runtime.internal.NativeConstructProviderServiceComponent",
        immediate = true,
        property = {
                "componentName=ballerina-native-provider"
        }
)
public class NativeConstructProviderServiceComponent implements RequiredCapabilityListener {

    private static final Logger logger = LoggerFactory.getLogger(NativeConstructProviderServiceComponent.class);

    private BundleContext bundleContext;

    private boolean isAllProviderAvailable;

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

        if (isAllProviderAvailable) {
            bundleContext.registerService(PackageRegistry.class, PackageRegistry.getInstance(), null);
        }
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        if (logger.isDebugEnabled()) {
            logger.debug("All Native Providers available");
        }
        isAllProviderAvailable = true;

        if (bundleContext != null) {
            bundleContext.registerService(PackageRegistry.class, PackageRegistry.getInstance(), null);
        }
    }

    @Reference(
            name = "NativeFunctionProviderService",
            service = AbstractNativeFunction.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterNativeFunctions"
    )
    protected void registerNativeFunctions(AbstractNativeFunction nativeFunction) {
        PackageRegistry.getInstance().registerNativeFunction(nativeFunction);
        if (logger.isDebugEnabled()) {
            logger.debug("Initialized native function {}:{} ", nativeFunction.getPackageName(),
                    nativeFunction.getName());
        }
    }

    protected void unregisterNativeFunctions(AbstractNativeFunction nativeFunction) {
        PackageRegistry.getInstance().unregisterNativeFunctions(nativeFunction);
    }

}
