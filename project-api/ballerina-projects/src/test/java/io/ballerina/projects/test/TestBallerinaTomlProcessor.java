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

package io.ballerina.projects.test;

import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.directory.BallerinaTomlProcessor;
import io.ballerina.projects.model.BallerinaToml;
import io.ballerina.projects.model.Package;
import org.ballerinalang.toml.exceptions.TomlException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Contains cases to test the ballerina toml processor.
 *
 * @since 2.0.0
 */
public class TestBallerinaTomlProcessor {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    @Test(description = "Test parse `ballerina.toml` file in to `PackageDescriptor` object")
    public void testParse() throws IOException, TomlException {
        Path ballerinaTomlPath = RESOURCE_DIRECTORY.resolve("ballerinatoml").resolve("Ballerina.toml");
        PackageDescriptor packageDescriptor = BallerinaTomlProcessor.parseAsPackageDescriptor(ballerinaTomlPath);

        Assert.assertEquals(packageDescriptor.org().value(), "foo");
        Assert.assertEquals(packageDescriptor.name().value(), "winery");
        Assert.assertEquals(packageDescriptor.version().toString(), "0.1.0");

        List<PackageDescriptor.Dependency> dependencyList = packageDescriptor.dependencies();
        Assert.assertEquals(dependencyList.size(), 2);
        Assert.assertEquals(dependencyList.get(0).org().value(), "wso2");
        Assert.assertEquals(dependencyList.get(0).name().value(), "twitter");
        Assert.assertEquals(dependencyList.get(0).version().toString(), "2.3.4");

        Assert.assertEquals(dependencyList.get(1).org().value(), "wso2");
        Assert.assertEquals(dependencyList.get(1).name().value(), "github");
        Assert.assertEquals(dependencyList.get(1).version().toString(), "1.2.3");
    }

    @Test(description = "Test validate ballerina toml package section which contains build options")
    public void testValidateBallerinaTomlWithBuildOptions() throws IOException, TomlException {
        Path ballerinaTomlPath = RESOURCE_DIRECTORY.resolve("ballerinatomlWithBuildOptions").resolve("Ballerina.toml");
        BallerinaToml ballerinaToml = BallerinaTomlProcessor.parse(ballerinaTomlPath);
        Assert.assertNotNull(ballerinaToml.getBuildOptions());
        Assert.assertEquals(ballerinaToml.getBuildOptions().getB7aConfigFile(), "/tmp/ballerina.conf");
        Assert.assertTrue(ballerinaToml.getBuildOptions().isExperimental());
        Assert.assertFalse(ballerinaToml.getBuildOptions().isOffline());
    }

    @Test(description = "Test validate ballerina toml package section")
    public void testValidateBallerinaTomlPackage() {
        BallerinaToml ballerinaToml = new BallerinaToml();
        Package pkg = new Package();
        pkg.setOrg("foo");
        pkg.setName("winery");
        pkg.setVersion("0.1.0");
        ballerinaToml.setPkg(pkg);

        try {
            BallerinaTomlProcessor.validateBallerinaTomlPackage(ballerinaToml);
        } catch (TomlException e) {
            Assert.fail();
        }
    }

    @Test(description = "Test validate ballerina toml package section which contains empty org")
    public void testValidateBallerinaTomlPackageWithEmptyOrg() {
        BallerinaToml ballerinaToml = new BallerinaToml();
        Package pkg = new Package();
        pkg.setName("winery");
        pkg.setVersion("0.1.0");
        ballerinaToml.setPkg(pkg);

        try {
            BallerinaTomlProcessor.validateBallerinaTomlPackage(ballerinaToml);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof TomlException);
            Assert.assertEquals(e.getMessage(), "invalid Ballerina.toml file: cannot find 'org' under [package]");
        }
    }

    @Test(description = "Test validate ballerina toml package section which contains empty name")
    public void testValidateBallerinaTomlPackageWithEmptyName() {
        BallerinaToml ballerinaToml = new BallerinaToml();
        Package pkg = new Package();
        pkg.setOrg("foo");
        pkg.setVersion("0.1.0");
        ballerinaToml.setPkg(pkg);

        try {
            BallerinaTomlProcessor.validateBallerinaTomlPackage(ballerinaToml);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof TomlException);
            Assert.assertEquals(e.getMessage(), "invalid Ballerina.toml file: cannot find 'name' under [package]");
        }
    }

    @Test(description = "Test validate ballerina toml package section which contains empty version")
    public void testValidateBallerinaTomlPackageWithEmptyVersion() {
        BallerinaToml ballerinaToml = new BallerinaToml();
        Package pkg = new Package();
        pkg.setOrg("foo");
        pkg.setName("winery");
        ballerinaToml.setPkg(pkg);

        try {
            BallerinaTomlProcessor.validateBallerinaTomlPackage(ballerinaToml);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof TomlException);
            Assert.assertEquals(e.getMessage(), "invalid Ballerina.toml file: cannot find 'version' under [package]");
        }
    }

    @Test(description = "Test validate ballerina toml package section which contains invalid org")
    public void testValidateBallerinaTomlPackageWithInvalidOrg() {
        BallerinaToml ballerinaToml = new BallerinaToml();
        Package pkg = new Package();
        pkg.setOrg("foo-org");
        pkg.setName("winery");
        pkg.setVersion("0.1.0");
        ballerinaToml.setPkg(pkg);

        try {
            BallerinaTomlProcessor.validateBallerinaTomlPackage(ballerinaToml);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof TomlException);
            Assert.assertEquals(e.getMessage(),
                    "invalid Ballerina.toml file: Invalid 'org' under [package]: 'foo-org' :\n"
                            + "'org' can only contain alphanumerics, underscores and periods and the maximum length "
                            + "is 256 characters");
        }
    }

    @Test(description = "Test validate ballerina toml package section which contains invalid version")
    public void testValidateBallerinaTomlPackageWithInvalidVersion() {
        BallerinaToml ballerinaToml = new BallerinaToml();
        Package pkg = new Package();
        pkg.setOrg("foo");
        pkg.setName("winery");
        pkg.setVersion("v1.0.0");
        ballerinaToml.setPkg(pkg);

        try {
            BallerinaTomlProcessor.validateBallerinaTomlPackage(ballerinaToml);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof TomlException);
            Assert.assertEquals(e.getMessage(),
                    "invalid Ballerina.toml file: 'version' under [package] is not semver");
        }
    }
}
