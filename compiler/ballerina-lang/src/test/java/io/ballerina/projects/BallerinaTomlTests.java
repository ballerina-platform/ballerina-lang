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

import io.ballerina.projects.internal.ManifestBuilder;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.ballerina.projects.util.ProjectConstants.INTERNAL_VERSION;
import static io.ballerina.projects.util.ProjectConstants.USER_NAME;

/**
 * Test BallerinaToml.
 */
public class BallerinaTomlTests {

    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources");
    private static final Path BAL_TOML_REPO = RESOURCE_DIRECTORY.resolve("ballerina-toml");

    @Test
    public void testValidBallerinaToml() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("valid-ballerina.toml"),
                                                             BAL_TOML_REPO.resolve("valid-dependencies.toml"));
        Assert.assertFalse(packageManifest.diagnostics().hasErrors());

        PackageDescriptor descriptor = packageManifest.descriptor();
        Assert.assertEquals(descriptor.name().value(), "winery");
        Assert.assertEquals(descriptor.org().value(), "foo");
        Assert.assertEquals(descriptor.version().value().toString(), "0.1.0");

        List<PackageManifest.Dependency> dependencies = packageManifest.dependencies();
        Assert.assertEquals(dependencies.size(), 2);
        for (PackageManifest.Dependency dependency : dependencies) {
            Assert.assertEquals(dependency.org().value(), "wso2");
            Assert.assertTrue(dependency.name().value().equals("twitter")
                                      || dependency.name().value().equals("github"));
            Assert.assertTrue(dependency.version().value().toString().equals("2.3.4")
                                      || dependency.version().value().toString().equals("1.2.3"));
        }

        PackageManifest.Platform platform = packageManifest.platform("java11");
        List<Map<String, Object>> platformDependencies = platform.dependencies();
        Assert.assertEquals(platformDependencies.size(), 2);
        for (Map<String, Object> library : platformDependencies) {
            Assert.assertTrue(library.get("path").equals("/user/sameera/libs/toml4j.jar")
                                      || library.get("path").equals("path/to/swagger.jar"));
            Assert.assertTrue(library.get("artifactId").equals("toml4j")
                                      || library.get("artifactId").equals("swagger"));
            Assert.assertEquals(library.get("version"), "0.7.2");
            Assert.assertTrue(library.get("groupId").equals("com.moandjiezana.toml")
                                      || library.get("groupId").equals("swagger.io"));
        }

        Assert.assertEquals(packageManifest.license(), Collections.singletonList("Apache 2.0"));
        Assert.assertEquals(packageManifest.authors(), Arrays.asList("jo", "pramodya"));
        Assert.assertEquals(packageManifest.keywords(), Arrays.asList("toml", "ballerina"));
        Assert.assertEquals(packageManifest.repository(), "https://github.com/ballerina-platform/ballerina-lang");

//        Assert.assertTrue(ballerinaToml.buildOptions().observabilityIncluded());
//        Assert.assertTrue(ballerinaToml.buildOptions().offlineBuild());
//        Assert.assertFalse(ballerinaToml.buildOptions().skipTests());
//        Assert.assertFalse(ballerinaToml.buildOptions().codeCoverage());
//        Assert.assertEquals(ballerinaToml.buildOptions().compilationOptions().getCloud(), "k8s");
//        Assert.assertTrue(ballerinaToml.buildOptions().compilationOptions().getTaintCheck());
    }

    @Test
    public void testSimpleBallerinaToml() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("simple-ballerina.toml"));
        Assert.assertFalse(packageManifest.diagnostics().hasErrors());

        PackageDescriptor descriptor = packageManifest.descriptor();
        Assert.assertEquals(descriptor.name().value(), "lang.annotations");
        Assert.assertEquals(descriptor.org().value(), "ballerina");
        Assert.assertEquals(descriptor.version().value().toString(), "1.0.0");
    }

    @Test
    public void testEmptyBallerinaToml() throws IOException {
        System.setProperty(USER_NAME, "john");
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("empty-ballerina.toml"));
        PackageDescriptor descriptor = packageManifest.descriptor();
        // Package name is the root directory name
        Assert.assertEquals(descriptor.name().value(), "ballerina_toml");
        // Org is user name
        Assert.assertEquals(descriptor.org().value(), "john");
        // Version is 0.1.0
        Assert.assertEquals(descriptor.version().value().toString(), INTERNAL_VERSION);
    }

    @Test
    public void testBallerinaTomlWithEmptyPackage() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("empty-package.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 1);
        Assert.assertEquals(packageManifest.diagnostics().errors().iterator().next().message(),
                            "invalid Ballerina.toml file: "
                                    + "organization, name and the version of the package is missing. example: \n"
                                    + "[package]\n"
                                    + "org=\"my_org\"\n"
                                    + "name=\"my_package\"\n"
                                    + "version=\"1.0.0\"\n");
    }

    @Test
    public void testBallerinaTomlWithoutOrgNameVersion() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("empty-platform-dependency.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 3);

        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();
        Assert.assertEquals(iterator.next().message(),
                            "invalid Ballerina.toml file: cannot find 'org' under [package]");
        Assert.assertEquals(iterator.next().message(),
                            "invalid Ballerina.toml file: cannot find 'name' under [package]");
        Assert.assertEquals(iterator.next().message(),
                            "invalid Ballerina.toml file: cannot find 'version' under [package]");
    }

    @Test
    public void testBallerinaTomlWithInvalidOrgNameVersion() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("invalid-org-name-version.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 3);

        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();
        Assert.assertEquals(iterator.next().message(),
                            "invalid Ballerina.toml file: Invalid 'org' under [package]: 'foo.one' :\n"
                                    + "'org' can only contain alphanumerics, underscores and periods and the maximum "
                                    + "length is 256 characters");
        Assert.assertEquals(iterator.next().message(),
                            "invalid Ballerina.toml file: Invalid 'name' under [package]: 'winery-project' :\n"
                                    + "'name' can only contain alphanumerics, underscores and the maximum "
                                    + "length is 256 characters");
        Assert.assertEquals(iterator.next().message(),
                            "invalid package version in Ballerina.toml. Invalid version: '100'. "
                                    + "Unexpected character 'EOI(null)' at position '3', expecting '[DOT]'");
    }

    @Test
    public void testBallerinaTomlWithPlatformHavingDependencyArray() throws IOException {
        PackageManifest packageManifest = getPackageManifest(
                BAL_TOML_REPO.resolve("platform-with-dependency-array.toml"));
        Assert.assertFalse(packageManifest.diagnostics().hasErrors());

        PackageDescriptor descriptor = packageManifest.descriptor();
        Assert.assertEquals(descriptor.name().value(), "debugger_helpers");
        Assert.assertEquals(descriptor.org().value(), "ballerina");
        Assert.assertEquals(descriptor.version().value().toString(), "1.0.0");

        List<Map<String, Object>> platformDependencies = packageManifest.platform("java11").dependencies();
        Assert.assertEquals(platformDependencies.size(), 0);
    }

    @Test
    public void testBallerinaTomlWithPlatformScopes() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("platfoms-with-scope.toml"));
        Assert.assertFalse(packageManifest.diagnostics().hasErrors());

        PackageManifest.Platform platform = packageManifest.platform("java11");
        List<Map<String, Object>> platformDependencies = platform.dependencies();
        Assert.assertEquals(platformDependencies.size(), 3);
        for (Map<String, Object> library : platformDependencies) {
            if (library.get("path").equals("./libs/ballerina-io-1.2.0-java.txt")) {
                Assert.assertEquals(library.get("scope"), "testOnly");
                Assert.assertEquals(library.get("artifactId"), "ldap");
                Assert.assertEquals(library.get("version"), "1.0.0");
                Assert.assertEquals(library.get("groupId"), "ballerina");
            } else {
                Assert.assertNull(library.get("scope"));
                Assert.assertTrue(library.get("path").equals("/user/sameera/libs/toml4j.jar") || library.get("path")
                        .equals("path/to/swagger.jar"));
                Assert.assertTrue(
                        library.get("artifactId").equals("toml4j") || library.get("artifactId").equals("swagger"));
                Assert.assertEquals(library.get("version"), "0.7.2");
                Assert.assertTrue(library.get("groupId").equals("com.moandjiezana.toml") || library.get("groupId")
                        .equals("swagger.io"));
            }
        }
    }

    @Test
    public void testBallerinaTomlWithPlatformDependencyAsInlineTable() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("inline-table.toml"));
        DiagnosticResult diagnostics = packageManifest.diagnostics();
        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertEquals(diagnostics.errors().iterator().next().message(),
                            "invalid Ballerina.toml file: 'dependency' under 'platform' should be a table array");
    }

    private PackageManifest getPackageManifest(Path tomlPath) throws IOException {
        String tomlContent = Files.readString(tomlPath);
        TomlDocument ballerinaToml = TomlDocument.from(ProjectConstants.BALLERINA_TOML, tomlContent);
        return ManifestBuilder.from(ballerinaToml, null, tomlPath.getParent()).packageManifest();
    }

    private PackageManifest getPackageManifest(Path ballerinaTomlPath, Path dependenciesTomlPath) throws IOException {
        String ballerinaTomlContent = Files.readString(ballerinaTomlPath);
        String dependenciesTomlContent = Files.readString(dependenciesTomlPath);

        TomlDocument ballerinaToml = TomlDocument.from(ProjectConstants.BALLERINA_TOML, ballerinaTomlContent);
        TomlDocument dependenciesToml = TomlDocument.from(ProjectConstants.DEPENDENCIES_TOML, dependenciesTomlContent);

        return ManifestBuilder.from(ballerinaToml, dependenciesToml, ballerinaTomlPath.getParent()).packageManifest();
    }
}
