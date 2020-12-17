/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.projects;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

/**
 * This class knows how to create a balo containing jballerina platform libs.
 *
 * @since 2.0.0
 */
public class JBallerinaBaloWriter extends BaloWriter {

    JBallerinaBackend backend;

    public JBallerinaBaloWriter(JBallerinaBackend backend) {
        this.backend = backend;
        this.target = backend.jdkVersion().code();
    }


    @Override
    protected Optional<JsonArray> addPlatformLibs(ZipOutputStream baloOutputStream, Package pkg)
            throws IOException {
        // retrieve platform dependencies that have default scope
        Collection<PlatformLibrary> jars = backend.platformLibraryDependencies(pkg.packageId(),
                PlatformLibraryScope.DEFAULT);
        if (jars.isEmpty()) {
            return Optional.empty();
        }
        // Iterate through native dependencies and add them to balo
        // organization would be
        // -- Balo Root
        //   - libs
        //     - platform
        //       - java11
        //         - java-library1.jar
        //         - java-library2.jar
        JsonArray newPlatformLibs = new JsonArray();
        // Iterate jars and create directories for each target
        for (PlatformLibrary platformLibrary : jars) {
            JarLibrary jar = (JarLibrary) platformLibrary;
            Path libPath = jar.path();
            // null check is added for spot bug with the toml validation filename cannot be null
            String fileName = Optional.ofNullable(libPath.getFileName())
                    .map(p -> p.toString()).orElse("annon");
            Path entryPath = Paths.get(PLATFORM)
                    .resolve(target)
                    .resolve(fileName);
            // create a zip entry for each file
            putZipEntry(baloOutputStream, entryPath, new FileInputStream(libPath.toString()));

            // Create the Package.json entry
            JsonObject newDependency = new JsonObject();
            newDependency.addProperty(JarLibrary.KEY_PATH, entryPath.toString());
            if (jar.artifactId().isPresent() && jar.groupId().isPresent() && jar.version().isPresent()) {
                newDependency.addProperty(JarLibrary.KEY_ARTIFACT_ID, jar.artifactId().get());
                newDependency.addProperty(JarLibrary.KEY_GROUP_ID, jar.groupId().get());
                newDependency.addProperty(JarLibrary.KEY_VERSION, jar.version().get());
            }
            newPlatformLibs.add(newDependency);
        }

        return Optional.of(newPlatformLibs);
    }
}
