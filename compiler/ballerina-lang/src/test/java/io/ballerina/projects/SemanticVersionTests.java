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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Contain cases to validate the functionality of {@code SemanticVersion}.
 *
 * @since 2.0.0
 */
public class SemanticVersionTests {

    @Test
    public void testPreReleaseVersion() {
        SemanticVersion version = SemanticVersion.from("1.0.1-alpha");
        Assert.assertEquals(version.major(), 1);
        Assert.assertEquals(version.minor(), 0);
        Assert.assertEquals(version.patch(), 1);
        Assert.assertEquals(version.preReleaseVersion(), "alpha");

        version = SemanticVersion.from("1.0.1-alpha.1.2.3.4.23423");
        Assert.assertEquals(version.preReleaseVersion(), "alpha.1.2.3.4.23423");
    }

    @Test
    public void testBuildMetadata() {
        SemanticVersion version = SemanticVersion.from("1.0.1+20130313144700");
        Assert.assertEquals(version.major(), 1);
        Assert.assertEquals(version.minor(), 0);
        Assert.assertEquals(version.patch(), 1);
        Assert.assertEquals(version.buildMetadata(), "20130313144700");

        version = SemanticVersion.from("1.0.1+20130313144700.A1234.34343a");
        Assert.assertEquals(version.buildMetadata(), "20130313144700.A1234.34343a");
    }

    @Test
    public void testPreReleaseAndBuildMetadata() {
        SemanticVersion version = SemanticVersion.from("1.0.1-alpha1.0.0+20130313144700.1.2.a");
        Assert.assertEquals(version.major(), 1);
        Assert.assertEquals(version.minor(), 0);
        Assert.assertEquals(version.patch(), 1);
        Assert.assertEquals(version.preReleaseVersion(), "alpha1.0.0");
        Assert.assertEquals(version.buildMetadata(), "20130313144700.1.2.a");
    }
}
