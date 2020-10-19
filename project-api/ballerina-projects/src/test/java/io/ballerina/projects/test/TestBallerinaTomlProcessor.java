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

import com.google.gson.internal.LinkedTreeMap;
import io.ballerina.projects.model.BallerinaToml;
import io.ballerina.projects.model.BallerinaTomlProcessor;
import io.ballerina.projects.model.Package;
import org.ballerinalang.toml.exceptions.TomlException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains cases to test the ballerina toml processor.
 *
 * @since 2.0.0
 */
public class TestBallerinaTomlProcessor {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    @Test(description = "Test parse `ballerina.toml` file in to `BallerinaToml` object")
    public void testParse() throws IOException, TomlException {
        Path ballerinaTomlPath = RESOURCE_DIRECTORY.resolve("ballerinatoml").resolve("Ballerina.toml");
        BallerinaToml ballerinaToml = BallerinaTomlProcessor.parse(ballerinaTomlPath);

        Assert.assertEquals(ballerinaToml.getPackage().getOrg(), "foo");
        Assert.assertEquals(ballerinaToml.getPackage().getName(), "winery");
        Assert.assertEquals(ballerinaToml.getPackage().getVersion(), "0.1.0");

        Assert.assertEquals(ballerinaToml.getDependencies().get("wso2/twitter"), "2.3.4");
        Assert.assertEquals(((LinkedTreeMap) ballerinaToml.getDependencies().get("wso2/github")).get("path"),
                "path/to/github.balo");
        Assert.assertEquals(((LinkedTreeMap) ballerinaToml.getDependencies().get("wso2/github")).get("version"),
                "1.2.3");

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
