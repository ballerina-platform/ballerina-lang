/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
    private final Set<String> toolCommands = Set.of(
            "dummyToolA", "dummyToolB", "dummyToolC", "dummyToolD", "dummyToolE", "dummyToolF", "dummyToolG",
            "dummyToolH");

    /* Test cases
     * 1. Only in dist - dummyToolE
     * 2. Only local - dummyToolX
     * 3. Both in dist and local
     * 3.1 Local active is compatible
     * 3.1.1 local active < dist - dummyToolD
     * 3.1.2 local active >= dist - dummyToolC
     * 3.2 Local active is incompatible
     * 3.2.1 Dist > locals - dummyToolA
     * 3.2.2 Locals > dist - dummyToolB
     * 4 No locally active versions - dummyToolF
     * 5 Local repo test
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
        // dummyToolE
        try (MockedStatic<BalToolsUtil> utils = Mockito.mockStatic(BalToolsUtil.class, CALLS_REAL_METHODS)) {
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(anyString(), anyString(), anyString(), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            when(BalToolsUtil.getInBuiltToolCommands(distBalToolsManifest)).thenReturn(this.toolCommands);

            BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest
                    .from(balToolsManifest, distBalToolsManifest);
            Optional<BalToolsManifest.Tool> activeDummyToolE = blendedBalToolsManifest.getActiveTool("dummyToolE");
            Assert.assertEquals(activeDummyToolE.orElseThrow().version(), "0.9.0");
        }
    }

    @Test
    public void testToolOnlyInLocal() {
        // dummyToolX
        try (MockedStatic<BalToolsUtil> utils = Mockito.mockStatic(BalToolsUtil.class, CALLS_REAL_METHODS)) {
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(anyString(), anyString(), anyString(), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                            eq("ballerina"), eq("tool_dummyToolA"), eq("1.3.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.GREATER_THAN);
            when(BalToolsUtil.getInBuiltToolCommands(distBalToolsManifest)).thenReturn(this.toolCommands);

            BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest
                    .from(balToolsManifest, distBalToolsManifest);
            Optional<BalToolsManifest.Tool> activeDummyToolXTool =
                    blendedBalToolsManifest.getActiveTool("dummyToolX");
            Assert.assertEquals(activeDummyToolXTool.orElseThrow().version(), "0.1.7");
        }
    }

    @Test
    public void testLocalActiveAvailableWithSameDist() {
        try (MockedStatic<BalToolsUtil> utils = Mockito.mockStatic(BalToolsUtil.class, CALLS_REAL_METHODS)) {
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(anyString(), anyString(), anyString(), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                            eq("ballerina"), eq("tool_dummyToolA"), eq("1.3.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.GREATER_THAN);
            when(BalToolsUtil.getInBuiltToolCommands(distBalToolsManifest)).thenReturn(this.toolCommands);

            BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest
                    .from(balToolsManifest, distBalToolsManifest);
            // local active < dist - dummyToolD
            Optional<BalToolsManifest.Tool> activeDummyToolD = blendedBalToolsManifest.getActiveTool("dummyToolD");
            Assert.assertEquals(activeDummyToolD.orElseThrow().version(), "1.1.0");

            // local active >= dist - dummyToolC
            Optional<BalToolsManifest.Tool> activeDummyToolC = blendedBalToolsManifest.getActiveTool("dummyToolC");
            Assert.assertEquals(activeDummyToolC.orElseThrow().version(), "1.1.5");
        }
    }

    @Test
    public void testLocalActiveWithHigherDist() {
        try (MockedStatic<BalToolsUtil> utils = Mockito.mockStatic(BalToolsUtil.class, CALLS_REAL_METHODS)) {
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(anyString(), anyString(), anyString(), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_dummyToolA"), eq("1.3.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.GREATER_THAN);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_dummyToolA"), eq("1.1.0"), any()))
                            .thenReturn(SemanticVersion.VersionCompatibilityResult.LESS_THAN);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_dummyToolA"), eq("1.2.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_dummyToolA"), eq("1.2.1"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_dummyToolA"), eq("1.2.2"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);

            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_dummyToolB"), eq("1.0.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_dummyToolB"), eq("1.1.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                    eq("ballerina"), eq("tool_dummyToolB"), eq("1.2.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.GREATER_THAN);
            when(BalToolsUtil.getInBuiltToolCommands(distBalToolsManifest)).thenReturn(this.toolCommands);

            BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest
                    .from(balToolsManifest, distBalToolsManifest);

            Optional<BalToolsManifest.Tool> activeDummyToolA = blendedBalToolsManifest.getActiveTool("dummyToolA");
            Assert.assertEquals(activeDummyToolA.orElseThrow().version(),
                    "1.2.1"); // 1.2.2 is ignored since the repo is local

            Optional<BalToolsManifest.Tool> activeDummyToolB = blendedBalToolsManifest.getActiveTool("dummyToolB");
            Assert.assertEquals(activeDummyToolB.orElseThrow().version(), "1.1.0"); // 1.2.0 is incompatible
        }
    }

    @Test
    public void noLocallyActiveVersions() {
        try (MockedStatic<BalToolsUtil> utils = Mockito.mockStatic(BalToolsUtil.class, CALLS_REAL_METHODS)) {
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(anyString(), anyString(), anyString(), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                            eq("ballerina"), eq("tool_dummyToolA"), eq("1.3.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.GREATER_THAN);
            when(BalToolsUtil.getInBuiltToolCommands(distBalToolsManifest)).thenReturn(this.toolCommands);

            BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest
                    .from(balToolsManifest, distBalToolsManifest);
            Optional<BalToolsManifest.Tool> activeDummyToolF = blendedBalToolsManifest.getActiveTool("dummyToolF");
            Assert.assertEquals(activeDummyToolF.orElseThrow().version(), "1.0.2"); // from the local repo
        }
    }

    @Test
    public void sameToolVersionActiveInLocalRepoAndDist() {
        try (MockedStatic<BalToolsUtil> utils = Mockito.mockStatic(BalToolsUtil.class, CALLS_REAL_METHODS)) {
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(anyString(), anyString(), anyString(), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                            eq("ballerina"), eq("tool_dummyToolG"), eq("2.4.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.GREATER_THAN);
            when(BalToolsUtil.getInBuiltToolCommands(distBalToolsManifest)).thenReturn(this.toolCommands);

            BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest
                    .from(balToolsManifest, distBalToolsManifest);
            Optional<BalToolsManifest.Tool> activeDummyToolF = blendedBalToolsManifest.getActiveTool("dummyToolG");
            Assert.assertEquals(activeDummyToolF.orElseThrow().version(), "2.4.0");
            Assert.assertEquals(activeDummyToolF.orElseThrow().repository(), "local");
        }
    }

    @Test
    public void olderToolVersionActiveInLocalRepo() {
        try (MockedStatic<BalToolsUtil> utils = Mockito.mockStatic(BalToolsUtil.class, CALLS_REAL_METHODS)) {
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(anyString(), anyString(), anyString(), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                            eq("ballerina"), eq("tool_dummyToolH"), eq("1.1.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                            eq("ballerina"), eq("tool_dummyToolH"), eq("1.1.1"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            utils.when(() -> BalToolsUtil.compareToolDistWithCurrentDist(
                            eq("ballerina"), eq("tool_dummyToolH"), eq("1.2.0"), any()))
                    .thenReturn(SemanticVersion.VersionCompatibilityResult.EQUAL);
            when(BalToolsUtil.getInBuiltToolCommands(distBalToolsManifest)).thenReturn(this.toolCommands);

            BlendedBalToolsManifest blendedBalToolsManifest = BlendedBalToolsManifest
                    .from(balToolsManifest, distBalToolsManifest);
            Optional<BalToolsManifest.Tool> activeDummyToolF = blendedBalToolsManifest.getActiveTool("dummyToolH");
            Assert.assertEquals(activeDummyToolF.orElseThrow().version(), "1.1.0");
            Assert.assertEquals(activeDummyToolF.orElseThrow().repository(), "local");
        }
    }
}
