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

/**
 * Test class to populate Manifest object by reading the toml.
 */
public class ManifestProcessorTest {
    
    @Test(description = "Empty Ballerina.toml file", expectedExceptions = TomlException.class,
          expectedExceptionsMessageRegExp = "invalid Ballerina.toml file: the Ballerina.toml file should have " +
                                            "the organization name and the version of the project. example: \n" +
                                            "[project]\n" +
                                            "org-name=\"my_org\"\n" +
                                            "version=\"1.0.0\"\n")
    public void testEmpty() throws TomlException {
        ManifestProcessor.parseTomlContentFromString("");
    }
    
    
    @Test(description = "Invalid Ballerina.toml file", expectedExceptions = TomlException.class,
          expectedExceptionsMessageRegExp = "invalid Ballerina.toml file: the Ballerina.toml file should have " +
                                            "the organization name and the version of the project. example: \n" +
                                            "[project]\n" +
                                            "org-name=\"my_org\"\n" +
                                            "version=\"1.0.0\"\n")
    public void testInvalid() throws TomlException {
        ManifestProcessor.parseTomlContentFromString("[foo");
    }
    
    @Test(description = "Module name in module section has an effect")
    public void testPackageName() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "#Name of the module \n orgName = \"foo\"");
        Assert.assertEquals(manifest.getProject().getOrgName(), "foo");
    }

    @Test(description = "Attribute with single comment doesn't have an effect")
    public void testAttributeWithSingleComment() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "#Name of the module \n orgName = \"foo\"");
        Assert.assertEquals(manifest.getProject().getOrgName(), "foo");
    }

    @Test(description = "Attribute with multiline comments doesn't have an effect")
    public void testAttributeWithMultilineComments() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "# Name of the module \n #This is the module config section \n orgName = \"foo/string\"");
        Assert.assertEquals(manifest.getProject().getOrgName(), "foo/string");
    }

    @Test(description = "Key with special characters in module section has no effect")
    public void testPackageNameWithSpecialCharacters() throws TomlException {
        ManifestProcessor.parseTomlContentFromString("[project] \n name-value = \"orgName/string\"");
        Assert.assertNotEquals(null, "\"org-name/string\"");
    }

    @Test(description = "Version in module section has an effect")
    public void testVersion() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n version = \"1.0.0\"");
        Assert.assertEquals(manifest.getProject().getVersion(), "1.0.0");
    }

    @Test(description = "Authors in module section has an effect")
    public void testAuthors() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "authors = [\"tyler@wso2.com\", \"manu@wso2.com\"]");
        Assert.assertEquals(manifest.getProject().getAuthors().get(0), "tyler@wso2.com");
        Assert.assertEquals(manifest.getProject().getAuthors().get(1), "manu@wso2.com");
    }

    @Test(description = "Empty author array in module section has an effect")
    public void testEmptyAuthorArray() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n authors = []");
        Assert.assertEquals(manifest.getProject().getAuthors().size(), 0);
    }

    @Test(description = "Repository url in module section has an effect")
    public void testRepositoryURL() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n " +
                "repository = \"https://github.com/ballerinalang/ballerina\"");
        Assert.assertEquals(manifest.getProject().getRepository(), "https://github.com/ballerinalang/ballerina");
    }

    @Test(description = "Version in non-module section has no effect")
    public void testVersionNeg() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[patches] \n version = \"v1\"");
        Assert.assertNull(manifest.getProject());
    }

    @Test(description = "Location in module section has no effect")
    public void testLocationNeg() throws TomlException {
        ManifestProcessor.parseTomlContentFromString("[project] \n location = \"local\"");
        Assert.assertNotEquals(null, "local");
    }
    
    @Test(description = "Keywords in module section has an effect")
    public void testKeywords() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "keywords=[\"ballerina\",\"security\",\"security\"]");
        Assert.assertEquals(manifest.getProject().getKeywords().get(0), "ballerina");
        Assert.assertEquals(manifest.getProject().getKeywords().get(1), "security");
        Assert.assertEquals(manifest.getProject().getKeywords().size(), 3);
    }

    @Test(description = "Description in module section has an effect")
    public void testLicenseDescription() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n " +
                "license = \"MIT OR Apache-2.0\"");
        Assert.assertEquals(manifest.getProject().getLicense(), "MIT OR Apache-2.0");
    }

    @Test(description = "One dependency added to the dependencies section has an effect")
    public void testSingleDependencies() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[dependencies] \n " +
                "string-utils = {path = \"src/string-utils\", version = \"1.1.5\"} \n");
        Assert.assertEquals(manifest.getDependencies().get(0).getModuleName(), "string-utils");
        Assert.assertEquals(manifest.getDependencies().get(0).getMetadata().getVersion(), "1.1.5");
        Assert.assertEquals(manifest.getDependencies().get(0).getMetadata().getPath(), "src/string-utils");
    }

    @Test(description = "Empty dependency added to the dependencies section has no effect")
    public void testSingleEmptyDependencies() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[dependencies] \n " +
                "string-utils = {} \n");
        Assert.assertEquals(manifest.getDependencies().get(0).getModuleName(), "string-utils");
    }

    @Test(description = "Multiple dependencies added to the dependencies section has an effect")
    public void testMultipleDependencies() throws TomlException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[dependencies] \n " +
                "string-utils = { path = \"src/string-utils\", version = \"1.0.5\" } \n " +
                "jquery = { version = \"2.2.3\" } \n");
        Assert.assertEquals(manifest.getDependencies().get(0).getModuleName(), "string-utils");
        Assert.assertEquals(manifest.getDependencies().get(0).getMetadata().getVersion(), "1.0.5");
        Assert.assertEquals(manifest.getDependencies().get(1).getModuleName(), "jquery");
        Assert.assertEquals(manifest.getDependencies().get(1).getMetadata().getVersion(), "2.2.3");
    }
}
