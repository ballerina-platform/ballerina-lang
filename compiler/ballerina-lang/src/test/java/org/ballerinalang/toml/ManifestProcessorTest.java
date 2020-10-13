/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.toml;


import org.ballerinalang.toml.exceptions.TomlException;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Test class to populate Manifest object by reading the toml.
 */
public class ManifestProcessorTest {
    private static final String OS = System.getProperty("os.name").toLowerCase(Locale.getDefault());
    private String validProjectBlock = "[project]\n" +
                                       "org-name = \"foo\"\n" +
                                       "version = \"1.0.0\"\n";
    
    @Test(description = "Empty Ballerina.toml file", expectedExceptions = TomlException.class,
          expectedExceptionsMessageRegExp = ".*invalid Ballerina.toml file: organization name and the version of the " +
                                            "project is missing. example: \n" +
                                            "\\[project\\]\n" +
                                            "org-name=\"my_org\"\n" +
                                            "version=\"1.0.0\".*")
    public void testEmpty() throws TomlException {
        ManifestProcessor.parseTomlContentFromString("");
    }
    
    
    @Test(description = "Invalid Ballerina.toml file", expectedExceptions = TomlException.class,
          expectedExceptionsMessageRegExp = "invalid Ballerina.toml file: invalid key on line 1: \\[foo")
    public void testInvalid() throws TomlException {
        ManifestProcessor.parseTomlContentFromString("[foo");
    }

    @Test(description = "Attribute with single comment doesn't have an effect")
    public void testAttributeWithSingleComment() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "#Name of the module \n org-name = \"foo\" \n" +
                 "version = \"1.0.0\"");
        Assert.assertEquals(manifest.getProject().getOrgName(), "foo");
    }

    @Test(description = "Attribute with multiline comments doesn't have an effect")
    public void testAttributeWithMultilineComments() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "# Name of the module \n #This is the module config section \n org-name = \"foo/string\"\n" +
                                                                         "version = \"1.0.0\"");
        Assert.assertEquals(manifest.getProject().getOrgName(), "foo/string");
    }

    @Test(description = "Key with special characters in module section has no effect")
    public void testPackageNameWithSpecialCharacters() throws TomlException {
        ManifestProcessor.parseTomlContentFromString(this.validProjectBlock + " \n name-value = \"bar/string\"");
    }

    @Test(description = "Version in module section has an effect")
    public void testVersion() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString(this.validProjectBlock);
        Assert.assertEquals(manifest.getProject().getOrgName(), "foo");
        Assert.assertEquals(manifest.getProject().getVersion(), "1.0.0");
    }

    @Test(description = "Authors in module section has an effect")
    public void testAuthors() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString(this.validProjectBlock +
                "authors = [\"tyler@wso2.com\", \"manu@wso2.com\"]");
        Assert.assertEquals(manifest.getProject().getAuthors().get(0), "tyler@wso2.com");
        Assert.assertEquals(manifest.getProject().getAuthors().get(1), "manu@wso2.com");
    }

    @Test(description = "Empty author array in module section has an effect")
    public void testEmptyAuthorArray() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString(this.validProjectBlock + "authors = []");
        Assert.assertEquals(manifest.getProject().getAuthors().size(), 0);
    }

    @Test(description = "Repository url in module section has an effect")
    public void testRepositoryURL() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString(this.validProjectBlock +
                "repository = \"https://github.com/ballerinalang/ballerina\"");
        Assert.assertEquals(manifest.getProject().getRepository(), "https://github.com/ballerinalang/ballerina");
    }

    @Test(description = "Version in non-module section has no effect", expectedExceptions = TomlException.class,
          expectedExceptionsMessageRegExp = "invalid Ballerina.toml file: cannot find \\[project\\]")
    public void testVersionNeg() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[patches] \n version = \"v1\"");
        Assert.assertNull(manifest.getProject());
    }
    
    @Test(description = "Keywords in module section has an effect")
    public void testKeywords() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString(this.validProjectBlock +
                "keywords=[\"ballerina\",\"security\",\"security\"]");
        Assert.assertEquals(manifest.getProject().getKeywords().get(0), "ballerina");
        Assert.assertEquals(manifest.getProject().getKeywords().get(1), "security");
        Assert.assertEquals(manifest.getProject().getKeywords().size(), 3);
    }

    @Test(description = "Description in module section has an effect")
    public void testLicenseDescription() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString(this.validProjectBlock +
                "license = [\"MIT\", \"Apache-2.0\"]");
        Assert.assertEquals(manifest.getProject().getLicense().get(0), "MIT");
        Assert.assertEquals(manifest.getProject().getLicense().get(1), "Apache-2.0");
    }

    @Test(description = "One dependency added to the dependencies section has an effect")
    public void testSingleDependencies() throws TomlException, IOException {
        Path tmpDir = Files.createTempDirectory("manifest-test-");
        Path baloPath = tmpDir.resolve("string_utils.balo");
        Files.createFile(baloPath);
        
        Manifest manifest = ManifestProcessor.parseTomlContentFromString(this.validProjectBlock + "[dependencies] \n " +
                "string-utils = {path = '" + baloPath + "', version = \"1.1.5\"} \n");
        Assert.assertEquals(manifest.getDependencies().get(0).getModuleID(), "string-utils");
        Assert.assertEquals(manifest.getDependencies().get(0).getMetadata().getVersion(), "1.1.5");
        Assert.assertEquals(manifest.getDependencies().get(0).getMetadata().getPath().toString(), baloPath.toString());
        
        Files.delete(baloPath);
        Files.delete(tmpDir);
    }

    @Test(description = "One dependency added with path in the irregular form of path to the dependencies section " +
            "has an effect")
    public void testDependenciesIrregularPath() throws TomlException, IOException {
        Path tmpDir = Files.createTempDirectory("manifest-test-");
        Path baloPath = tmpDir.resolve("string_utils.balo");
        Files.createFile(baloPath);

        if (baloPath.toString().contains("\\")) {
            baloPath = Paths.get(baloPath.toString().replace("\\", "/"));
        } else {
            baloPath = Paths.get(baloPath.toString().replace("/", "\\"));
        }

        Manifest manifest = ManifestProcessor.parseTomlContentFromString(this.validProjectBlock + "[dependencies] \n " +
                "string-utils = {path = '" + baloPath + "', version = \"1.1.5\"} \n");
        Path manifestPath = manifest.getDependencies().get(0).getMetadata().getPath();
        if (manifestPath.toString().contains("\\")) {
            manifestPath = Paths.get(manifestPath.toString().replace("\\", "/"));
        } else {
            manifestPath = Paths.get(manifestPath.toString().replace("/", "\\"));
        }

        Assert.assertEquals(manifest.getDependencies().get(0).getModuleID(), "string-utils");
        Assert.assertEquals(manifest.getDependencies().get(0).getMetadata().getVersion(), "1.1.5");
        Assert.assertEquals(manifestPath.toString(), baloPath.toString());
    }

    @Test(description = "Empty dependency added to the dependencies section has no effect")
    public void testSingleEmptyDependencies() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString(this.validProjectBlock + "[dependencies] \n " +
                "string-utils = {} \n");
        Assert.assertEquals(manifest.getDependencies().get(0).getModuleID(), "string-utils");
    }

    @Test(description = "Multiple dependencies added with path in the regular form to the dependencies section " +
            "has an effect")
    public void testMultipleDependencies() throws TomlException, IOException {
        Path tmpDir = Files.createTempDirectory("manifest-test-");
        Path baloPath = tmpDir.resolve("string_utils.balo");
        Files.createFile(baloPath);
        
        Manifest manifest = ManifestProcessor.parseTomlContentFromString(this.validProjectBlock + "[dependencies] \n " +
                "string-utils = { path = '" + baloPath + "', version = \"1.0.5\" } \n " +
                "jquery = { version = \"2.2.3\" } \n");
        Assert.assertEquals(manifest.getDependencies().get(0).getModuleID(), "string-utils");
        Assert.assertEquals(manifest.getDependencies().get(0).getMetadata().getVersion(), "1.0.5");
        Assert.assertEquals(manifest.getDependencies().get(1).getModuleID(), "jquery");
        Assert.assertEquals(manifest.getDependencies().get(1).getMetadata().getVersion(), "2.2.3");
        Assert.assertEquals(manifest.getDependencies().get(0).getMetadata().getPath().toString(), baloPath.toString());

        Files.delete(baloPath);
        Files.delete(tmpDir);
    }

    @Test(description = "Dependencies added with Windows absolute path in the regular form to the dependencies " +
            "section has an effect")
    public void testDependencyWithWindowsAbsolutePath() throws TomlException, IOException {
        if (OS.contains("win")) {
            Path tmpDir = Files.createTempDirectory("manifest-test-");
            Path baloPath = tmpDir.resolve("string_utils.balo").toAbsolutePath();
            Files.createFile(baloPath);
            Manifest manifest = ManifestProcessor.parseTomlContentFromString(this.validProjectBlock +
                    "[dependencies] \n " + "string-utils = { path = '" + baloPath + "', version = \"1.0.5\" } \n " +
                    "jquery = { version = \"2.2.3\" } \n");
            Assert.assertEquals(manifest.getDependencies().get(0).getModuleID(), "string-utils");
            Assert.assertEquals(manifest.getDependencies().get(0).getMetadata().getVersion(), "1.0.5");
            Assert.assertEquals(manifest.getDependencies().get(1).getModuleID(), "jquery");
            Assert.assertEquals(manifest.getDependencies().get(1).getMetadata().getVersion(), "2.2.3");
            Assert.assertEquals(manifest.getDependencies().get(0).getMetadata().getPath().toString(),
                    baloPath.toString());

            Files.delete(baloPath);
            Files.delete(tmpDir);
        }
    }

    @Test(description = "Dependencies added with Windows absolute path in the regular form to the dependencies " +
            "section has an effect")
    public void testNativeLibWithRegularPath() throws TomlException, IOException {
        Path tmpDir = Files.createTempDirectory("manifest-test-");
        Path libPath = tmpDir.resolve("string_utils.jar");
        Files.createFile(libPath);

        Manifest manifest = ManifestProcessor.parseTomlContentFromString(this.validProjectBlock +
                "[platform] \n target = \"java11\" \n \n " +
                "[[platform.libraries]] \n " +
                "artifactId = \"utils\" \n path = '" + libPath + "'\n groupId = \"wso2\" \n " +
                "modules = [\"mymodule\"] ");
        Assert.assertEquals(manifest.platform.libraries.get(0).getPath(), libPath.toString());
        Files.delete(libPath);
        Files.delete(tmpDir);
    }

    @Test(description = "Dependencies added with Windows absolute path in the irregular form to the dependencies " +
            "section has an effect")
    public void testNativeLibWithIrregularPath() throws TomlException, IOException {
        Path tmpDir = Files.createTempDirectory("manifest-test-");
        Path libPath = tmpDir.resolve("string_utils.jar");
        Files.createFile(libPath);

        if (libPath.toString().contains("\\")) {
            libPath = Paths.get(libPath.toString().replace("\\", "/"));
        } else {
            libPath = Paths.get(libPath.toString().replace("/", "\\"));
        }
        Manifest manifest = ManifestProcessor.parseTomlContentFromString(this.validProjectBlock +
                "[platform] \n target = \"java11\" \n \n " +
                "[[platform.libraries]] \n " +
                "artifactId = \"utils\" \n path = '" + libPath + "'\n groupId = \"wso2\" \n " +
                "modules = [\"mymodule\"] ");
        Path manifestPath = Paths.get(manifest.platform.libraries.get(0).getPath());
        if (manifestPath.toString().contains("\\")) {
            manifestPath = Paths.get(manifestPath.toString().replace("\\", "/"));
        } else {
            manifestPath = Paths.get(manifestPath.toString().replace("/", "\\"));
        }
        Assert.assertEquals(manifestPath.toString(), libPath.toString());
    }
}
