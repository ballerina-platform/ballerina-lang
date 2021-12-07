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
import io.ballerina.projects.providers.SemverDataProvider;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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

        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("valid-ballerina.toml"));
        Assert.assertFalse(packageManifest.diagnostics().hasErrors());

        PackageDescriptor descriptor = packageManifest.descriptor();
        Assert.assertEquals(descriptor.name().value(), "winery");
        Assert.assertEquals(descriptor.org().value(), "foo");
        Assert.assertEquals(descriptor.version().value().toString(), "0.1.0");

        PackageManifest.Platform platform = packageManifest.platform("java11");
        List<Map<String, Object>> platformDependencies = platform.dependencies();
        Assert.assertEquals(platformDependencies.size(), 2);
        for (Map<String, Object> library : platformDependencies) {
            Assert.assertTrue(library.get("path").equals("../dummy-jars/toml4j.txt")
                                      || library.get("path").equals("../dummy-jars/swagger.txt"));
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
        Assert.assertEquals(packageManifest.ballerinaVersion(), "slbeta2");
        Assert.assertEquals(packageManifest.visibility(), "private");

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
        Assert.assertEquals(descriptor.version().value().toString(), "0.0.0");
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
        Assert.assertEquals(platformDependencies.size(), 2);
        for (Map<String, Object> library : platformDependencies) {
            if (library.get("path").equals("../dummy-jars/swagger.txt")) {
                Assert.assertEquals(library.get("scope"), "testOnly");
                Assert.assertEquals(library.get("artifactId"), "swagger");
                Assert.assertEquals(library.get("version"), "0.7.2");
                Assert.assertEquals(library.get("groupId"), "swagger.io");
            } else {
                Assert.assertNull(library.get("scope"));
                Assert.assertEquals(library.get("path"), "../dummy-jars/toml4j.txt");
                Assert.assertEquals(library.get("artifactId"), "toml4j");
                Assert.assertEquals(library.get("version"), "0.7.2");
                Assert.assertEquals(library.get("groupId"), "com.moandjiezana.toml");
            }
        }
    }

    // Negative tests

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

    @Test(enabled = false)
    public void testBallerinaTomlWithEmptyPackage() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("empty-package.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 3);

        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();
        Assert.assertEquals(iterator.next().message(), "'name' under [package] is missing");
        Assert.assertEquals(iterator.next().message(), "'org' under [package] is missing");
        Assert.assertEquals(iterator.next().message(), "'version' under [package] is missing");
    }

    @Test(description = "Package should be given as [package], Here checking error when it given as [[package]]")
    public void testBallerinaTomlWithPackageGivenAsTableArray() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("package-as-table-array.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 1);

        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();
        Assert.assertEquals(iterator.next().message(),
                "incompatible type for key 'package': expected 'OBJECT', found 'ARRAY'");
    }

    @Test(description = "Build options should be given as [build-options], " +
            "Here checking error when it given as [build-options]")
    public void testBallerinaTomlWithBuildOptionsGivenAsTableArray() throws IOException {
        PackageManifest packageManifest =
                getPackageManifest(BAL_TOML_REPO.resolve("build-options-as-table-array.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 1);

        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();
        Assert.assertEquals(iterator.next().message(),
                "incompatible type for key 'build-options': expected 'OBJECT', found 'ARRAY'");
    }

    @Test(description = "Platform libs should be given as [[platform.java11.dependency]], " +
            "Here checking error when it given as [platform.java11.dependency]")
    public void testBallerinaTomlWithPlatformLibsGivenAsTable() throws IOException {
        PackageManifest packageManifest =
                getPackageManifest(BAL_TOML_REPO.resolve("platform-libs-as-table.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 2);

        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();
        Assert.assertEquals(iterator.next().message(),
                "incompatible type for key 'dependency': expected 'ARRAY', found 'OBJECT'");
        Assert.assertEquals(iterator.next().message(),
                "existing node 'dependency'");
    }

    @Test(description = "Local dependencies should be given as [[dependency]], " +
            "Here checking error when it given as [dependency]")
    public void testBallerinaTomlWithLocalDependenciesGivenAsTable() throws IOException {
        PackageManifest packageManifest =
                getPackageManifest(BAL_TOML_REPO.resolve("local-dependencies-as-table-array.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 2);

        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();
        Assert.assertEquals(iterator.next().message(),
                "incompatible type for key 'dependency': expected 'ARRAY', found 'OBJECT'");
        Assert.assertEquals(iterator.next().message(),
                "existing node 'dependency'");
    }

    @Test(enabled = false)
    public void testBallerinaTomlWithoutOrgNameVersion() throws IOException {
        PackageManifest packageManifest = getPackageManifest(
                BAL_TOML_REPO.resolve("platform-without-org-name-version.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 3);

        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();
        Assert.assertEquals(iterator.next().message(), "'name' under [package] is missing");
        Assert.assertEquals(iterator.next().message(), "'org' under [package] is missing");
        Assert.assertEquals(iterator.next().message(), "'version' under [package] is missing");
    }

    @Test
    public void testBallerinaTomlWithInvalidOrgNameVersion() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("invalid-org-name-version.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 3);

        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();

        Diagnostic firstDiagnostic = iterator.next();
        Assert.assertEquals(firstDiagnostic.message(), "invalid 'name' under [package]: "
                + "'name' can only contain alphanumerics, underscores and periods");
        Assert.assertEquals(firstDiagnostic.location().lineRange().toString(), "(1:7,1:23)");

        String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        if (os.contains("win")) {
            // text range including minutiae, if we get a node that includes newline minutiae,
            // its text range will be different. i.e windows will have an extra 1 length due to \r\n.
            Assert.assertEquals(firstDiagnostic.location().textRange().toString(), "(18,34)");
        } else {
            Assert.assertEquals(firstDiagnostic.location().textRange().toString(), "(17,33)");
        }

        Assert.assertEquals(iterator.next().message(), "invalid 'org' under [package]: "
                + "'org' can only contain alphanumerics and underscores");
        Assert.assertEquals(iterator.next().message(), "invalid 'version' under [package]: "
                + "'version' should be compatible with semver");
    }

    @Test
    public void testBallerinaTomlWithInvalidLengthOrgName() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("invalid-length-org-name.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 3);

        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();

        Diagnostic firstDiagnostic = iterator.next();
        Assert.assertEquals(firstDiagnostic.message(), "invalid 'name' under [package]: "
                + "maximum length of 'name' is 256 characters");
        Assert.assertEquals(firstDiagnostic.location().lineRange().toString(), "(1:7,1:303)");

        String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        if (os.contains("win")) {
            // text range including minutiae, if we get a node that includes newline minutiae,
            // its text range will be different. i.e windows will have an extra 1 length due to \r\n.
            Assert.assertEquals(firstDiagnostic.location().textRange().toString(), "(18,314)");
        } else {
            Assert.assertEquals(firstDiagnostic.location().textRange().toString(), "(17,313)");
        }

        Assert.assertEquals(iterator.next().message(), "invalid 'org' under [package]: "
                + "maximum length of 'org' is 256 characters");
    }

    @Test
    public void testPackageHasOrgAndNameStartingWithUnderscore() throws IOException {
        PackageManifest packageManifest = getPackageManifest(
                BAL_TOML_REPO.resolve("underscores-starting-org-name.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 2);

        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();

        Diagnostic firstDiagnostic = iterator.next();
        Assert.assertEquals(firstDiagnostic.message(),
                "invalid 'name' under [package]: 'name' cannot have initial underscore characters");
        Assert.assertEquals(firstDiagnostic.location().lineRange().toString(), "(1:7,1:20)");
        Assert.assertEquals(iterator.next().message(),
                "invalid 'org' under [package]: 'org' cannot have initial underscore characters");
    }

    @Test
    public void testPackageHasOrgAndNameTrailingWithUnderscore() throws IOException {
        PackageManifest packageManifest = getPackageManifest(
                BAL_TOML_REPO.resolve("underscores-trailing-org-name.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 2);

        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();

        Diagnostic firstDiagnostic = iterator.next();
        Assert.assertEquals(firstDiagnostic.message(),
                "invalid 'name' under [package]: 'name' cannot have trailing underscore characters");
        Assert.assertEquals(firstDiagnostic.location().lineRange().toString(), "(1:7,1:20)");
        Assert.assertEquals(iterator.next().message(),
                "invalid 'org' under [package]: 'org' cannot have trailing underscore characters");
    }

    @Test
    public void testPackageHasOrgAndNameWithConsecutiveUnderscores() throws IOException {
        PackageManifest packageManifest = getPackageManifest(
                BAL_TOML_REPO.resolve("underscores-consecutive-org-name.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 2);

        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();

        Diagnostic firstDiagnostic = iterator.next();
        Assert.assertEquals(firstDiagnostic.message(),
                "invalid 'name' under [package]: 'name' cannot have consecutive underscore characters");
        Assert.assertEquals(firstDiagnostic.location().lineRange().toString(), "(1:7,1:20)");
        Assert.assertEquals(iterator.next().message(),
                "invalid 'org' under [package]: 'org' cannot have consecutive underscore characters");
    }

    @Test
    public void testPackageHasOrgAndNameWithAllUnderscoreErrors() throws IOException {
        PackageManifest packageManifest = getPackageManifest(
                BAL_TOML_REPO.resolve("underscores-all-errors-org-name.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 6);

        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();

        Diagnostic firstDiagnostic = iterator.next();
        Assert.assertEquals(firstDiagnostic.message(),
                "invalid 'name' under [package]: 'name' cannot have consecutive underscore characters");
        Assert.assertEquals(firstDiagnostic.location().lineRange().toString(), "(1:7,1:22)");
        Assert.assertEquals(iterator.next().message(),
                "invalid 'name' under [package]: 'name' cannot have initial underscore characters");
        Assert.assertEquals(iterator.next().message(),
                "invalid 'name' under [package]: 'name' cannot have trailing underscore characters");
        Assert.assertEquals(iterator.next().message(),
                "invalid 'org' under [package]: 'org' cannot have consecutive underscore characters");
        Assert.assertEquals(iterator.next().message(),
                "invalid 'org' under [package]: 'org' cannot have initial underscore characters");
        Assert.assertEquals(iterator.next().message(),
                "invalid 'org' under [package]: 'org' cannot have trailing underscore characters");
    }

    @Test
    public void testLangPackageHasNameWithConsecutiveUnderscores() throws IOException {
        PackageManifest packageManifest = getPackageManifest(
                BAL_TOML_REPO.resolve("underscores-lang-package.toml"));
        Assert.assertFalse(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().errors().size(), 0);
    }

    @Test
    public void testBallerinaTomlWithPlatformDependencyAsInlineTable() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("inline-table.toml"));
        DiagnosticResult diagnostics = packageManifest.diagnostics();
        Assert.assertFalse(diagnostics.hasErrors());
    }

    @Test
    public void testBallerinaTomlWithMissingDependencyInPlatform() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("platform-missing-dependency.toml"));
        DiagnosticResult diagnostics = packageManifest.diagnostics();
        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertEquals(diagnostics.errors().iterator().next().message(),
                            "incompatible type for key 'java11': expected 'OBJECT', found 'ARRAY'");
    }

    @Test(description = "Test Ballerina.toml having invalid types for entries in package and build options")
    public void testBallerinaTomlWithInvalidEntries() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("invalid-entries.toml"));
        DiagnosticResult diagnostics = packageManifest.diagnostics();
        Assert.assertTrue(diagnostics.hasErrors());
        Assert.assertEquals(diagnostics.errors().size(), 6);

        Iterator<Diagnostic> iterator = diagnostics.errors().iterator();
        Assert.assertEquals(iterator.next().message(),
                "incompatible type for key 'license': expected 'ARRAY', found 'STRING'");
        Assert.assertEquals(iterator.next().message(),
                "incompatible type for key 'repository': expected 'STRING', found 'BOOLEAN'");
        Assert.assertEquals(iterator.next().message(),
                "invalid 'visibility' under [package]: 'visibility' can only have value 'private'");
        Assert.assertEquals(iterator.next().message(),
                "could not locate dependency path 'path/to/swagger.txt'");
        Assert.assertEquals(iterator.next().message(),
                "incompatible type for key 'observabilityIncluded': expected 'BOOLEAN', found 'ARRAY'");
        Assert.assertEquals(iterator.next().message(),
                "incompatible type for key 'offline': expected 'BOOLEAN', found 'STRING'");
    }

    @Test
    public void testBallerinaTomlWithAdditionalProperties() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("additional-props-ballerina.toml"));
        DiagnosticResult diagnostics = packageManifest.diagnostics();
        Assert.assertFalse(diagnostics.hasErrors());
        Assert.assertEquals(diagnostics.errors().size(), 0);
    }

    @Test(dataProvider = "semverVersions", dataProviderClass = SemverDataProvider.class)
    public void testSemverVersions(String version) throws IOException {
        String tomlContent = Files.readString(BAL_TOML_REPO.resolve("simple-ballerina.toml"));
        String replacedContent = tomlContent.replace("1.0.0", version);
        TomlDocument ballerinaToml = TomlDocument.from(ProjectConstants.BALLERINA_TOML, replacedContent);
        PackageManifest manifest = ManifestBuilder.from(ballerinaToml, null, BAL_TOML_REPO).packageManifest();
        Assert.assertFalse(manifest.diagnostics().hasErrors());
    }

    @Test(dataProvider = "invalidSemverVersions", dataProviderClass = SemverDataProvider.class)
    public void testInvalidSemverVersions(String version) throws IOException {
        String tomlContent = Files.readString(BAL_TOML_REPO.resolve("simple-ballerina.toml"));
        String replacedContent = tomlContent.replace("1.0.0", version);
        TomlDocument ballerinaToml = TomlDocument.from(ProjectConstants.BALLERINA_TOML, replacedContent);
        PackageManifest manifest = ManifestBuilder.from(ballerinaToml, null, BAL_TOML_REPO).packageManifest();
        Assert.assertTrue(manifest.diagnostics().hasErrors());
        Assert.assertEquals(manifest.diagnostics().errors().iterator().next().message(),
                            "invalid 'version' under [package]: 'version' should be compatible with semver");
    }

    @Test(description = "Test other entries added by the user")
    public void testOtherEntries() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("other-entries.toml"));
        Assert.assertFalse(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.descriptor().org().value(), "foo");
        Assert.assertEquals(packageManifest.descriptor().name().value(), "winery");
        Assert.assertEquals(packageManifest.descriptor().version().toString(), "1.0.0");

        // package should not be able to access using `getValue` method
        Assert.assertNull(packageManifest.getValue("package"));

        // other entry field
        Assert.assertEquals(packageManifest.getValue("firstField"), "yee");

        // other entry table
        Assert.assertEquals(((Map<String, Object>) packageManifest.getValue("userTable")).get("name"), "bar");
        Long frequency = (Long) (((Map<String, Object>) packageManifest.getValue("userTable")).get("frequency"));
        Assert.assertEquals(frequency.longValue(), 225);

        // other entry table array
        List<Map<String, Object>> userContacts = (ArrayList) packageManifest.getValue("userContact");
        Assert.assertEquals(userContacts.size(), 2);
        Map<String, Object> firstContact = userContacts.get(0);
        Assert.assertEquals(firstContact.get("name"), "hevayo");
        Assert.assertEquals(firstContact.get("phone"), "0123456789");
    }

    @Test
    public void testLocalDependencies() throws IOException {
        PackageManifest packageManifest = getPackageManifest(BAL_TOML_REPO.resolve("local-dependencies.toml"));
        Assert.assertFalse(packageManifest.diagnostics().hasErrors());
        List<PackageManifest.Dependency> dependencies = packageManifest.dependencies();
        Assert.assertEquals(dependencies.size(), 2);

        PackageManifest.Dependency firstLocalDep = dependencies.get(0);
        Assert.assertEquals(firstLocalDep.org().value(), "abc");
        Assert.assertEquals(firstLocalDep.name().value(), "test");
        Assert.assertEquals(firstLocalDep.version().value().toString(), "1.0.0");
        Assert.assertEquals(firstLocalDep.repository(), "local");

        PackageManifest.Dependency secLocalDep = dependencies.get(1);
        Assert.assertEquals(secLocalDep.org().value(), "xyz");
        Assert.assertEquals(secLocalDep.name().value(), "sample");
        Assert.assertEquals(secLocalDep.version().value().toString(), "2.0.0");
        Assert.assertEquals(secLocalDep.repository(), "local");
    }

    @Test
    public void testLocalDependenciesWithInvalidOrgAndName() throws IOException {
        PackageManifest packageManifest = getPackageManifest(
                BAL_TOML_REPO.resolve("local-dependencies-with-invalid-org-name.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().diagnostics().size(), 6);
        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();
        Assert.assertEquals(iterator.next().message(), "invalid 'org' under [[dependency]]: "
                + "'org' can only contain alphanumerics and underscores");
        Assert.assertEquals(iterator.next().message(), "invalid 'name' under [[dependency]]: "
                + "'name' can only contain alphanumerics, underscores and periods");
        Assert.assertEquals(iterator.next().message(), "invalid 'org' under [[dependency]]: "
                + "'org' can only contain alphanumerics and underscores");
        Assert.assertEquals(iterator.next().message(), "invalid 'name' under [[dependency]]: "
                + "'name' can only contain alphanumerics, underscores and periods");
        Assert.assertEquals(iterator.next().message(), "invalid 'org' under [[dependency]]: "
                + "maximum length of 'org' is 256 characters");
        Assert.assertEquals(iterator.next().message(), "invalid 'name' under [[dependency]]: "
                + "maximum length of 'name' is 256 characters");
    }

    @Test
    public void testInvalidIconPath() throws IOException {
        PackageManifest packageManifest = getPackageManifest(
                BAL_TOML_REPO.resolve("invalid-icon-path.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().diagnostics().size(), 1);
        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();
        Assert.assertEquals(iterator.next().message(), "could not locate icon path '../sample.png'");
    }

    @Test
    public void testInvalidIconType() throws IOException {
        PackageManifest packageManifest = getPackageManifest(
                BAL_TOML_REPO.resolve("invalid-icon-type.toml"));
        Assert.assertTrue(packageManifest.diagnostics().hasErrors());
        Assert.assertEquals(packageManifest.diagnostics().diagnostics().size(), 1);
        Iterator<Diagnostic> iterator = packageManifest.diagnostics().errors().iterator();
        Assert.assertEquals(iterator.next().message(),
                "invalid 'icon' under [package]: 'icon' can only have 'png' images");
    }

    static PackageManifest getPackageManifest(Path tomlPath) throws IOException {
        String tomlContent = Files.readString(tomlPath);
        TomlDocument ballerinaToml = TomlDocument.from(ProjectConstants.BALLERINA_TOML, tomlContent);
        return ManifestBuilder.from(ballerinaToml, null, tomlPath.getParent()).packageManifest();
    }
}
