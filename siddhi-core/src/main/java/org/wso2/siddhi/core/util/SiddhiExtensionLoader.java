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

package org.wso2.siddhi.core.util;

import org.apache.log4j.Logger;
import org.atteo.classindex.ClassIndex;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.wiring.BundleWiring;
import org.wso2.siddhi.annotation.Extension;

import java.util.HashMap;
import java.util.Map;

/**
 * Class used to load Siddhi extensions
 */
public class SiddhiExtensionLoader {

    private static final Logger log = Logger.getLogger(SiddhiExtensionLoader.class);

    /**
     * Helper method to load the Siddhi extensions
     *
     * @param siddhiExtensionsMap reference map for the Siddhi extension
     */
    public static void loadSiddhiExtensions(Map<String, Class> siddhiExtensionsMap) {
        loadLocalExtensions(siddhiExtensionsMap);
        BundleContext bundleContext = ReferenceHolder.getInstance().getBundleContext();
        if (bundleContext != null) {
            loadExtensionOSGI(bundleContext, siddhiExtensionsMap);
        }
    }

    /**
     * Load Extensions in OSGi environment
     *
     * @param bundleContext       OSGi bundleContext
     * @param siddhiExtensionsMap reference map for the Siddhi extension
     */
    private static void loadExtensionOSGI(BundleContext bundleContext, Map<String, Class> siddhiExtensionsMap) {
        ExtensionBundleListener extensionBundleListener = new ExtensionBundleListener(siddhiExtensionsMap);
        bundleContext.addBundleListener(extensionBundleListener);
        extensionBundleListener.loadAllExtensions(bundleContext);
    }

    /**
     * Load Siddhi extensions in java non OSGi environment
     *
     * @param siddhiExtensionsMap reference map for the Siddhi extension
     */
    private static void loadLocalExtensions(Map<String, Class> siddhiExtensionsMap) {
        Iterable<Class<?>> extensions = ClassIndex.getAnnotated(Extension.class);
        for (Class extension : extensions) {
            addExtensionToMap(extension, siddhiExtensionsMap);
        }
    }

    /**
     * Adding extensions to Siddhi siddhiExtensionsMap
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
     * Class to listen to Bundle changes to update available extensions
     */
    private static class ExtensionBundleListener implements BundleListener {

        private Map<Class, Integer> bundleExtensions = new HashMap<Class, Integer>();
        private Map<String, Class> siddhiExtensionsMap;

        ExtensionBundleListener(Map<String, Class> siddhiExtensionsMap) {
            this.siddhiExtensionsMap = siddhiExtensionsMap;
        }

        @Override
        public void bundleChanged(BundleEvent bundleEvent) {
            if (bundleEvent.getType() == BundleEvent.STARTED) {
                addExtensions(bundleEvent.getBundle());
            } else {
                removeExtensions(bundleEvent.getBundle());
            }
        }

        private void addExtensions(Bundle bundle) {
            ClassLoader classLoader = bundle.adapt(BundleWiring.class).getClassLoader();
            Iterable<Class<?>> extensions = ClassIndex.getAnnotated(Extension.class, classLoader);
            for (Class extension : extensions) {
                addExtensionToMap(extension, siddhiExtensionsMap);
                bundleExtensions.put(extension, (int) bundle.getBundleId());
            }
        }

        private void removeExtensions(Bundle bundle) {
            bundleExtensions.entrySet().stream().filter(entry -> entry.getValue() ==
                    bundle.getBundleId()).forEachOrdered(entry -> {
                siddhiExtensionsMap.remove(entry.getKey());
            });
            bundleExtensions.entrySet().removeIf(entry -> entry.getValue() ==
                    bundle.getBundleId());
        }

        void loadAllExtensions(BundleContext bundleContext) {
            for (Bundle b : bundleContext.getBundles()) {
                if (b.getState() == Bundle.ACTIVE) {
                    addExtensions(b);
                }
            }
        }
    }
}
