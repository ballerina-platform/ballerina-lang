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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a ChildFirstClassLoader for checking specified jars before the parent.
 *
 * @since 2.0.0
 */
public class ChildFirstClassLoader extends URLClassLoader {

    private ClassLoader system;

    ChildFirstClassLoader(URL[] classpath, ClassLoader parent) {
        super(classpath, parent);
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
    public URL getResource(String name) {
        URL url = findResource(name);
        if (url == null) {
            url = super.getResource(name);
        }
        if (url == null && system != null) {
            url = system.getResource(name);
        }
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> systemUrls = null;
        if (system != null) {
            systemUrls = system.getResources(name);
        }
        Enumeration<URL> localUrls = findResources(name);
        Enumeration<URL> parentUrls = null;
        if (getParent() != null) {
            parentUrls = getParent().getResources(name);
        }
        final List<URL> urls = new ArrayList<>();
        if (localUrls != null) {
            while (localUrls.hasMoreElements()) {
                URL local = localUrls.nextElement();
                urls.add(local);
            }
        }
        if (systemUrls != null) {
            while (systemUrls.hasMoreElements()) {
                urls.add(systemUrls.nextElement());
            }
        }
        if (parentUrls != null) {
            while (parentUrls.hasMoreElements()) {
                urls.add(parentUrls.nextElement());
            }
        }
        return new Enumeration<URL>() {
            Iterator<URL> iter = urls.iterator();

            public boolean hasMoreElements() {
                return iter.hasNext();
            }

            public URL nextElement() {
                return iter.next();
            }
        };
    }
}
