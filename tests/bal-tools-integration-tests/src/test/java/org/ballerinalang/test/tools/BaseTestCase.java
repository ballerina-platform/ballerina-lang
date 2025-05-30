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
package org.ballerinalang.test.tools;

import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.BalToolsToml;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.context.BalServer;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Parent test class for all bal tools integration test cases.
 * This will provide basic functionality for integration tests.
 * This will initialize a single ballerina instance which will be used by all the test cases throughout.
 *
 * @since 2201.13.0
 */
public class BaseTestCase {

    public static BalServer balServer;

    @BeforeSuite(alwaysRun = true)
    public void initialize() throws BallerinaTestException, IOException {
        balServer = new BalServer(System.getProperty("server.path"));
        Path distRepoPath = Paths.get(System.getProperty("server.path")).resolve("repo");
        Path distBalToolsTomlPath = Paths.get("src/test/resources/bal-tool-tomls/dist-bal-tools.toml");
        compileAndCopyBalas(distRepoPath, distBalToolsTomlPath);
        Path centralRepoPath = Paths.get("build/user-home/.ballerina/repositories/central.ballerina.io");
        Path centralBalToolsTomlPath = Paths.get("src/test/resources/bal-tool-tomls/central-bal-tools.toml");
        compileAndCopyBalas(centralRepoPath, centralBalToolsTomlPath);
    }

    @AfterSuite(alwaysRun = true)
    public void destroy() {
        balServer.cleanup();
    }

    private void compileAndCopyBalas(Path repoPath, Path balToolsTomlPath) throws IOException {
        Path balaTemplate = Paths.get("build/bala-template");
        FileUtils.copyDirectory(new File("src/test/resources/bala-template"), balaTemplate.toFile());

        // Add tools to dist
        BalToolsToml distBalToolsToml = BalToolsToml.from(balToolsTomlPath);
        BalToolsManifest balToolsManifest = BalToolsManifestBuilder.from(distBalToolsToml).build();
        List<BalToolsManifest.Tool> flattenedTools = new ArrayList<>();
        balToolsManifest.tools().values().stream()
                .flatMap(map -> map.values().stream()).flatMap(map -> map.values().stream())
                .sorted(Comparator.comparing(BalToolsManifest.Tool::id)
                        .thenComparing(BalToolsManifest.Tool::version).reversed())
                .forEach(flattenedTools::add);

        for (BalToolsManifest.Tool flattenedTool : flattenedTools) {
            FileUtils.copyDirectory(new File("src/test/resources/bala-template"), balaTemplate.toFile());
            Path balToolToml = balaTemplate.resolve("BalTool.toml");
            String balToolTplContent = Files.readString(balToolToml);
            String balToolContent = balToolTplContent.replace("<TOOL_NAME>", flattenedTool.id())
                    .replace("<VERSION>", flattenedTool.version());
            Files.writeString(balToolToml, balToolContent);

            Path balToml = balaTemplate.resolve("Ballerina.toml");
            String balTomlTplContent = Files.readString(balToml);
            String balTomlContent = balTomlTplContent.replace("<ORG>", flattenedTool.org()).replace(
                    "<NAME>", flattenedTool.name()).replace("<VERSION>", flattenedTool.version());
            Files.writeString(balToml, balTomlContent);
            CompileResult compileResult = BCompileUtil.compileAndCacheBala(balaTemplate, repoPath,
                    BCompileUtil.getTestProjectEnvironmentBuilder());
            if (compileResult.getDiagnosticResult().hasErrors()) {
                throw new RuntimeException("compilation failed for tool bala: " + flattenedTool.id());
            }
            boolean isDummytoolA130 = "dummytoolA".equals(flattenedTool.id())
                    && "1.3.0".equals(flattenedTool.version());
            boolean isDummytoolB120 = "dummytoolB".equals(flattenedTool.id())
                    && "1.2.0".equals(flattenedTool.version());
            if (isDummytoolA130 || isDummytoolB120) {
                // Replace the ballerina_version to a higher dist in the package.json
                Path packageJsonPath = repoPath.resolve("bala").resolve(flattenedTool.org())
                        .resolve(flattenedTool.name()).resolve(flattenedTool.version())
                        .resolve(JvmTarget.JAVA_21.code()).resolve("package.json");
                String packageJsonContent = Files.readString(packageJsonPath);
                String replacedContent = packageJsonContent.replace(
                        RepoUtils.getBallerinaShortVersion(), "2201.99.0");
                Files.writeString(packageJsonPath, replacedContent);
            }
        }
    }
}
