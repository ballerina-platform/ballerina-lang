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
package org.ballerinalang.util;

import org.ballerinalang.compiler.BLangCompilerException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * An in-memory jar class loader.
 *
 * @since 0.955.0
 */

public class JBallerinaInMemoryClassLoader {

    private URLClassLoader cl;

    public JBallerinaInMemoryClassLoader(Path testJarPath, File importsCache) {
        try {
            int index = 0;
            URL[] jars;
            if (importsCache.isDirectory()) {
                String[] jarFIles = importsCache.list();
                jars = new URL[jarFIles.length + 1];
                for (String file : jarFIles) {
                    jars[index++] = Paths.get(importsCache.getPath(), file).toUri().toURL();
                }
            } else {
                jars = new URL[1];
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

}
