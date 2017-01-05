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
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils that internal to Ballerina Runtime i.e OSGi.
 */
public class RuntimeUtils {

    private static Logger logger = LoggerFactory.getLogger(RuntimeUtils.class);

    private RuntimeUtils() {
    }

    /**
     * Shutdown Ballerina Runtime.
     */
    public static void shutdownRuntime() {
        BundleContext bundleContext = ServiceContextHolder.getInstance().getBundleContext();
        if (bundleContext != null) {
            try {
                bundleContext.getBundle(0).stop();
            } catch (BundleException e) {
                logger.error("Fatal: Error while unloading Ballerina runtime. Manual termination is required.", e);
            }
        } else {
            logger.error("Fatal: Unable to Shutdown Ballerina Runtime. Manual termination is required.");
        }
    }
}
