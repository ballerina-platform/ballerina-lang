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
import java.util.Arrays;
import java.util.Collections;

/**
 * An in-memory jar class loader.
 *
 * @since 0.955.0
 */

public class JBallerinaInMemoryClassLoader {

    private URLClassLoader cl;
    
    public JBallerinaInMemoryClassLoader(Path testJarPath, File importsCache) {
        try {
            URLClassLoader importCl = null;
            if (importsCache.isDirectory()) {
                String[] jarFIles = importsCache.list();
                // TODO: fix the class loader ordering. reverseOrder work for now. but may break if 'b' depends on 'a'
                Arrays.sort(jarFIles, Collections.reverseOrder());
                for (String file : jarFIles) {
                    importCl = createClassLoader(new File(importsCache, file), importCl);
                }
            }
            importCl = createClassLoader(testJarPath.toFile(), importCl);
            cl = importCl;
        } catch (MalformedURLException e) {
            throw new BLangCompilerException("error loading jar " + testJarPath, e);
        }
    }

    private URLClassLoader createClassLoader(File jarFile, URLClassLoader optParent) throws MalformedURLException {
        if (optParent == null) {
            optParent = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});
        } else {
            optParent = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, optParent);
        }
        return optParent;
    }

    public Class<?> loadClass(String className) {
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class '" + className + "' cannot be loaded in-memory", e);
        }
    }

}
