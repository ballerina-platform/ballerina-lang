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

package io.ballerina.projects.test.writers;

import com.google.gson.Gson;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.Package;
import io.ballerina.projects.directory.PackageLoader;
import io.ballerina.projects.model.BaloJson;
import io.ballerina.projects.model.PackageJson;
import io.ballerina.projects.writers.BaloWriter;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains cases to test the balo writer.
 *
 * @since 2.0.0
 */
public class TestBaloWriter {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");
    private static final Path BALO_PATH = RESOURCE_DIRECTORY.resolve("tmpBaloDir");


    @BeforeClass
    public void setUp() throws IOException {
        Files.createDirectory(Paths.get(String.valueOf(BALO_PATH)));
    }

    @Test
    public void testBaloWriter() throws IOException {
        Gson gson = new Gson();
        Path projectPath = RESOURCE_DIRECTORY.resolve("balowriter").resolve("projectOne");
        PackageConfig packageConfig = PackageLoader.loadPackage(String.valueOf(projectPath), false);
        Package pkg = Package.from(packageConfig);

        // invoke write balo method
        Path balo = BaloWriter.write(pkg, BALO_PATH);

        // balo name
        Assert.assertEquals(new File(String.valueOf(balo)).getName(), "foo-winery-java8-0.1.0.balo");

        // unzip balo
        TestUtils.unzip(String.valueOf(balo), String.valueOf(BALO_PATH));

        // balo.json
        Path baloJsonPath = BALO_PATH.resolve("balo.json");
        Assert.assertTrue(baloJsonPath.toFile().exists());
        BaloJson baloJson = gson.fromJson(new FileReader(String.valueOf(baloJsonPath)), BaloJson.class);
        Assert.assertEquals(baloJson.getBalo_version(), "2.0.0");
        Assert.assertEquals(baloJson.getBuilt_by(), "WSO2");

        // package.json
        Path packageJsonPath = BALO_PATH.resolve("package.json");
        Assert.assertTrue(packageJsonPath.toFile().exists());
        PackageJson packageJson = gson.fromJson(new FileReader(String.valueOf(packageJsonPath)), PackageJson.class);
        Assert.assertEquals(packageJson.getOrganization(), "foo");
        Assert.assertEquals(packageJson.getName(), "winery");
        Assert.assertEquals(packageJson.getVersion(), "0.1.0");

        // docs
        Path packageMdPath = BALO_PATH.resolve("docs").resolve("Package.md");
        Assert.assertTrue(packageMdPath.toFile().exists());
        Path defaultModuleMdPath = BALO_PATH.resolve("docs").resolve("modules").resolve("winery").resolve("Module.md");
        Assert.assertTrue(defaultModuleMdPath.toFile().exists());
        Path servicesModuleMdPath = BALO_PATH.resolve("docs").resolve("modules").resolve("winery.services").resolve("Module.md");
        Assert.assertTrue(servicesModuleMdPath.toFile().exists());
        Path storageModuleMdPath = BALO_PATH.resolve("docs").resolve("modules").resolve("winery.storage").resolve("Module.md");
        Assert.assertTrue(storageModuleMdPath.toFile().exists());

        // module sources
        // default module
        Path defaultModuleSrcPath = BALO_PATH.resolve("modules").resolve("winery");
        Assert.assertTrue(defaultModuleSrcPath.toFile().exists());
        Assert.assertTrue(defaultModuleSrcPath.resolve(Paths.get("main.bal")).toFile().exists());
        Assert.assertTrue(defaultModuleSrcPath.resolve(Paths.get("utils.bal")).toFile().exists());
        Assert.assertTrue(defaultModuleSrcPath.resolve(Paths.get("resources")).toFile().exists());
        Assert.assertTrue(defaultModuleSrcPath.resolve(Paths.get("resources", "main.json")).toFile().exists());
        Assert.assertFalse(defaultModuleSrcPath.resolve("modules").toFile().exists());
        Assert.assertFalse(defaultModuleSrcPath.resolve("tests").toFile().exists());
        Assert.assertFalse(defaultModuleSrcPath.resolve("targets").toFile().exists());
        Assert.assertFalse(defaultModuleSrcPath.resolve("Package.md").toFile().exists());
        Assert.assertFalse(defaultModuleSrcPath.resolve("Ballerina.toml").toFile().exists());
        // other modules
        // storage module
        Path storageModuleSrcPath = BALO_PATH.resolve("modules").resolve("winery.storage");
        Assert.assertTrue(storageModuleSrcPath.resolve("db.bal").toFile().exists());
        Assert.assertTrue(storageModuleSrcPath.resolve("resources").toFile().exists());
        Assert.assertTrue(storageModuleSrcPath.resolve("resources").resolve("db.json").toFile().exists());
        Assert.assertFalse(storageModuleSrcPath.resolve("tests").toFile().exists());
        Assert.assertFalse(storageModuleSrcPath.resolve("Module.md").toFile().exists());

        // libs
        Path libPath = BALO_PATH.resolve("lib");
        Assert.assertTrue(libPath.toFile().exists());
        Assert.assertTrue(libPath.resolve("ballerina-io-1.0.0-java.txt").toFile().exists());
    }

    // TODO minimal test : bal file + Ballerina.toml
    @Test
    public void testBaloWriterWithMinimalBalProject() throws IOException {
        Gson gson = new Gson();
        Path projectPath = RESOURCE_DIRECTORY.resolve("balowriter").resolve("projectTwo");
        PackageConfig packageConfig = PackageLoader.loadPackage(String.valueOf(projectPath), false);
        Package pkg = Package.from(packageConfig);

        // invoke write balo method
        Path balo = BaloWriter.write(pkg, BALO_PATH);

        // balo name
        Assert.assertEquals(new File(String.valueOf(balo)).getName(), "bar-winery-any-0.1.0.balo");

        // unzip balo
        TestUtils.unzip(String.valueOf(balo), String.valueOf(BALO_PATH));

        // balo.json
        Path baloJsonPath = BALO_PATH.resolve("balo.json");
        Assert.assertTrue(baloJsonPath.toFile().exists());
        BaloJson baloJson = gson.fromJson(new FileReader(String.valueOf(baloJsonPath)), BaloJson.class);
        Assert.assertEquals(baloJson.getBalo_version(), "2.0.0");
        Assert.assertEquals(baloJson.getBuilt_by(), "WSO2");

        // package.json
        Path packageJsonPath = BALO_PATH.resolve("package.json");
        Assert.assertTrue(packageJsonPath.toFile().exists());
        PackageJson packageJson = gson.fromJson(new FileReader(String.valueOf(packageJsonPath)), PackageJson.class);
        Assert.assertEquals(packageJson.getOrganization(), "bar");
        Assert.assertEquals(packageJson.getName(), "winery");
        Assert.assertEquals(packageJson.getVersion(), "0.1.0");
        Assert.assertEquals(packageJson.getBallerinaVersion(), "unknown");
    }


    // TODO without access : mock isFileWritable()
    @Test
    public void testBaleWriterAccessDenied() {

    }

    @AfterMethod
    public void cleanUp() {
//        cleanBaloPath();
    }

    private void cleanBaloPath() {
        TestUtils.deleteDirectory(new File(String.valueOf(BALO_PATH)));
    }
}
