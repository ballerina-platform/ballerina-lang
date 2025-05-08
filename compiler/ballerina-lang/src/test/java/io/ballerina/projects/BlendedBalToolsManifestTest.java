/*
 *  Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

import io.ballerina.projects.internal.BalToolsManifestBuilder;
import io.ballerina.projects.util.BalToolsUtil;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.when;

/**
 * Test cases for the BlendedBalToolsManifest class.
 *
 * @since 2201.13.0
 */
public class BlendedBalToolsManifestTest {
    private final Path balToolsTomlPath = Paths.get("src/test/resources/bal-tools-tomls/bal-tools.toml");
    private final Path distBalToolsTomlPath = Paths.get("src/test/resources/bal-tools-tomls/bal-tools-dist.toml");

    private BalToolsManifest balToolsManifest;
    private BalToolsManifest distBalToolsManifest;
    private final Set<String> toolCommands = Set.of("openapi", "asyncapi", "graphql", "edi", "persist", "grpc");

    /* Test cases
     * 1. Only in dist - persist
     * 2. Only local - consolidate-packages
     * 3. Both in dist and local
     * 3.1 Local active is compatible
     * 3.1.1 local active < dist - edi
     * 3.1.2 local active >= dist - graphql
     * 3.2 Local active is incompatible
     * 3.2.1 Dist > locals - openapi
     * 3.2.2 Locals > dist - asyncapi
     * 4 No locally active versions - grpc
     * 5. Not a tool
     * 5.1 No locally active versions
     * 5.2 No versions at all
     * */

    @BeforeClass
    public void setup() {
        BalToolsToml balToolsToml = BalToolsToml.from(balToolsTomlPath);
        this.balToolsManifest = BalToolsManifestBuilder.from(balToolsToml).build();
        BalToolsToml distBalToolsToml = BalToolsToml.from(distBalToolsTomlPath);
        this.distBalToolsManifest = BalToolsManifestBuilder.from(distBalToolsToml).build();
    }

    @Test
    public void testToolOnlyInDist() {
        // persist
        try (MockedStatic<BalToolsUtil> utils = Mockito.mockStatic(BalToolsUtil.class, CALLS_REAL_METHODS)) {
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(anyString(), anyString(), anyString(), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            when(BalToolsUtil.getInBuiltToolCommands(distBalToolsManifest)).thenReturn(this.toolCommands);

            BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest
                    .from(balToolsManifest, distBalToolsManifest);
            Optional<BalToolsManifest.Tool> activePersistTool = blendedBalToolsManifest.getActiveTool("persist");
            Assert.assertEquals(activePersistTool.orElseThrow().version(), "0.9.0");
        }
    }

    @Test
    public void testToolOnlyInLocal() {
        // consolidate-packages
        try (MockedStatic<BalToolsUtil> utils = Mockito.mockStatic(BalToolsUtil.class, CALLS_REAL_METHODS)) {
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(anyString(), anyString(), anyString(), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                            eq("ballerina"), eq("tool_openapi"), eq("1.3.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.GREATER_THAN);
            when(BalToolsUtil.getInBuiltToolCommands(distBalToolsManifest)).thenReturn(this.toolCommands);

            BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest
                    .from(balToolsManifest, distBalToolsManifest);
            Optional<BalToolsManifest.Tool> activeConsolidatePackagesTool =
                    blendedBalToolsManifest.getActiveTool("consolidate-packages");
            Assert.assertEquals(activeConsolidatePackagesTool.orElseThrow().version(), "0.1.7");
        }
    }

    @Test
    public void testLocalActiveAvailableWithSameDist() {
        try (MockedStatic<BalToolsUtil> utils = Mockito.mockStatic(BalToolsUtil.class, CALLS_REAL_METHODS)) {
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(anyString(), anyString(), anyString(), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                            eq("ballerina"), eq("tool_openapi"), eq("1.3.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.GREATER_THAN);
            when(BalToolsUtil.getInBuiltToolCommands(distBalToolsManifest)).thenReturn(this.toolCommands);

            BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest
                    .from(balToolsManifest, distBalToolsManifest);
            // local active < dist - edi
            Optional<BalToolsManifest.Tool> activeEdiTool = blendedBalToolsManifest.getActiveTool("edi");
            Assert.assertEquals(activeEdiTool.orElseThrow().version(), "1.1.0");

            // local active >= dist - graphql
            Optional<BalToolsManifest.Tool> activeGraphqlTool = blendedBalToolsManifest.getActiveTool("graphql");
            Assert.assertEquals(activeGraphqlTool.orElseThrow().version(), "1.1.5");
        }
    }

    @Test
    public void testLocalActiveWithHigherDist() {
        try (MockedStatic<BalToolsUtil> utils = Mockito.mockStatic(BalToolsUtil.class, CALLS_REAL_METHODS)) {
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(anyString(), anyString(), anyString(), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_openapi"), eq("1.3.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.GREATER_THAN);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_openapi"), eq("1.1.0"), any()))
                            .thenReturn(SemanticVersion.VersionCompatibilityResult.LESS_THAN);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_openapi"), eq("1.2.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_openapi"), eq("1.2.1"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_openapi"), eq("1.2.2"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);

            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_asyncapi"), eq("1.0.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_asyncapi"), eq("1.1.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_asyncapi"), eq("1.2.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.GREATER_THAN);
            when(BalToolsUtil.getInBuiltToolCommands(distBalToolsManifest)).thenReturn(this.toolCommands);

            BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest
                    .from(balToolsManifest, distBalToolsManifest);

            Optional<BalToolsManifest.Tool> activeOpenApiTool = blendedBalToolsManifest.getActiveTool("openapi");
            Assert.assertEquals(activeOpenApiTool.orElseThrow().version(),
                    "1.2.1"); // 1.2.2 is ignored since the repo is local

            Optional<BalToolsManifest.Tool> activeAsyncApiTool = blendedBalToolsManifest.getActiveTool("asyncapi");
            Assert.assertEquals(activeAsyncApiTool.orElseThrow().version(), "1.1.0"); // 1.2.0 is incompatible
        }
    }

    @Test
    public void noLocallyActiveVersions() {
        try (MockedStatic<BalToolsUtil> utils = Mockito.mockStatic(BalToolsUtil.class, CALLS_REAL_METHODS)) {
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(anyString(), anyString(), anyString(), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                            eq("ballerina"), eq("tool_openapi"), eq("1.3.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.GREATER_THAN);
            when(BalToolsUtil.getInBuiltToolCommands(distBalToolsManifest)).thenReturn(this.toolCommands);

            BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest
                    .from(balToolsManifest, distBalToolsManifest);
            Optional<BalToolsManifest.Tool> activeGrpcTool = blendedBalToolsManifest.getActiveTool("grpc");
            Assert.assertEquals(activeGrpcTool.orElseThrow().version(), "1.0.0");
        }
    }
}
