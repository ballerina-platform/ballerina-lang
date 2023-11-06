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

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Custom class loader used to load the tool implementation classes.
 * This prioritizes the classes from the tool jars over the classes from the parent class loader.
 *
 * @since 2201.8.0
 */
public class CustomToolClassLoader extends URLClassLoader {

    public CustomToolClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            // Load from parent if cli or picocli classes. This is to avoid SPI and class loading issues
            if (name.startsWith("io.ballerina.cli") || name.startsWith("picocli")) {
                return super.loadClass(name, resolve);
            }
            // First, try to load the class from the URLs
            return findClass(name);
        } catch (ClassNotFoundException e) {
            // If not found, delegate to the parent
            return super.loadClass(name, resolve);
        }
    }
}
