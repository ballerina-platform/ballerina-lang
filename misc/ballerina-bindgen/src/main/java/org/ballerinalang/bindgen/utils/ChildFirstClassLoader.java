/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bindgen.utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a ChildFirstClassLoader for checking specified jars before the parent.
 *
 * @since 2.0.0
 */
public class ChildFirstClassLoader extends URLClassLoader {

    private final ClassLoader system;

    ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        system = getSystemClassLoader();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass == null) {
            try {
                loadedClass = findClass(name);
            } catch (ClassNotFoundException e) {
                try {
                    loadedClass = super.loadClass(name, resolve);
                } catch (ClassNotFoundException e2) {
                    if (system != null) {
                        loadedClass = system.loadClass(name);
                    }
                }
            }
        }
        if (resolve) {
            resolveClass(loadedClass);
        }
        return loadedClass;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> allResources = new LinkedList<>();
        Enumeration<URL> sysResource;
        if (system != null) {
            sysResource = system.getResources(name);
            while (sysResource.hasMoreElements()) {
                allResources.add(sysResource.nextElement());
            }
        }
        Enumeration<URL> thisResource = findResources(name);
        while (thisResource.hasMoreElements()) {
            allResources.add(thisResource.nextElement());
        }
        Enumeration<URL> parentResource;
        if (getParent() != null) {
            parentResource = getParent().getResources(name);
            while (parentResource.hasMoreElements()) {
                allResources.add(parentResource.nextElement());
            }
        }
        return Collections.enumeration(allResources);
    }

    @Override
    public URL getResource(String name) {
        URL resource = findResource(name);
        if (resource == null) {
            resource = super.getResource(name);
        }
        if (resource == null && system != null) {
            resource = system.getResource(name);
        }
        return resource;
    }
}
