/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml.parser;

import com.moandjiezana.toml.Toml;
import org.ballerinalang.toml.model.DependencyMetadata;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;

/**
 * Manifest Processor which processes the toml file parsed and populate the Manifest POJO.
 *
 * @since 0.964
 */
public class ManifestProcessor {

    private static final CompilerContext.Key<ManifestProcessor> MANIFEST_PROC_KEY = new CompilerContext.Key<>();
    private static final PrintStream ERR_STREAM = System.err;
    private final Manifest manifest;

    public static ManifestProcessor getInstance(CompilerContext context) {
        ManifestProcessor manifestProcessor = context.get(MANIFEST_PROC_KEY);
        if (manifestProcessor == null) {
            SourceDirectory sourceDirectory = context.get(SourceDirectory.class);
            Manifest manifest = ManifestProcessor.parseTomlContentAsStream(sourceDirectory.getManifestContent());
            ManifestProcessor instance = new ManifestProcessor(manifest);
            context.put(MANIFEST_PROC_KEY, instance);
            return instance;
        }
        return manifestProcessor;
    }

    private ManifestProcessor(Manifest manifest) {
        this.manifest = manifest;
    }

    public Manifest getManifest() {
        return this.manifest;
    }

    /**
     * Get the char stream of the content from file.
     *
     * @param fileName path of the toml file
     * @return manifest object
     * @throws IOException exception if the file cannot be found
     */
    public static Manifest parseTomlContentFromFile(String fileName) throws IOException {
        InputStream targetStream = new FileInputStream(fileName);
        Manifest manifest = parseTomlContentAsStream(targetStream);
        validateManifestDependencies(manifest);
        return manifest;
    }

    /**
     * Get the char stream from string content.
     *
     * @param content toml file content as a string
     * @return manifest object
     */
    public static Manifest parseTomlContentFromString(String content) {
        Toml toml = new Toml().read(content);
        Manifest manifest = toml.to(Manifest.class);
        manifest.getProject().setOrgName(toml.getString("project.org-name"));
        validateManifestDependencies(manifest);
        return manifest;
    }

    /**
     * Get the char stream from inputstream.
     *
     * @param inputStream inputstream of the toml file content
     * @return manifest object
     */
    public static Manifest parseTomlContentAsStream(InputStream inputStream) {
        try {
            Toml toml = new Toml().read(inputStream);
            Manifest manifest = toml.to(Manifest.class);
            manifest.getProject().setOrgName(toml.getString("project.org-name"));
            validateManifestDependencies(manifest);
            return manifest;
        } catch (IllegalStateException ise) {
            ERR_STREAM.println("error occurred parsing Ballerina.toml: " +
                               ise.getMessage().toLowerCase(Locale.getDefault()));
        }
        return new Manifest();
    }
    
    private static void validateManifestDependencies(Manifest manifest) {
        for (Map.Entry<String, Object> dependency : manifest.getDependenciesAsObjectMap().entrySet()) {
            DependencyMetadata metadata = new DependencyMetadata();
            if (dependency.getValue() instanceof String) {
                metadata.setVersion((String) dependency.getValue());
            } else if (dependency.getValue() instanceof Map) {
                Map metadataMap = (Map) dependency.getValue();
                if (metadataMap.keySet().contains("version") && metadataMap.get("version") instanceof String) {
                    metadata.setVersion((String) metadataMap.get("version"));
                }
    
                if (metadataMap.keySet().contains("path") &&  metadataMap.get("path") instanceof String) {
                    metadata.setPath((String) metadataMap.get("path"));
        
                    Path dependencyBaloPath = Paths.get(metadata.getPath());
                    if (!Files.exists(dependencyBaloPath)) {
                        ERR_STREAM.println("balo file for dependency '" + dependency.getKey() + "' does not exists: " +
                                           dependencyBaloPath.toAbsolutePath());
                    }
                }
            } else {
                ERR_STREAM.println("invalid dependency metadata found for: " + dependency.getKey());
            }
        }
    }
}
