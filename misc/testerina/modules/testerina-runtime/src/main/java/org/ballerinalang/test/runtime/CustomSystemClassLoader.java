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

import io.ballerina.projects.util.ProjectConstants;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomSystemClassLoader extends ClassLoader {

    private final HashMap<String, byte[]> modifiedClassDefs;
    private final List<String> excludingClasses = new ArrayList<>();

    public CustomSystemClassLoader() {
        super(getSystemClassLoader());
        this.modifiedClassDefs = new HashMap<>();

        populateExcludingClasses();
    }

    private void populateExcludingClasses() {
        try(InputStream is = BTestMain.class.getResourceAsStream("/" + ProjectConstants.EXCLUDING_CLASSES_FILE);
            BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)))) {
            String line;
            while ((line = br.readLine()) != null) {
                excludingClasses.add(line);
            }
        }
        catch (NullPointerException | IOException e) {
            throw new RuntimeException("Error reading excludingClasses.txt", e);
        }

    }

    public CustomSystemClassLoader(Map<String, byte[]> modifiedClassDefs) {
        super(getSystemClassLoader());
        this.modifiedClassDefs = new HashMap<>(modifiedClassDefs);

        populateExcludingClasses();
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

        //if the class is in not in the excludingClasses list, delegate the classloading to the system classloader
        if (!excludingClasses.contains(name)) {
            return super.loadClass(name);
        }

        return findClass(name);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = this.modifiedClassDefs.remove(name);
        if (classBytes != null) {
            return defineClass(name, classBytes, 0, classBytes.length);
        }

        try {
            classBytes = getClassBytes(name);
            return defineClass(name, classBytes, 0, classBytes.length);
        } catch (IOException e) {
            return super.findClass(name);
        }
    }

    private byte[] getClassBytes(String classFileName) throws IOException {
        String path = File.separator + classFileName.replace('.', File.separatorChar) + ".class";
        InputStream is = BTestMain.class.getResourceAsStream(path);

        int size = Objects.requireNonNull(is).available();

        byte[] buff = new byte[size];

        DataInputStream dis = new DataInputStream(is);
        dis.readFully(buff);
        dis.close();

        return buff;
    }
}
