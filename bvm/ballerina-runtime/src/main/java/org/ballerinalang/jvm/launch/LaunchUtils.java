/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.jvm.launch;

import java.util.ServiceLoader;

import static org.ballerinalang.jvm.util.BLangConstants.UTIL_LOGGING_CONFIG_CLASS_PROPERTY;
import static org.ballerinalang.jvm.util.BLangConstants.UTIL_LOGGING_CONFIG_CLASS_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.UTIL_LOGGING_MANAGER_CLASS_PROPERTY;
import static org.ballerinalang.jvm.util.BLangConstants.UTIL_LOGGING_MANAGER_CLASS_VALUE;

/**
 * Util methods to be used during starting and ending a ballerina program.
 * 
 * @since 1.0
 */
public class LaunchUtils {

    public static void setSystemProperties() {
        // This will add the initial java args required for uber jar. These were added through ballerina startup 
        // script bur for uber jar we need to add them through main method.
        System.setProperty(UTIL_LOGGING_CONFIG_CLASS_PROPERTY, UTIL_LOGGING_CONFIG_CLASS_VALUE);
        System.setProperty(UTIL_LOGGING_MANAGER_CLASS_PROPERTY, UTIL_LOGGING_MANAGER_CLASS_VALUE);
    }

    public static void startListeners(boolean isService) {
        ServiceLoader<LaunchListener> listeners = ServiceLoader.load(LaunchListener.class);
        listeners.forEach(listener -> listener.beforeRunProgram(isService));
    }

    public static void stopListeners(boolean isService) {
        ServiceLoader<LaunchListener> listeners = ServiceLoader.load(LaunchListener.class);
        listeners.forEach(listener -> listener.afterRunProgram(isService));
    }
}
