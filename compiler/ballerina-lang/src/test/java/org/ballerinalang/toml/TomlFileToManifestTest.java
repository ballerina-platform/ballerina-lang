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
 * Test class to populate a Manifest object from a toml file.
 */
public class TomlFileToManifestTest {
    private static final String userDir = System.getProperty("user.dir");
    private static final String resource_dir = userDir + "/src/test/resources/";

    @Test(description = "Test which covers all the attributes tested above")
    public void testTomlFile() throws IOException {
        Manifest manifest = ManifestProcessor.parseTomlContentFromFile(resource_dir + "example.toml");
        Assert.assertEquals(manifest.getName(), "foo");
        Assert.assertEquals(manifest.getVersion(), "1.0.0");
        Assert.assertEquals(manifest.getDescription(), "This is a sample description which contains " +
                "information");
        Assert.assertEquals(manifest.getDocumentationURL(), "https://ballerinalang.org/docs/api/0.95.5/");
        Assert.assertEquals(manifest.getHomepageURL(), "https://ballerinalang.org/");
        Assert.assertEquals(manifest.getRepositoryURL(), "https://github.com/ballerinalang/ballerina");
        Assert.assertEquals(manifest.getReadmeFilePath(), "https://github.com/ballerinalang/composer/blob/" +
                "master/README.md");
        Assert.assertEquals(manifest.getAuthors().get(0), "tyler@wso2.com");
        Assert.assertEquals(manifest.getAuthors().get(1), "manu@wso2.com");

        Assert.assertEquals(manifest.getKeywords().get(0), "ballerina");
        Assert.assertEquals(manifest.getKeywords().get(2), "crypto");
        Assert.assertEquals(manifest.getKeywords().size(), 3);

        Assert.assertEquals(manifest.getDependencies().size(), 6);
        Assert.assertEquals(manifest.getDependencies().get(0).getPackageName(), "synchapi");
        Assert.assertEquals(manifest.getDependencies().get(0).getVersion(), "0.9.2");

        Assert.assertEquals(manifest.getDependencies().get(1).getLocation(), "src/libc");
        Assert.assertEquals(manifest.getDependencies().get(1).getPackageName(), "libc");

        Assert.assertEquals(manifest.getDependencies().get(2).getPackageName(), "string-utils");
        Assert.assertEquals(manifest.getDependencies().get(3).getLocation(), null);

        Assert.assertEquals(manifest.getDependencies().get(4).getPackageName(), "toml");
        Assert.assertEquals(manifest.getDependencies().get(4).getVersion(), "0.4.6");

        Assert.assertEquals(manifest.getDependencies().get(5).getLocation(), "src/core/jobapi");
        Assert.assertEquals(manifest.getDependencies().get(5).getPackageName(), "jobapi.core");
    }
}
