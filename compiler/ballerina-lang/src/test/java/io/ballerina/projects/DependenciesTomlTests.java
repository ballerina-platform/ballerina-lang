/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

/**
 * Test DependenciesToml.
 */
public class DependenciesTomlTests {

    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources");
    private static final Path BAL_TOML_REPO = RESOURCE_DIRECTORY.resolve("ballerina-toml");
    private static final Path DEPENDENCIES_TOML_REPO = RESOURCE_DIRECTORY.resolve("dependencies-toml");

    @Test
    public void testValidDependenciesToml() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("valid-ballerina.toml"),
                                                             BAL_TOML_REPO.resolve("dependencies-valid.toml"));
        Assert.assertFalse(packageManifest.diagnostics().hasErrors());

        List<PackageManifest.Dependency> dependencies = packageManifest.dependencies();
        Assert.assertEquals(dependencies.size(), 2);
        for (PackageManifest.Dependency dependency : dependencies) {
            Assert.assertEquals(dependency.org().value(), "wso2");
            Assert.assertTrue(dependency.name().value().equals("twitter")
                                      || dependency.name().value().equals("github"));
            Assert.assertTrue(dependency.version().value().toString().equals("2.3.4")
                                      || dependency.version().value().toString().equals("1.2.3"));
        }
    }

    /**
     * Invalid Dependencies.toml file with dependencies missing org.
     * <p>
     * [[dependency]]
     * name = "foo1"
     * version = "0.1.0"
     */
    @Test
    public void testInvalidDependenciesTomlWithoutOrg() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("valid-ballerina.toml"),
                                                             DEPENDENCIES_TOML_REPO.resolve("dependency-wo-org.toml"));
        DiagnosticResult diagnostics = packageManifest.diagnostics();
        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertEquals(diagnostics.errors().size(), 2);
        Iterator<Diagnostic> iterator = diagnostics.errors().iterator();
        Diagnostic firstDiagnostic = iterator.next();
        Assert.assertEquals(firstDiagnostic.message(), "'org' under [[dependency]] is missing");
        Assert.assertEquals(firstDiagnostic.location().lineRange().toString(), "(0:0,2:17)");
        Assert.assertEquals(iterator.next().message(), "'org' under [[dependency]] is missing");
    }

    /**
     * Invalid Dependencies.toml file with dependencies missing org value.
     * <p>
     * [[dependency]]
     * org =
     * name = "foo1"
     * version = "0.1.0"
     */
    @Test
    public void testInvalidDependenciesTomlWithoutOrgValue() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("valid-ballerina.toml"),
                                                    DEPENDENCIES_TOML_REPO.resolve("dependency-wo-org-value.toml"));
        DiagnosticResult diagnostics = packageManifest.diagnostics();
        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertEquals(diagnostics.errors().size(), 1);
        Iterator<Diagnostic> iterator = diagnostics.errors().iterator();
        Diagnostic firstDiagnostic = iterator.next();
        Assert.assertEquals(firstDiagnostic.location().lineRange().toString(), "(2:0,2:0)");
        Assert.assertEquals(firstDiagnostic.message(), "missing value");
    }

    /**
     * Invalid Dependencies.toml file with invalid org, name and version.
     * <p>
     * [[dependency]]
     * org = "foo-1"
     * name = "proj-1"
     * version = "221"
     */
    @Test
    public void testDependenciesTomlWithInvalidOrgNameVersion() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("valid-ballerina.toml"),
                                                     DEPENDENCIES_TOML_REPO.resolve("invalid-org-name-value.toml"));
        DiagnosticResult diagnostics = packageManifest.diagnostics();
        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertEquals(diagnostics.errors().size(), 3);
        Iterator<Diagnostic> iterator = diagnostics.errors().iterator();
        Diagnostic firstDiagnostic = iterator.next();
        Assert.assertEquals(firstDiagnostic.message(), "invalid 'org' under [[dependency]]: "
                + "'org' can only contain alphanumerics, underscores and the maximum length is 256 characters");
        Assert.assertEquals(firstDiagnostic.location().lineRange().toString(), "(1:6,1:13)");
        Assert.assertEquals(iterator.next().message(), "invalid 'name' under [[dependency]]: 'name' can only contain "
                + "alphanumerics, underscores and periods and the maximum length is 256 characters");
        Assert.assertEquals(iterator.next().message(), "invalid 'version' under [[dependency]]: "
                + "'version' should be compatible with semver");
    }

    private PackageManifest getPackageManifest(Path ballerinaTomlPath, Path dependenciesTomlPath) throws IOException {
        String ballerinaTomlContent = Files.readString(ballerinaTomlPath, Charset.defaultCharset());
        Path absLibPath = Paths.get(System.getProperty("user.dir")).resolve("src/test/resources/dummy-jars/toml4j.txt");
        ballerinaTomlContent = ballerinaTomlContent.replace("<ABS_LIB_PATH>", absLibPath.toString());
        String dependenciesTomlContent = Files.readString(dependenciesTomlPath);

        TomlDocument ballerinaToml = TomlDocument.from(ProjectConstants.BALLERINA_TOML, ballerinaTomlContent);
        TomlDocument dependenciesToml = TomlDocument.from(ProjectConstants.DEPENDENCIES_TOML, dependenciesTomlContent);

        return ManifestBuilder
                .from(ballerinaToml, dependenciesToml, null, ballerinaTomlPath.getParent())
                .packageManifest();
    }
}
