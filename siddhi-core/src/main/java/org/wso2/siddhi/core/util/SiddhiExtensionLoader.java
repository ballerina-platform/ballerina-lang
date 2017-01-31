/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.function.EvalScript;
import org.wso2.siddhi.core.publisher.OutputMapper;
import org.wso2.siddhi.core.publisher.OutputTransport;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;
import org.wso2.siddhi.core.subscription.InputMapper;
import org.wso2.siddhi.core.subscription.InputTransport;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.annotation.SiddhiExtension;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiddhiExtensionLoader {

    private static final String path = "META-INF/extensions/";
    private static final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private static final Logger log = Logger.getLogger(SiddhiExtensionLoader.class);
    private static List<Class> extensionBaseImplementationList = new ArrayList<Class>();

    /**
     * Helper method to load the siddhi extensions
     *
     * @param siddhiExtensionsMap reference map for the siddhi extension
     */
    public void loadSiddhiExtensions(Map<String, Class> siddhiExtensionsMap) {
        extensionBaseImplementationList.add(AttributeAggregator.class);
        extensionBaseImplementationList.add(EvalScript.class);
        extensionBaseImplementationList.add(EventTable.class);
        extensionBaseImplementationList.add(InputMapper.class);
        extensionBaseImplementationList.add(InputTransport.class);
        extensionBaseImplementationList.add(OutputMapper.class);
        extensionBaseImplementationList.add(OutputTransport.class);
        extensionBaseImplementationList.add(StreamFunctionProcessor.class);
        extensionBaseImplementationList.add(StreamProcessor.class);
        extensionBaseImplementationList.add(WindowProcessor.class);
        extensionBaseImplementationList.add(FunctionExecutor.class);
        loadExtensionsSPI(siddhiExtensionsMap);
        BundleContext bundleContext = SiddhiManagerServiceComponent.getBundleContext();
        if (bundleContext != null) {
            loadExtenstionOSGI(bundleContext, siddhiExtensionsMap);
        }
    }

    private void loadExtensionsSPI(Map<String, Class> siddhiExtensionsMap) {
        try {
            loadAllImplementasions(siddhiExtensionsMap);
        } catch (IOException e) {
            log.error("Unable to load extension, the URL cannot be read.", e);
        } catch (ClassNotFoundException e) {
            log.error("Unable to load extension, the class found is not loadable.", e);
        }
    }

    private void loadExtenstionOSGI(BundleContext bundleContext, Map<String, Class> siddhiExtensionsMap) {
        ExtensionBundleListener extensionBundleListener = new ExtensionBundleListener(bundleContext, siddhiExtensionsMap);
        extensionBundleListener.loadAllExtensions();
    }


    /**
     * Assumes the class specified points to a file in the classpath that contains
     * the name of a class that implements or is a subclass of the specified class.
     * <p/>
     * Any class that cannot be loaded or assigned to the specified interface will be cause
     * an exception to be thrown.
     * <p/>
     * Example classpath:
     * <p/>
     * META-INF/extension/org.wso2.siddhi.core.table.EventTable   # contains the classname
     * org.wso2.siddhi.extension.eventtable.RDBMSEventTable
     * <p/>
     *
     * @param siddhiExtensionsMap a superclass or interface for extension base class
     * @throws IOException            if the URL cannot be read
     * @throws ClassNotFoundException if the class found is not loadable
     */
    private void loadAllImplementasions(Map<String, Class> siddhiExtensionsMap) throws IOException,
            ClassNotFoundException {
        for (Class extensionBaseClass : extensionBaseImplementationList) {
            List<String> classNames = findAllStrings(extensionBaseClass.getName());
            if (classNames != null) {
                for (String className : classNames) {
                    Class extension = classLoader.loadClass(className).asSubclass(extensionBaseClass);
                    addExtensionToMap(extension, siddhiExtensionsMap);
                }
            }
        }
    }

    /**
     * Reads the contents of the found URLs as a list of {@link String}'s and returns them.
     *
     * @param uri resource identifier path.
     * @return a list of the content of each resource URL found
     * @throws IOException if any of the found URLs are unable to be read.
     */
    private static List<String> findAllStrings(String uri) throws IOException {
        String fullUri = path + uri;
        List<String> stringList = new ArrayList<>();
        Enumeration<URL> resources = classLoader.getResources(fullUri);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            stringList = readContentList(url);
        }
        return stringList;
    }

    /**
     * Read the resource file line by line nad return list of the available contents.
     *
     * @param resource resource path.
     * @return list of the classes found in the resource file.
     * @throws IOException if the file is unable to be read.
     */
    private static List<String> readContentList(URL resource) throws IOException {
        List<String> resources = new ArrayList<String>();
        try (InputStream inputStream = resource.openStream()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(inputStream),
                    StandardCharsets.UTF_8))) {
                String extensionDetails;
                while ((extensionDetails = br.readLine()) != null) {
                    resources.add(extensionDetails);
                }
            }
        }
        return resources;
    }

    private void addExtensionToMap(Class extensionClass, Map<String, Class> siddhiExtensionsMap) {
        SiddhiExtension siddhiExtensionAnnotation = (SiddhiExtension) extensionClass.getAnnotation(SiddhiExtension.class);
        if (siddhiExtensionAnnotation != null) {
            if (!siddhiExtensionAnnotation.name().isEmpty()) {
                if (!siddhiExtensionAnnotation.namespace().isEmpty()) {
                    siddhiExtensionsMap.put(siddhiExtensionAnnotation.namespace() + SiddhiConstants.EXTENSION_SEPARATOR +
                            siddhiExtensionAnnotation.name(), extensionClass);
                } else {
                    siddhiExtensionsMap.put(siddhiExtensionAnnotation.name(), extensionClass);
                }
            } else {
                log.error("Unable to load extension " + extensionClass.getName() + ", missing SiddhiExtension annotation.");
            }
        } else {
            log.error("Unable to load extension " + extensionClass.getName() + ", empty name element given in SiddhiExtension annotation.");
        }
    }

    private class ExtensionBundleListener implements BundleListener {

        private Map<Class, Integer> bundleExtensions = new HashMap<Class, Integer>();
        private Map extensionMap;
        private BundleContext bundleContext;

        public ExtensionBundleListener(BundleContext bundleContext, Map<String, Class> siddhiExtensionsMap) {
            this.bundleContext = bundleContext;
            bundleContext.addBundleListener(this);
            extensionMap = siddhiExtensionsMap;
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
            for (Class extensionBaseClass : extensionBaseImplementationList) {
                try {
                    URL classURL = bundle.getEntry(path + extensionBaseClass.getCanonicalName());
                    if (classURL != null) {
                        List<String> classStringList = readContentList(classURL);
                        for (String classString : classStringList) {
                            Class classFile = bundle.loadClass(classString);
                            bundleExtensions.put(classFile, (int) bundle.getBundleId());
                            addExtensionToMap(classFile, extensionMap);
                        }
                    }
                } catch (IOException e) {
                    log.error("Unable to load extension, the URL cannot be read.", e);
                } catch (ClassNotFoundException e) {
                    log.error("Unable to load extension, the class found is not loadable.", e);
                }
            }
        }

        private void removeExtensions(Bundle bundle) {
            bundleExtensions.entrySet().stream().filter(entry -> entry.getValue() == bundle.getBundleId()).forEachOrdered(entry -> {
                extensionMap.remove(entry.getKey());
            });
            bundleExtensions.entrySet().removeIf(entry -> entry.getValue() == bundle.getBundleId());
        }

        public void loadAllExtensions() {
            for (Bundle b : bundleContext.getBundles()) {
                if (b.getState() == Bundle.ACTIVE) {
                    addExtensions(b);
                }
            }
        }
    }
}
