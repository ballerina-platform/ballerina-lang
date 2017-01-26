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
    private static Map<String, Class> extensionMapList = new HashMap<String, Class>();

    /**
     * Helper method to load the siddhi extensions
     *
     * @return class map
     */
    public static Map<String, Class> loadSiddhiExtensions() {
        try {
            loadAllImplementations(AttributeAggregator.class);
            loadAllImplementations(EvalScript.class);
            loadAllImplementations(EventTable.class);
            loadAllImplementations(FunctionExecutor.class);
            loadAllImplementations(InputMapper.class);
            loadAllImplementations(InputTransport.class);
            loadAllImplementations(OutputMapper.class);
            loadAllImplementations(OutputTransport.class);
            loadAllImplementations(StreamFunctionProcessor.class);
            loadAllImplementations(StreamProcessor.class);
            loadAllImplementations(WindowProcessor.class);
        } catch (IOException e) {
            log.error("Unable to load extension, the URL cannot be read.", e);
        } catch (ClassNotFoundException e) {
            log.error("Unable to load extension, the class found is not loadable.", e);
        }
        return extensionMapList;
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
     * @param extensionBaseClass a superclass or interface for extension base class
     * @throws IOException            if the URL cannot be read
     * @throws ClassNotFoundException if the class found is not loadable
     */
    private static void loadAllImplementations(Class extensionBaseClass) throws IOException,
            ClassNotFoundException {
        List<String> classNames = findAllStrings(extensionBaseClass.getName());
        if (classNames != null) {
            for (String className : classNames) {
                Class extension = classLoader.loadClass(className).asSubclass(extensionBaseClass);
                SiddhiExtension siddhiExtensionAnnotation = (SiddhiExtension) extension.getAnnotation(SiddhiExtension.class);
                if (siddhiExtensionAnnotation != null) {
                    if (!siddhiExtensionAnnotation.name().isEmpty()) {
                        if (!siddhiExtensionAnnotation.namespace().isEmpty()) {
                            extensionMapList.put(siddhiExtensionAnnotation.namespace() + SiddhiConstants.EXTENSION_SEPARATOR +
                                    siddhiExtensionAnnotation.name(), extension);
                        } else {
                            extensionMapList.put(siddhiExtensionAnnotation.name(), extension);
                        }
                    } else {
                        log.error("Unable to load extension " + extension.getName() + ", missing SiddhiExtension annotation.");
                    }
                } else {
                    log.error("Unable to load extension " + extension.getName() + ", empty name element given in SiddhiExtension annotation.");
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
        List<String> stringList = null;
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
}
