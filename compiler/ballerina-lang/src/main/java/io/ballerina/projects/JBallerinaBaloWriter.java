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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

/**
 * This class knows how to create a balo containing jballerina platform libs.
 *
 * @since 2.0.0
 */
public class JBallerinaBaloWriter extends BaloWriter {


    public JBallerinaBaloWriter(JdkVersion targetPlatform) {
        this.target = targetPlatform.code();
    }


    @Override
    protected Optional<JsonArray> addPlatformLibs(ZipOutputStream baloOutputStream, Package pkg)
            throws IOException {
        Path sourceRoot = pkg.project().sourceRoot();
        //If platform libs are defined add them to balo
        PackageManifest.Platform platform = pkg.manifest().platform(target);
        if (platform == null) {
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
        // Iterate platforms and create directories for each target
        for (Map<String, Object> dependency : platform.dependencies()) {
            // todo in the future dependency object can be complex than this
            String path;
            if (dependency.get(PATH) instanceof String) {
                path = (String) dependency.get(PATH);
            } else {
                // This cannot be the case if we have proper Ballerina.toml validation
                continue;
            }
            Path libPath = sourceRoot.resolve(path);
            // null check is added for spot bug with the toml validation filename cannot be null
            String fileName = Optional.ofNullable(libPath.getFileName())
                    .map(p -> p.toString()).orElse("annon");
            Path entryPath = Paths.get(PLATFORM)
                    .resolve(target)
                    .resolve(fileName);
            // create a zip entry for each file
            putZipEntry(baloOutputStream, entryPath, new FileInputStream(libPath.toString()));

            // Create the Package.json entry
            Gson gson = new Gson();
            JsonElement newDependency = gson.toJsonTree(dependency);
            newDependency.getAsJsonObject().addProperty(PATH, entryPath.toString());
            newPlatformLibs.add(newDependency);
        }

        return Optional.of(newPlatformLibs);
    }
}
