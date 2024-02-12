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

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomSystemClassLoader extends ClassLoader {

    private final HashMap<String, byte[]> modifiedClassDefs;

    public CustomSystemClassLoader() {
        super(getSystemClassLoader());
        this.modifiedClassDefs = new HashMap<>();
    }

    public CustomSystemClassLoader(Map<String, byte[]> modifiedClassDefs) {
        super(getSystemClassLoader());
        this.modifiedClassDefs = new HashMap<>(modifiedClassDefs);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> loadedClass;

        //return the class if it is already loaded
        loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        //if the class is an inbuilt class, delegate the classloading to the system classloader
        if (name.startsWith("java.") || name.startsWith("javax.")) {
            return super.loadClass(name);
        }

        //get the class directory name
        String classFileName = File.separator + name.replace('.', File.separatorChar) + ".class";

        byte[] classBytes;
        //load the class from the file system
        try {
            //if the class is in the modified class definitions, load the modified class
            classBytes = this.modifiedClassDefs.remove(name);
            if (classBytes != null) {
                loadedClass = defineClass(name, classBytes, 0, classBytes.length);
                resolveClass(loadedClass);
                return loadedClass;
            }


            classBytes = getClassBytes(classFileName);
            loadedClass = defineClass(name, classBytes, 0, classBytes.length);
            resolveClass(loadedClass);
            return loadedClass;
        }
        catch (IOException e) {
            try {
                //if the class is not found in the file system, delegate the classloading to the system classloader
                loadedClass = super.loadClass(name);
                return loadedClass;
            }
            catch (ClassNotFoundException e2) {
                throw new ClassNotFoundException("Class not found : " + name, e2);
            }
        }
    }

    private byte[] getClassBytes(String classFileName) throws IOException {
        InputStream is = BTestMain.class.getResourceAsStream(classFileName);

        int size = Objects.requireNonNull(is).available();

        byte[] buff = new byte[size];

        DataInputStream dis = new DataInputStream(is);
        dis.readFully(buff);
        dis.close();

        return buff;
    }
}
