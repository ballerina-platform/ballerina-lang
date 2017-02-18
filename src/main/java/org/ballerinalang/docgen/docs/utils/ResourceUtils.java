/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.docgen.docs.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Resource utils class.
 */
public class ResourceUtils {

    private static final PrintStream out = System.out;
    private static final String JAVA_CLASS_PATH = "java.class.path";
    private static final String PATH_SEPARATOR = System.getProperty("path.separator");

    /**
     * Copy a resource folder from the class path to a given location
     *
     * @param resourceFolder resource folder name
     * @param destFolderPath destination folder path
     */
    public static void copyResources(String resourceFolder, String destFolderPath) throws IOException {
        // Get resources in the resource folder
        final Collection<String> reourcePathList = ResourceUtils.getResources(resourceFolder);
        for (String resourcePath : reourcePathList) {
            // Prepare resource path
            String targetClassesFolder = "target" + File.separator + "classes";
            if (resourcePath.contains(targetClassesFolder)) {
                resourcePath = resourcePath.split(targetClassesFolder)[1];
            }
            String targetTestClassesFolder = "target" + File.separator + "test-classes";
            if (resourcePath.contains(targetTestClassesFolder)) {
                resourcePath = resourcePath.split(targetTestClassesFolder)[1];
            }
            if (!resourcePath.startsWith(File.separator)) {
                resourcePath = PATH_SEPARATOR + resourcePath;
            }

            InputStream inputStream = ResourceUtils.class.getResourceAsStream(resourcePath);
            try {
                File destFile = new File(destFolderPath + resourcePath);
                createDirectories(destFile);
                Files.copy(inputStream, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                out.println("File copied: " + destFile);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
    }

    /**
     * Get resources found in the resource folder in the class path
     *
     * @param resourceFolder resource folder name
     * @return the resources found in the resource folder
     */
    private static Collection<String> getResources(final String resourceFolder) {
        final ArrayList<String> fileList = new ArrayList<String>();
        final String classPath = System.getProperty(JAVA_CLASS_PATH, ".");
        final String[] classPathElements = classPath.split(PATH_SEPARATOR);
        for (final String classPathElement : classPathElements) {
            if (!isNullOrEmpty(classPathElement)) {
                fileList.addAll(getResources(classPathElement, resourceFolder));
            }
        }
        return fileList;
    }

    /**
     * Get resources from class path element
     *
     * @param classPathElement class path element
     * @param resourceFolder   resource folder name
     * @return resources found in class path element
     */
    private static Collection<String> getResources(final String classPathElement, final String resourceFolder) {
        final ArrayList<String> fileList = new ArrayList<>();
        final File file = new File(classPathElement);
        if (file.isDirectory()) {
            fileList.addAll(getResourcesFromDirectory(file, resourceFolder));
        } else {
            fileList.addAll(getResourcesFromJarFile(file, resourceFolder));
        }
        return fileList;
    }

    /**
     * Get resources in JAR file
     *
     * @param file           jar file
     * @param resourceFolder resource folder name
     * @return resources found in jar file
     */
    private static Collection<String> getResourcesFromJarFile(final File file, final String resourceFolder) {
        final ArrayList<String> fileList = new ArrayList<>();
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(file);
        } catch (final ZipException e) {
            throw new Error(e);
        } catch (final IOException e1) {
            throw new Error(e1);
        }
        final Enumeration entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = (ZipEntry) entries.nextElement();
            final String fileName = entry.getName();
            if (fileName.contains(resourceFolder)) {
                fileList.add(fileName);
            }
        }
        try {
            zipFile.close();
        } catch (final IOException e2) {
            throw new Error(e2);
        }
        return fileList;
    }

    /**
     * Get resource from directory
     *
     * @param directory      directory file
     * @param resourceFolder resource folder name
     * @return resources found in resources folder
     */
    private static Collection<String> getResourcesFromDirectory(final File directory, final String resourceFolder) {
        final ArrayList<String> retval = new ArrayList<>();
        final File[] fileList = directory.listFiles();
        if (fileList == null) {
            return retval;
        }

        for (final File file : fileList) {
            if (file.isDirectory()) {
                retval.addAll(getResourcesFromDirectory(file, resourceFolder));
            } else {
                try {
                    final String fileName = file.getCanonicalPath();
                    if (fileName.contains(resourceFolder)) {
                        retval.add(fileName);
                    }
                } catch (final IOException e) {
                    throw new Error(e);
                }
            }
        }
        return retval;
    }

    /**
     * Create directories
     *
     * @param directoryFile directory file
     * @throws IOException
     */
    private static void createDirectories(File directoryFile) throws IOException {
        Path path = Paths.get(directoryFile.getPath());
        if (path != null) {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        }
    }

    /**
     * Checks a given string is null or empty
     *
     * @param value String value
     * @return returns true if value is null or empty, else false
     */
    private static boolean isNullOrEmpty(String value) {
        return (value == null) || (value.trim().length() == 0);
    }
}
