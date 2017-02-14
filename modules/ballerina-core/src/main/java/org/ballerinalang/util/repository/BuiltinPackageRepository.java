/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.util.repository;

import org.wso2.ballerina.core.exception.BallerinaException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Represents a repository contains built in ballerina packages.
 * 
 * @since 0.8.0
 */
public class BuiltinPackageRepository extends PackageRepository {
    
    private Class<?> nativePackageProvider;
    private static final String BASE_PATH = "META-INF" + File.separator + "natives" + File.separator;
    private static final String BAL_FILE_EXT = ".bal";
    
    private String packageDirPath;
    
    public BuiltinPackageRepository(Class providerClass) {
        this.nativePackageProvider = providerClass;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PackageSource loadPackage(Path packageDirPath) {
        this.packageDirPath = packageDirPath.toString();
        Map<String, InputStream> sourceFileStreamMap = new HashMap<String, InputStream>();
        ClassLoader classLoader = nativePackageProvider.getClassLoader();
        
        // Get the names of the source files in the package
        List<String> fileNames =  getFileNames(classLoader);
        
        // Read all resources as input streams and create the package source 
        for (String fileName : fileNames) {
            InputStream balSourceStream = classLoader.getResourceAsStream(BASE_PATH + packageDirPath.toString()
                + File.separator + fileName);
            sourceFileStreamMap.put(fileName, balSourceStream);
        }
        PackageSource builtInBalSource = new PackageSource(packageDirPath, sourceFileStreamMap, this);
        return builtInBalSource;
    }
    
    /**
     * Get all the file names listed under the package.
     * 
     * @param classLoader Class loader of the package provider class
     * @param packageDirPath Directory path of the package
     * @return
     */
    private List<String> getFileNames(ClassLoader classLoader) {
        URL repoUrl = nativePackageProvider.getProtectionDomain().getCodeSource().getLocation();
        String pkgRelPath = BASE_PATH + packageDirPath;
        if (isJar(repoUrl)) {
            return getPackageNamesFromJar(repoUrl, pkgRelPath);
        } else {
            return getPackageNamesFromClassPath(pkgRelPath);
        }
    }
    
    /**
     * Get package names from the class path.
     * 
     * @param pkgRelPath Relative path of the from the class path
     * @return List of source files in the package
     */
    private List<String> getPackageNamesFromClassPath(String pkgRelPath) {
        List<String> fileNames =  new ArrayList<String>();
        BufferedReader reader = null;
        try {
            InputStream fileNamesStream = nativePackageProvider.getClassLoader().getResourceAsStream(pkgRelPath + File.separator);
            if (fileNamesStream != null) {
                reader = new BufferedReader(new InputStreamReader(fileNamesStream));
                String fileName;
                while ((fileName = reader.readLine()) != null) {
                    fileNames.add(fileName);
                }
            }
        } catch (Exception e) {
            throw new BallerinaException("error while loading built-in package '" + packageDirPath + "'. " 
                    + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {
                }
            }
        }
        return fileNames;
    }
    
    /**
     * Get package names from the jar.
     * 
     * @param repoUrl URL of the repo source (url of the jar)
     * @param pkgRelPath Relative path of the from root of the jar
     * @return List of source files in the package
     */
    private List<String> getPackageNamesFromJar(URL repoUrl, String pkgRelPath) {
        List<String> fileNames =  new ArrayList<String>();
        ZipInputStream jarInputStream = null;
        ZipEntry fileNameEntry;
        try {
            jarInputStream = new ZipInputStream(repoUrl.openStream());
            while((fileNameEntry = jarInputStream.getNextEntry()) != null) {
                String fileName = fileNameEntry.getName();
                if (fileName.startsWith(pkgRelPath) && fileName.endsWith(BAL_FILE_EXT)) {
                    // get only the file name 
                    fileName = Paths.get(pkgRelPath).getFileName().toString();
                    fileNames.add(fileName);
                }
            }
        } catch (Exception e) {
            throw new BallerinaException("error while loading built-in package '" + packageDirPath + "'. " 
                    + e.getMessage());
        } finally {
            if (jarInputStream != null) {
                try {
                    jarInputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
        
        return fileNames;
    }
    
    /**
     * Check whether the given url represent a jar.
     * 
     * @param url
     * @return
     */
    private boolean isJar(URL url) {
        return url.toString().endsWith(".jar");
    }

    @Override
    public PackageSource loadFile(Path filePath) {
        // TODO
        return null;
    }
}
