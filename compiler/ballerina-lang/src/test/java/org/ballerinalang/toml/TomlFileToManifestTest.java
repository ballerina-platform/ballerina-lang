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
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.nio.file.Paths;

/**
 * Test class to populate a Manifest object from a toml file.
 */
public class TomlFileToManifestTest {

    @Test(description = "Test which covers all the attributes tested above")
    public void testTomlFile() {
        Manifest manifest = TomlParserUtils.getManifest(Paths.get(System.getProperty("user.dir"), "src", "test",
                                                                  "resources"));
        Assert.assertEquals(manifest.getProject().getOrgName(), "foo");
        Assert.assertEquals(manifest.getProject().getVersion(), "1.0.0");
        Assert.assertEquals(manifest.getProject().getRepository(), "https://github.com/ballerinalang/ballerina");
        Assert.assertEquals(manifest.getProject().getAuthors().get(0), "tyler@wso2.com");
        Assert.assertEquals(manifest.getProject().getAuthors().get(1), "manu@wso2.com");

        Assert.assertEquals(manifest.getProject().getKeywords().get(0), "ballerina");
        Assert.assertEquals(manifest.getProject().getKeywords().get(2), "crypto");
        Assert.assertEquals(manifest.getProject().getKeywords().size(), 3);

        Assert.assertEquals(manifest.getDependencies().size(), 2);
        
        Assert.assertEquals(manifest.getDependencies().get(0).getModuleName(), "wso2/twitter");
        Assert.assertEquals(manifest.getDependencies().get(0).getMetadata().getVersion(), "2.3.4");
    
        Assert.assertEquals(manifest.getDependencies().get(1).getModuleName(), "wso2/github");
        Assert.assertEquals(manifest.getDependencies().get(1).getMetadata().getVersion(), "1.2.3");
        Assert.assertEquals(manifest.getDependencies().get(1).getMetadata().getPath(), "path/to/github.balo");
        
    }
}
