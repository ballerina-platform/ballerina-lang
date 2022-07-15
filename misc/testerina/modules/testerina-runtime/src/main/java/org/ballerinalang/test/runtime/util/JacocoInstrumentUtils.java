/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.runtime.util;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

/**
 * Class containing utility methods required to jacoco offline instrumentation.
 *
 * @since 2201.2.0
 */
public class JacocoInstrumentUtils {

    private static final Instrumenter instrumenter = new Instrumenter(new OfflineInstrumentationAccessGenerator());

    public static void instrumentOffline(List<URL> projectModuleJarList, Path destDir, List<String> mockClassNames)
            throws IOException, ClassNotFoundException {
        URLClassLoader classLoader = new URLClassLoader(projectModuleJarList.toArray(new URL[0]));
        for (String className : mockClassNames) {
            Class<?> clazz = classLoader.loadClass(className);
            File file = new File(destDir.toString(), className.replaceAll("\\.", "/") + ".class");
            file.getParentFile().mkdirs();
            try (InputStream input = clazz.getResourceAsStream(clazz.getSimpleName() + ".class");
                 OutputStream output = new FileOutputStream(file)) {
                instrumenter.instrumentAll(input, output, className);
            }
        }
    }
}
