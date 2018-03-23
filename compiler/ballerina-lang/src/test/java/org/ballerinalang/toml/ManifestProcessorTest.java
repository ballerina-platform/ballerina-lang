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


import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test class to populate Manifest object by reading the toml.
 */
public class ManifestProcessorTest {
    @Test(description = "Package name in package section has an effect")
    public void testPackageName() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "#Name of the package \n org-name = \"foo\"");
        Assert.assertEquals(manifest.getName(), "foo");
    }

    @Test(description = "Attribute with single comment doesn't have an effect")
    public void testAttributeWithSingleComment() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "#Name of the package \n org-name = \"foo\"");
        Assert.assertEquals(manifest.getName(), "foo");
    }

    @Test(description = "Attribute with multiline comments doesn't have an effect")
    public void testAttributeWithMultilineComments() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "# Name of the package \n #This is the package congif section \n org-name = \"foo/string\"");
        Assert.assertEquals(manifest.getName(), "foo/string");
    }

    @Test(description = "Key with special characters in package section has no effect")
    public void testPackageNameWithSpecialCharacters() throws IOException {
        ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "name-value = \"org-name/string\"");
        Assert.assertNotEquals(null, "\"org-name/string\"");
    }

    @Test(description = "Version in package section has an effect")
    public void testVersion() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project]\n" +
                "version = \"1.0.0\"");
        Assert.assertEquals(manifest.getVersion(), "1.0.0");
    }

    @Test(description = "Authors in package section has an effect")
    public void testAuthors() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "authors = [\"tyler@wso2.com\", \"manu@wso2.com\"]");
        Assert.assertEquals(manifest.getAuthors().get(0), "tyler@wso2.com");
        Assert.assertEquals(manifest.getAuthors().get(1), "manu@wso2.com");
    }

    @Test(description = "Empty author array in package section has an effect")
    public void testEmptyAuthorArray() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "authors = []");
        Assert.assertEquals(manifest.getAuthors().size(), 0);
    }

    @Test(description = "Description in package section has an effect")
    public void testDescription() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "description = \"This is a description about the package\"");
        Assert.assertEquals(manifest.getDescription(), "This is a description about the package");
    }

    @Test(description = "Documentation url in package section has an effect")
    public void testDocumentationURL() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n " +
                "documentation = \"https://ballerinalang.org/docs/api/0.95.5/\"");
        Assert.assertEquals(manifest.getDocumentationURL(), "https://ballerinalang.org/docs/api/0.95.5/");
    }

    @Test(description = "Homepage url in package section has an effect")
    public void testHomePageURL() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n " +
                "homepage = \"https://ballerinalang.org/\"");
        Assert.assertEquals(manifest.getHomepageURL(), "https://ballerinalang.org/");
    }

    @Test(description = "Repository url in package section has an effect")
    public void testRepositoryURL() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n " +
                "repository = \"https://github.com/ballerinalang/ballerina\"");
        Assert.assertEquals(manifest.getRepositoryURL(), "https://github.com/ballerinalang/ballerina");
    }

    @Test(description = "Version in non-package section has no effect")
    public void testVersionNeg() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[patch] \n version = \"v1\"");
        Assert.assertNotEquals(manifest.getVersion(), "v1");
    }

    @Test(description = "Location in package section has no effect")
    public void testLocationNeg() throws IOException {
        ManifestProcessor.parseTomlContentFromString("[project] \n location = \"local\"");
        Assert.assertNotEquals(null, "local");
    }

    @Test(description = "Readme file path in package section has an effect")
    public void testReadmeFilePath() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n " +
                "readme = \"https://github.com/ballerinalang/composer/blob/master/README.md\"");
        Assert.assertEquals(manifest.getReadmeFilePath(), "https://github.com/ballerinalang/composer/blob" +
                "/master/README.md");
    }

    @Test(description = "Keywords in package section has an effect")
    public void testKeywords() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n" +
                "keywords=[\"ballerina\",\"security\",\"security\"]");
        Assert.assertEquals(manifest.getKeywords().get(0), "ballerina");
        Assert.assertEquals(manifest.getKeywords().get(1), "security");
        Assert.assertEquals(manifest.getKeywords().size(), 3);
    }

    @Test(description = "Description in package section has an effect")
    public void testLicenseDescription() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[project] \n " +
                "license = \"MIT OR Apache-2.0\"");
        Assert.assertEquals(manifest.getLicense(), "MIT OR Apache-2.0");
    }

    @Test(description = "One dependency added to the dependencies section has an effect")
    public void testSingleDependancies() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[dependencies] \n " +
                "string-utils = {location = \"src/string-utils\", version = \"1.1.5\"} \n");
        Assert.assertEquals(manifest.getDependencies().get(0).getPackageName(), "string-utils");
        Assert.assertEquals(manifest.getDependencies().get(0).getVersion(), "1.1.5");
        Assert.assertEquals(manifest.getDependencies().get(0).getLocation(), "src/string-utils");
    }

    @Test(description = "Empty dependency added to the dependencies section has no effect")
    public void testSingleEmptyDependancies() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[dependencies] \n " +
                "string-utils = {} \n");
        Assert.assertEquals(manifest.getDependencies().get(0).getPackageName(), "string-utils");
    }

    @Test(description = "Multiple dependencies added to the dependencies section has an effect")
    public void testMultipleDependancies() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[dependencies] \n " +
                "string-utils = {location = \"src/string-utils\", version = \"1.0.5\" } \n " +
                "jquery = { version = \"2.2.3\" } \n");
        Assert.assertEquals(manifest.getDependencies().get(0).getPackageName(), "string-utils");
        Assert.assertEquals(manifest.getDependencies().get(0).getVersion(), "1.0.5");
        Assert.assertEquals(manifest.getDependencies().get(1).getPackageName(), "jquery");
        Assert.assertEquals(manifest.getDependencies().get(1).getVersion(), "2.2.3");
    }

    @Test(description = "One dependency added to the dependencies section individually has an effect")
    public void testSingleDependanciesAdded() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[dependencies.string-utils] \n " +
                "version = \"1.0.5\" \n location = \"src/string-utils\"");
        Assert.assertEquals(manifest.getDependencies().get(0).getPackageName(), "string-utils");
        Assert.assertEquals(manifest.getDependencies().get(0).getVersion(), "1.0.5");
        Assert.assertEquals(manifest.getDependencies().get(0).getLocation(), "src/string-utils");
    }

    @Test(description = "Multiple dependencies added to the dependencies section individually has an effect")
    public void testMultipleDependanciesAddedIndividually() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[dependencies.string-utils] \n " +
                "version = \"1.1.5\" \n location = \"src/string-utils\" \n [dependencies.jquery] \n " +
                "version = \"2.2.3\"");

        Assert.assertEquals(manifest.getDependencies().get(0).getPackageName(), "string-utils");
        Assert.assertEquals(manifest.getDependencies().get(0).getVersion(), "1.1.5");
        Assert.assertEquals(manifest.getDependencies().get(0).getLocation(), "src/string-utils");

        Assert.assertEquals(manifest.getDependencies().get(1).getPackageName(), "jquery");
        Assert.assertEquals(manifest.getDependencies().get(1).getVersion(), "2.2.3");
        Assert.assertEquals(manifest.getDependencies().get(1).getLocation(), null);
    }

    @Test(description = "One patch added to the patches section has an effect")
    public void testSinglePatch() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[patches] \n " +
                "string-utils = {version = \"1.6.1\", location = \"src/patches/string-utils\" } \n");
        Assert.assertEquals(manifest.getPatches().get(0).getPackageName(), "string-utils");
        Assert.assertEquals(manifest.getPatches().get(0).getVersion(), "1.6.1");
        Assert.assertEquals(manifest.getPatches().get(0).getLocation(), "src/patches/string-utils");
    }

    @Test(description = "Multiple patches added to the patches section has an effect")
    public void testMultiplePatches() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[patches] \n " +
                "string-utils = {version = \"1.5.2\" } \n " +
                "jquery = { version = \"2.2.1\" } \n");
        Assert.assertEquals(manifest.getPatches().get(0).getPackageName(), "string-utils");
        Assert.assertEquals(manifest.getPatches().get(0).getVersion(), "1.5.2");
        Assert.assertEquals(manifest.getPatches().get(1).getPackageName(), "jquery");
        Assert.assertEquals(manifest.getPatches().get(1).getVersion(), "2.2.1");
    }

    @Test(description = "Dependencies added both ways i.e. individually and multiple dependencies together has" +
            "an effect")
    public void testMixtureOfDependencies() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[dependencies.string-utils] \n " +
                "version = \"1.5\" \n location = \"src/string-utils\" \n [dependencies] \n " +
                "jquery = {version = \"2.2.3\"} \n react = {version = \"1.6.6\", location = \"npm-modules/react\"} \n" +
                "[dependencies.toml] \n version = \"1.4.5\" \n ");

        Assert.assertEquals(manifest.getDependencies().size(), 4);
        Assert.assertEquals(manifest.getDependencies().get(0).getPackageName(), "string-utils");
        Assert.assertEquals(manifest.getDependencies().get(0).getVersion(), "1.5");
        Assert.assertEquals(manifest.getDependencies().get(0).getLocation(), "src/string-utils");

        Assert.assertEquals(manifest.getDependencies().get(1).getPackageName(), "jquery");
        Assert.assertEquals(manifest.getDependencies().get(1).getVersion(), "2.2.3");
        Assert.assertEquals(manifest.getDependencies().get(1).getLocation(), null);

        Assert.assertEquals(manifest.getDependencies().get(2).getPackageName(), "react");
        Assert.assertEquals(manifest.getDependencies().get(2).getVersion(), "1.6.6");
        Assert.assertEquals(manifest.getDependencies().get(2).getLocation(), "npm-modules/react");

        Assert.assertEquals(manifest.getDependencies().get(3).getPackageName(), "toml");
        Assert.assertEquals(manifest.getDependencies().get(3).getVersion(), "1.4.5");
    }

    @Test(description = "Dependencies and patches added together has an effect")
    public void testDependenciesAndPatches() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromString("[dependencies.string-utils] \n " +
                "version = \"1.1.5\" \n location = \"src/string-utils\" \n [patches] \n jobapi = {version =" +
                "\"2.23\"} \n [dependencies] \n jquery = {version = \"2.2.3\"} \n react = {version = \"1.6.6\", " +
                "location = \"npm-modules/react\"} \n [patches.toml] \n version = \"0.4.5\" \n");

        Assert.assertEquals(manifest.getDependencies().size(), 3);
        Assert.assertEquals(manifest.getPatches().size(), 2);

        Assert.assertEquals(manifest.getDependencies().get(0).getPackageName(), "string-utils");
        Assert.assertEquals(manifest.getDependencies().get(0).getVersion(), "1.1.5");
        Assert.assertEquals(manifest.getDependencies().get(1).getPackageName(), "jquery");
        Assert.assertEquals(manifest.getDependencies().get(2).getVersion(), "1.6.6");

        Assert.assertEquals(manifest.getPatches().get(0).getPackageName(), "jobapi");
        Assert.assertEquals(manifest.getPatches().get(1).getPackageName(), "toml");

    }
}
