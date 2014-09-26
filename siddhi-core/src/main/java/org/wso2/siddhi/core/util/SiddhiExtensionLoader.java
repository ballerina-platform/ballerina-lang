/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class SiddhiExtensionLoader {

    private static final String CLASS_PATH = "java.class.path";
    private static final Logger log = Logger.getLogger(SiddhiExtensionLoader.class);

    /**
     * Helper method to load the siddhi config
     */
    public static Map<String, Class> loadSiddhiExtensions() {
        String classPath = System.getProperty(CLASS_PATH, ".");
        String[] classPathElements = classPath.split(":");

        ArrayList<String> jarFilesInClassPath = new ArrayList<String>();

        Pattern pattern = Pattern.compile(".*.jar");
        for (String element : classPathElements) {
            if (pattern.matcher(element).matches()) {
                jarFilesInClassPath.add(element);
            } else {
                jarFilesInClassPath.addAll(getResourcesFromDirectory(new File(element), pattern));
            }
        }

        Map<String, Class> classMap = new HashMap<String, Class>();

        for (String filePath : jarFilesInClassPath) {
            File file = new File(filePath);
            Collection<String> siddhiExtensionResource = getResourcesFromJarFile(file, Pattern.compile(".*siddhi.extension"));

            if (!siddhiExtensionResource.isEmpty()) {
                for (String resourceName : siddhiExtensionResource) {
                    try {
                        JarFile jar = new JarFile(file);
                        ZipEntry entry = jar.getEntry(resourceName);
                        InputStream inputStream = jar.getInputStream(entry);

                        try {
                            BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(inputStream)));
                            String extensionDetails;
                            while ((extensionDetails = br.readLine()) != null) {
                                String[] info = extensionDetails.split("=");
                                try {
                                    classMap.put(info[0].trim(), Class.forName(info[1].trim()));
                                } catch (ClassNotFoundException e) {
                                    log.error("Cannot load Siddhi extension " + info[1].trim(), e);
                                }
                            }
                        } finally {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        log.error("unable to get file " + file, e);
                    }
                }
            }

        }

        return classMap;
    }


    private static Collection<String> getResourcesFromDirectory(File directory, Pattern pattern) {
        ArrayList<String> resources = new ArrayList<String>();
        File[] fileList = directory.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    resources.addAll(getResourcesFromDirectory(file, pattern));
                } else {
                    try {
                        String fileName = file.getCanonicalPath();
                        if (pattern.matcher(fileName).matches()) {
                            resources.add(fileName);
                        }
                    } catch (IOException e) {
                        log.error("Can not get canonical path of file:" + file, e);
                    }
                }
            }
        }
        return resources;
    }

    private static Collection<String> getResourcesFromJarFile(File file, Pattern pattern) {
        ArrayList<String> resources = new ArrayList<String>();
        ZipFile zf;
        try {
            zf = new ZipFile(file);
        } catch (ZipException e) {
            throw new Error(e);
        } catch (IOException e) {
            throw new Error(e);
        }
        Enumeration e = zf.entries();
        while (e.hasMoreElements()) {
            ZipEntry ze = (ZipEntry) e.nextElement();
            String fileName = ze.getName();
            if (pattern.matcher(fileName).matches()) {
                resources.add(fileName);
            }
        }
        try {
            zf.close();
        } catch (IOException e1) {
            throw new Error(e1);
        }
        return resources;
    }

}
