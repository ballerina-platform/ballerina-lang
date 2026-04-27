/*
 *  Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package io.ballerina.projects.util;

import com.google.gson.Gson;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class SbomGeneratorTest {

    @Test
    public void testGenerateBomBasic() throws IOException, org.ballerinalang.toml.exceptions.TomlException {
        Path tmp = Files.createTempDirectory("sbom-test-basic");
        try {
            // Write a minimal Ballerina.toml
            String toml = "[package]\norg = \"ranvin\"\nname = \"math_utils_v2\"\nversion = \"2.1.2\"\n";
            Files.writeString(tmp.resolve("Ballerina.toml"), toml);

            // Run generator
            Path output = tmp.resolve("bom-output.json");
            SbomGenerator.generateBom(tmp.resolve("Ballerina.toml"), output);

            // Find generated .cdx.json
            Path bom = findGeneratedCdxJson(tmp);
            Assert.assertNotNull(bom, "No .cdx.json produced");

            try (BufferedReader r = Files.newBufferedReader(bom)) {
                Gson g = new Gson();
                Map<?, ?> bomJson = g.fromJson(r, Map.class);
                Assert.assertTrue(bomJson.containsKey("components"));
                List<?> comps = (List<?>) bomJson.get("components");
                // Expect at least the project component
                boolean found = comps.stream().anyMatch(o -> {
                    Map<?, ?> m = (Map<?, ?>) o;
                    return ("pkg:ballerina/ranvin/math_utils_v2@2.1.2").equals(m.get("purl"));
                });
                Assert.assertTrue(found, "Project purl component missing");
            }
        } finally {
            deleteRecursively(tmp);
        }
    }

    @Test
    public void testGenerateBomWithDependencies() throws IOException, org.ballerinalang.toml.exceptions.TomlException {
        Path tmp = Files.createTempDirectory("sbom-test-deps");
        try {
            // Ballerina.toml
            String toml = "[package]\norg = \"ranvin\"\nname = \"math_utils_v2\"\nversion = \"2.1.2\"\n";
            Files.writeString(tmp.resolve("Ballerina.toml"), toml);

            // dependencies.toml with a ballerina dep and a maven dep
            String deps = "[[package]]\norg = \"ranvin\"\nname = \"math_utils_v2\"\nversion = \"2.1.2\"\ndependencies = [\n{org = \"ballerina\", name = \"io\", version = \"1.8.0\"},\n{org = \"org.apache.logging.log4j\", name = \"log4j-core\", version = \"2.14.1\"}\n]\n";
            Files.writeString(tmp.resolve("dependencies.toml"), deps);

            Path output = tmp.resolve("bom-output.json");
            SbomGenerator.generateBom(tmp.resolve("Ballerina.toml"), output);

            Path bom = findGeneratedCdxJson(tmp);
            Assert.assertNotNull(bom, "No .cdx.json produced");

            try (BufferedReader r = Files.newBufferedReader(bom)) {
                Gson g = new Gson();
                Map<?, ?> bomJson = g.fromJson(r, Map.class);
                List<?> comps = (List<?>) bomJson.get("components");
                Set<String> purls = comps.stream().map(o -> (Map<?, ?>) o)
                        .map(m -> Objects.toString(((Map<?, ?>) m).get("purl"), "")).collect(Collectors.toSet());

                Assert.assertTrue(purls.contains("pkg:ballerina/ranvin/math_utils_v2@2.1.2"));
                Assert.assertTrue(purls.contains("pkg:ballerina/ballerina/io@1.8.0") || purls.contains("pkg:ballerina/io@1.8.0"),
                        "Expected ballerina io component");
                Assert.assertTrue(purls.contains("pkg:maven/org.apache.logging.log4j/log4j-core@2.14.1"), "Expected maven log4j component");

                // dependencies list should contain an entry for the project ref
                List<?> depsList = (List<?>) bomJson.get("dependencies");
                Optional<?> projectNode = depsList.stream().filter(o -> {
                    Map<?, ?> n = (Map<?, ?>) o;
                    return "pkg:ballerina/ranvin/math_utils_v2@2.1.2".equals(n.get("ref"));
                }).findFirst();
                Assert.assertTrue(projectNode.isPresent(), "Missing project dependency node");
                Map<?, ?> node = (Map<?, ?>) projectNode.get();
                List<?> dependsOn = (List<?>) node.get("dependsOn");
                Set<String> depsSet = dependsOn.stream().map(Object::toString).collect(Collectors.toSet());
                Assert.assertTrue(depsSet.contains("pkg:ballerina/ballerina/io@1.8.0") || depsSet.contains("pkg:ballerina/io@1.8.0"));
                Assert.assertTrue(depsSet.contains("pkg:maven/org.apache.logging.log4j/log4j-core@2.14.1"));
            }
        } finally {
            deleteRecursively(tmp);
        }
    }

    private static Path findGeneratedCdxJson(Path dir) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.cdx.json")) {
            for (Path p : ds) {
                return p;
            }
        }
        // also try any file with .cdx.json suffix anywhere under dir
        try (var walk = Files.walk(dir)) {
            return walk.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".cdx.json")).findFirst().orElse(null);
        }
    }

    private static void deleteRecursively(Path p) throws IOException {
        if (Files.notExists(p)) return;
        try (var walk = Files.walk(p)) {
            walk.sorted(Comparator.reverseOrder()).forEach(tp -> {
                try {
                    Files.deleteIfExists(tp);
                } catch (IOException e) {
                    // ignore
                }
            });
        }
    }
}
