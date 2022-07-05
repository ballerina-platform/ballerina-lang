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
package org.ballerinalang.test.runtime;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom classloader responsible for loading modified classes.
 *
 * @since 2201.1.0
 */
public class CustomClassLoader extends URLClassLoader {

    private final HashMap<String, byte[]> modifiedClassDefs;

    public CustomClassLoader(URL[] urls, ClassLoader parent, Map<String, byte[]> modifiedClassDefs) {
        super(urls, parent);
        this.modifiedClassDefs = new HashMap<>(modifiedClassDefs);
    }

    public void loadClassData(String name, byte[] bytes) {
        modifiedClassDefs.put(name, bytes);
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        byte[] classBytes = this.modifiedClassDefs.remove(name);
        if (classBytes != null) {
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        return super.findClass(name);
    }
}
