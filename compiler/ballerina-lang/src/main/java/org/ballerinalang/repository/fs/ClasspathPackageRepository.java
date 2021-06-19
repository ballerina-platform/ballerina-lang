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
package org.ballerinalang.repository.fs;

import org.apache.commons.lang3.SystemUtils;
import org.wso2.ballerinalang.compiler.util.Name;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * This represents a Java classpath based {@link org.ballerinalang.repository.PackageRepository}, which can either
 * represent a .jar file or a file system directory.
 *
 * @since 0.94
 */
public class ClasspathPackageRepository extends GeneralFSPackageRepository {
    
    private static final String JAR_URI_SCHEME = "jar";
    private static final String JAR_SOURCE_LOCATION = "/META-INF/";

    public ClasspathPackageRepository(Class<? extends Object> providerClassRef, String orgName) {
        super(generatePath(providerClassRef, orgName), new Name(orgName));
    }

    private static Path generatePath(Class<? extends Object> providerClassRef, String orgName) {
        try {
            URI classURI = providerClassRef.getProtectionDomain().getCodeSource().getLocation().toURI();
            String classPath = classURI.getPath();
            // TODO Fix this properly for other platforms too
            if (SystemUtils.IS_OS_WINDOWS) {
                classPath = classPath.replace(" ", "%20");
            }
            URI pathUri;
            String basePath = JAR_SOURCE_LOCATION + orgName;
            if (classPath.endsWith(".jar")) {
                pathUri = URI.create("jar:file:" + classPath + "!" + basePath);
            } else {
                if (classPath.endsWith(File.separator)) {
                    classPath = classPath.substring(0, classPath.length() - 1);
                }
                pathUri = URI.create("file:" + classPath + basePath);
            }
            initFS(pathUri);
            return Paths.get(pathUri);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void initFS(URI uri) throws IOException {
        if (JAR_URI_SCHEME.equals(uri.getScheme())) {
            Map<String, String> env = new HashMap<>(); 
            env.put("create", "true");
            try {
                FileSystems.newFileSystem(uri, env);
            } catch (FileSystemAlreadyExistsException ignore) { }
        }
    }
    
    @Override
    public String toString() {
        return "ClasspathModuleRepository: " + this.basePath.toUri();
    }

}
