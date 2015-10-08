/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class SiddhiExtensionLoader {

    private static final String CLASS_PATH = "java.class.path";
    private static final String CLASS_EXT = "[^#]\\S+=\\S+";
    private static final String SIDDHI_EXT = ".*\\.siddhiext";
    private static final String JAR = ".*\\.jar";
    private static final Logger log = Logger.getLogger(SiddhiExtensionLoader.class);

    /**
     * Helper method to load the siddhi extensions
     * @return class map
     */
    public static Map<String, Class> loadSiddhiExtensions() {
        String classPath = System.getProperty(CLASS_PATH);
        if (classPath == null) {
            classPath = ".";
        } else {
            classPath += File.pathSeparator + ".";
        }
        String[] classPathElements = classPath.split(File.pathSeparator);
        Pattern pattern = Pattern.compile(SIDDHI_EXT);
        Collection<String> extensionsList = new ArrayList<String>();

        for (String element : classPathElements) {
            extensionsList.addAll(getResources(element, pattern));
        }

        Map<String, Class> classMap = new HashMap<String, Class>();

        for (String extension : extensionsList) {
            if (extension.matches(CLASS_EXT)) {
                String[] info = extension.split("=");
                try {
                    classMap.put(info[0].trim(), Class.forName(info[1].trim()));
                } catch (ClassNotFoundException e) {
                    log.debug("Cannot load Siddhi extension " + extension);
                }
            }
        }
        return classMap;
    }

    private static Collection<String> getResources(String fileName, Pattern pattern) {
        File file = new File(fileName);
        return getContent(file, pattern);
    }

    private static Collection<String> getContent(File file, Pattern pattern) {
        List<String> resources = new ArrayList<String>();
        if (file.isDirectory()) {
            resources.addAll(getContentFromDirectory(file, pattern));
        } else {
            String fileName = file.getName();
            Pattern jar = Pattern.compile(JAR);
            if (jar.matcher(fileName).matches()) {
                resources.addAll(getContentFromJarFile(file, pattern));
            } else if (pattern.matcher(fileName).matches()) {
                try {
                    InputStream inputStream = new FileInputStream(file.getCanonicalPath());
                    resources.addAll(readContent(fileName, inputStream));
                } catch (IOException ex) {
                    log.error("unable to get input stream of " + fileName, ex);
                }
            }
        }
        return resources;
    }


    private static Collection<String> getContentFromDirectory(File directory, Pattern pattern) {
        List<String> resources = new ArrayList<String>();
        File[] fileList = directory.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                resources.addAll(getContent(file, pattern));
            }
        }
        return resources;
    }

    private static Collection<String> getContentFromJarFile(File file, Pattern pattern) {
        List<String> resources = new ArrayList<String>();
        ZipFile zf = null;
        try {
            zf = new ZipFile(file);
        } catch (ZipException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error viewing zip file for jar:" + file, e);
            }
        } catch (IOException e) {
            log.error("Error viewing zip file for jar:" + file, e);
        }

        if (zf != null) {
            Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();
                String fileName = ze.getName();
                if (pattern.matcher(fileName).matches()) {
                    try {
                        InputStream inputStream = zf.getInputStream(ze);
                        resources.addAll(readContent(fileName, inputStream));
                    } catch (IOException ex) {
                        log.error("unable to get input stream of " + fileName + "in jar:" + file, ex);
                    }
                }
            }
            try {
                zf.close();
            } catch (IOException e1) {
                log.error("Error closing zip file created for jar:" + file, e1);
            }
        }

        return resources;
    }

    public static Collection<String> readContent(String fileName, InputStream inputStream) {
        List<String> resources = new ArrayList<String>();
        String namespace = fileName.split("\\.")[0];
        try {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(inputStream)));
                String extensionDetails;
                while ((extensionDetails = br.readLine()) != null) {
                    resources.add(namespace + ":" + extensionDetails);
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException ex) {
            log.error("unable to read file " + fileName + ex);
        }
        return resources;
    }

}
