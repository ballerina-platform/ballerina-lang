/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.ballerinalang.testerina.util;

import org.ballerinalang.compiler.BLangCompilerException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.StringJoiner;


/**
 * Testarina class loader.
 *
 * @since 1.0.0
 */
public class TestarinaClassLoader {

    private URLClassLoader cl;

    public TestarinaClassLoader(Path testJarPath, HashSet<Path> dependencyJarPaths) {
        try {
            int index = 0;
            URL[] jars;
            jars = new URL[dependencyJarPaths.size() + 1];
            for (Path file : dependencyJarPaths) {
                jars[index++] = file.toUri().toURL();
            }
            jars[index] = testJarPath.toFile().toURI().toURL();
            cl = new URLClassLoader(jars);
        } catch (MalformedURLException e) {
            throw new BLangCompilerException("error loading jar " + testJarPath, e);
        }
    }

    public Class<?> loadClass(String className) {
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class '" + className + "' cannot be loaded in-memory", e);
        }
    }

    public String getClassPath() {
        URL[] urls = cl.getURLs();
        StringJoiner joiner = new StringJoiner(":");
        for (URL url : urls) {
            joiner.add(url.getPath());
        }
        return joiner.toString();
    }

}
