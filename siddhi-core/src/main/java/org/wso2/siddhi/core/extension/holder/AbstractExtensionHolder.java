/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.extension.holder;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractExtensionHolder {
    public static final String EXTENSION_SEPARATOR = ":";
    static final Logger log = Logger.getLogger(AbstractExtensionHolder.class);

    protected Map<String, Class> extensionMap = new HashMap<String, Class>();

    protected AbstractExtensionHolder(Class clazz, SiddhiContext siddhiContext) {

        // ServiceLoader extensionImpls = ServiceLoader.load(clazz,ClassLoader.getSystemClassLoader());
        if (siddhiContext.getSiddhiExtensions() != null) {
            for (Class extension : siddhiContext.getSiddhiExtensions()) {
                if (clazz.isAssignableFrom(extension)) {
                    if (extension.isAnnotationPresent(SiddhiExtension.class)) {
                        SiddhiExtension siddhiExtension = (SiddhiExtension) extension.getAnnotation(SiddhiExtension.class);
                        String extensionKey = siddhiExtension.namespace() + EXTENSION_SEPARATOR + siddhiExtension.function();
                        if (extensionMap.containsKey(extensionKey)) {
                            log.error("Extension class " + extension.getName() + " not loaded, as there is already an matching extension " + extensionKey + ", implemented at " + extensionMap.get(extensionKey).getName());
                        } else {
                            extensionMap.put(extensionKey, extension);
                        }

                    } else {
                        log.error("Extension class " + extension.getName() + " not loaded, as it does not have SiddhiExtension Annotation!");
                    }
                }
            }
        }
    }

    public Class getExtension(String namespace, String function) {
        String extensionKey = namespace + EXTENSION_SEPARATOR + function;
        return extensionMap.get(extensionKey);

    }

}
