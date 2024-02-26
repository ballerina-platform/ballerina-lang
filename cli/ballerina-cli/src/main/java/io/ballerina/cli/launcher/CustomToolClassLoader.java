/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.launcher;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Custom class loader used to load the tool implementation classes.
 * This prioritizes the classes from the tool jars over the classes from the parent class loader.
 *
 * @since 2201.8.0
 */
public class CustomToolClassLoader extends URLClassLoader {
    private final ClassLoader system;

    public CustomToolClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        system = getSystemClassLoader();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass == null) {
            try {
                // Load from parent if cli or picocli classes. This is to avoid SPI and class loading issues
                if (name.startsWith("io.ballerina.cli") || name.startsWith("picocli")) {
                    loadedClass = super.loadClass(name, resolve);
                } else {
                    // Try to load the class from the URLs
                    loadedClass = findClass(name);
                }
            } catch (ClassNotFoundException e) {
                try {
                    // If not found, delegate to the parent
                    loadedClass = super.loadClass(name, resolve);
                } catch (ClassNotFoundException e2) {
                    // If not found, delegate to the system class loader
                    if (system != null) {
                        loadedClass = system.loadClass(name);
                    }
                }
            }
        }
        if (resolve) {
            resolveClass(Objects.requireNonNull(loadedClass));
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
