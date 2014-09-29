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
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SiddhiExtensionLoader {

    private static final String CLASS_PATH = "java.class.path";
    private static final String SIDDHI_EXT = ".*\\.siddhiext";
    private static final Logger log = Logger.getLogger(SiddhiExtensionLoader.class);

    /**
     * Helper method to load the siddhi extensions
     */
    public static Map<String, Class> loadSiddhiExtensions() {
        String classPath = System.getProperty(CLASS_PATH, ".");
        String[] classPathElements = classPath.split(":");
        Pattern pattern = Pattern.compile(SIDDHI_EXT);
        Collection<String> extensionsList = new ArrayList<String>();

        for (String element : classPathElements) {
            extensionsList.addAll(getResources(element, pattern));
        }

        Map<String, Class> classMap = new HashMap<String, Class>();

        for (String extension : extensionsList) {
            String[] info = extension.split("=");
            try {
                classMap.put(info[0].trim(), Class.forName(info[1].trim()));
            } catch (ClassNotFoundException e) {
               log.error("Cannot load Siddhi extension " + info[1].trim(), e);
            }
        }
        return classMap;
    }

    private static Collection<String> getResources(String element,Pattern pattern){
        List<String> resources = new ArrayList<String>();
        File file = new File(element);
        if(file.isDirectory()){
            resources.addAll(getContentFromDirectory(file, pattern));
        } else{
            resources.addAll(getContentFromJarFile(file, pattern));
        }
        return resources;
    }

    private static Collection<String> getContentFromDirectory(File directory, Pattern pattern) {
        List<String> resources = new ArrayList<String>();
        File[] fileList = directory.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    resources.addAll(getContentFromDirectory(file, pattern));
                } else {
                    try {
                        String fileName = file.getCanonicalPath();
                        Pattern jar  =Pattern.compile(".*.jar");
                        if(jar.matcher(fileName).matches()){
                            resources.addAll(getContentFromJarFile(new File(fileName), pattern));
                        } else if (pattern.matcher(fileName).matches()) {
                            try{
                                InputStream inputStream = new FileInputStream(fileName);
                                resources.addAll(readContent(fileName, inputStream));
                            } catch (IOException ex) {
                                log.error("unable to get input stream of "+ fileName, ex);
                            }
                        }
                    } catch (IOException e) {
                        log.error("unable to get canonical path of file " + file, e);
                    }
                }
            }
        }
        return resources;
    }

    private static Collection<String> getContentFromJarFile(File file, Pattern pattern){
        List<String> resources = new ArrayList<String>();
        ZipFile zf = null;
        try{
            zf = new ZipFile(file);
        } catch( IOException e){
            log.error("Error creating zip file for jar:" + file, e);
        }

        if (zf != null) {
            Enumeration e = zf.entries();
            while(e.hasMoreElements()){
                ZipEntry ze = (ZipEntry) e.nextElement();
                String fileName = ze.getName();
                if(pattern.matcher(fileName).matches()){
                    try{
                        InputStream inputStream = zf.getInputStream(ze);
                        resources.addAll(readContent(fileName, inputStream));
                    } catch (IOException ex) {
                        log.error("unable to get input stream of " + fileName + "in jar:" + file, ex);
                    }
                }
            }
            try{
                zf.close();
            } catch( IOException e1){
                log.error("Error closing zip file created for jar:" + file, e1);
            }
        }

        return resources;
    }

    public static Collection<String> readContent(String fileName,InputStream inputStream){
        List<String> resources = new ArrayList<String>();
        String[] file = fileName.split("/");
        String namespace = file[file.length-1].split("\\.")[0];
        try{
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(inputStream)));
                String extensionDetails;
                while ((extensionDetails = br.readLine()) != null) {
                    resources.add(namespace+":"+extensionDetails);
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
