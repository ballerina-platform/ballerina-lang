/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.core.util;

import org.ballerinalang.siddhi.annotation.Extension;
import org.ballerinalang.siddhi.annotation.classindex.ClassIndex;
import org.ballerinalang.siddhi.core.executor.incremental.IncrementalStartTimeEndTimeFunctionExecutor;
import org.ballerinalang.siddhi.core.executor.incremental.IncrementalTimeGetTimeZone;
import org.ballerinalang.siddhi.core.executor.incremental.IncrementalUnixTimeFunctionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Class used to load Siddhi extensions.
 */
public class SiddhiExtensionLoader {

    private static final Logger log = LoggerFactory.getLogger(SiddhiExtensionLoader.class);

    /**
     * Helper method to load the Siddhi extensions.
     *
     * @param siddhiExtensionsMap reference map for the Siddhi extension
     */
    public static void loadSiddhiExtensions(Map<String, Class> siddhiExtensionsMap) {
        loadLocalExtensions(siddhiExtensionsMap);
    }

    /**
     * Load Siddhi extensions in java non OSGi environment.
     *
     * @param siddhiExtensionsMap reference map for the Siddhi extension
     */
    private static void loadLocalExtensions(Map<String, Class> siddhiExtensionsMap) {
        Iterable<Class<?>> extensions = ClassIndex.getAnnotated(Extension.class);
        for (Class extension : extensions) {
            addExtensionToMap(extension, siddhiExtensionsMap);
        }

        // load extensions related to incremental aggregation
        addExtensionToMap("incrementalAggregator:startTimeEndTime",
                IncrementalStartTimeEndTimeFunctionExecutor.class, siddhiExtensionsMap);
        addExtensionToMap("incrementalAggregator:timestampInMilliseconds",
                IncrementalUnixTimeFunctionExecutor.class, siddhiExtensionsMap);
        addExtensionToMap("incrementalAggregator:getTimeZone",
                IncrementalTimeGetTimeZone.class, siddhiExtensionsMap);
    }

    /**
     * Adding extensions to Siddhi siddhiExtensionsMap.
     *
     * @param extensionClass      extension class
     * @param siddhiExtensionsMap reference map for the Siddhi extension
     */
    private static void addExtensionToMap(Class extensionClass, Map<String, Class> siddhiExtensionsMap) {
        Extension siddhiExtensionAnnotation = (Extension) extensionClass.getAnnotation(Extension.class);
        if (siddhiExtensionAnnotation != null) {
            if (!siddhiExtensionAnnotation.name().isEmpty()) {
                Class previousClass = null;
                if (!siddhiExtensionAnnotation.namespace().isEmpty()) {
                    String key = siddhiExtensionAnnotation.namespace() + SiddhiConstants.EXTENSION_SEPARATOR +
                            siddhiExtensionAnnotation.name();
                    Class existingValue = siddhiExtensionsMap.get(key);
                    if (existingValue == null) {
                        previousClass = siddhiExtensionsMap.put(key, extensionClass);
                    }
                    if (previousClass != null) {
                        log.warn("Dropping extension '" + extensionClass + "' as '" + previousClass + "' was already " +
                                "loaded with the same namespace and name '" +
                                siddhiExtensionAnnotation.namespace() + SiddhiConstants.EXTENSION_SEPARATOR +
                                siddhiExtensionAnnotation.name() + "'");
                    }
                } else {
                    previousClass = siddhiExtensionsMap.put(siddhiExtensionAnnotation.name(), extensionClass);
                    if (previousClass != null) {
                        log.warn("Dropping extension '" + extensionClass + "' as '" + previousClass + "' was already " +
                                "loaded with the " +
                                "same name '" + siddhiExtensionAnnotation.name() + "'");
                    }
                }
            } else {
                log.error("Unable to load extension " + extensionClass.getName() + ", missing Extension annotation.");
            }
        } else {
            log.error("Unable to load extension " + extensionClass.getName() + ", empty name element given in " +
                    "Extension annotation.");
        }
    }

    /**
     * Adding extensions to Siddhi siddhiExtensionsMap.
     *
     * @param fqExtensionName     fully qualified extension name (namespace:extensionName or extensionName)
     * @param extensionClass      extension class
     * @param siddhiExtensionsMap reference map for the Siddhi extension
     */
    private static void addExtensionToMap(String fqExtensionName, Class extensionClass,
                                          Map<String, Class> siddhiExtensionsMap) {
        Class previousClass = null;
        Class existingValue = siddhiExtensionsMap.get(fqExtensionName);
        if (existingValue == null) {
            previousClass = siddhiExtensionsMap.put(fqExtensionName, extensionClass);
        }
        if (previousClass != null) {
            log.warn("Dropping extension '" + extensionClass + "' as '" + previousClass + "' was already " +
                    "loaded with the same namespace and name '" + fqExtensionName + "'");
        }
    }
}
