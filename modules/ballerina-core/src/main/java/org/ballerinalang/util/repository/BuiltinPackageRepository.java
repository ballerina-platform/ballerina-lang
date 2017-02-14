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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a repository contains built in ballerina packages.
 * 
 * @since 0.8.0
 */
public class BuiltinPackageRepository extends PackageRepository {
    
    private Class<?> nativePackageProvider;
    private static final String BASE_PATH = "META-INF" + File.separator + "natives" + File.separator;
    
    public BuiltinPackageRepository(Class providerClass) {
        this.nativePackageProvider = providerClass;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PackageSource loadPackage(Path packageDirPath) {
        Map<String, InputStream> sourceFileStreamMap = new HashMap<String, InputStream>();
        ClassLoader classLoader = nativePackageProvider.getClassLoader();
        List<String> fileNames =  getFileNames(classLoader, packageDirPath);
        
        // Read all resources as input streams and create the package source 
        for (String fileName : fileNames) {
            InputStream balSourceStream = classLoader.getResourceAsStream(BASE_PATH + packageDirPath.toString()
                + File.separator + fileName);
            sourceFileStreamMap.put(fileName, balSourceStream);
        }
        PackageSource builtInBalSource = new PackageSource(packageDirPath, sourceFileStreamMap, this);
        return builtInBalSource;
    }

    @Override
    public PackageSource loadFile(Path filePath) {
        return null;
    }

    /**
     * Get all the file names listed under the package.
     * 
     * @param classLoader Class loader of the package provider class
     * @param packageDirPath Directory path of the package
     * @return
     */
    private List<String> getFileNames(ClassLoader classLoader, Path packageDirPath) {
        List<String> fileNames =  new ArrayList<String>();
        BufferedReader reader = null;
        try {
            InputStream fileNamesStream = classLoader.getResourceAsStream(BASE_PATH + packageDirPath.toString());
            if (fileNamesStream != null) {
                reader = new BufferedReader(new InputStreamReader(fileNamesStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    fileNames.add(line);
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
}
