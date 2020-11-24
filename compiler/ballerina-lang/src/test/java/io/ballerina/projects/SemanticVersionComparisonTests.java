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

import io.ballerina.projects.SemanticVersion.VersionCompatibilityResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Contain cases to validate the functionality of {@code SemanticVersion}.
 *
 * @since 2.0.0
 */
public class SemanticVersionComparisonTests {

    @Test
    public void testInitialVersions() {
        SemanticVersion v1 = SemanticVersion.from("0.1.1");
        SemanticVersion v2 = SemanticVersion.from("0.1.1");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.EQUAL);

        v1 = SemanticVersion.from("0.1.1");
        v2 = SemanticVersion.from("0.1.2");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.INCOMPATIBLE);

        v1 = SemanticVersion.from("0.1.1");
        v2 = SemanticVersion.from("0.3.2");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.INCOMPATIBLE);

        v1 = SemanticVersion.from("0.1.1");
        v2 = SemanticVersion.from("1.3.2");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.INCOMPATIBLE);

        v1 = SemanticVersion.from("1.1.1");
        v2 = SemanticVersion.from("0.3.2");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.INCOMPATIBLE);

        v1 = SemanticVersion.from("2.1.1");
        v2 = SemanticVersion.from("0.3.2");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.INCOMPATIBLE);
    }

    @Test
    public void testStableVersions() {
        SemanticVersion v1 = SemanticVersion.from("1.1.1");
        SemanticVersion v2 = SemanticVersion.from("1.1.1");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.EQUAL);

        v1 = SemanticVersion.from("3.0.0");
        v2 = SemanticVersion.from("3.0.0");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.EQUAL);

        v1 = SemanticVersion.from("1.1.1");
        v2 = SemanticVersion.from("1.1.2");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.LESS_THAN);

        v1 = SemanticVersion.from("1.1.1");
        v2 = SemanticVersion.from("1.2.1");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.LESS_THAN);

        v1 = SemanticVersion.from("1.1.1");
        v2 = SemanticVersion.from("2.1.1");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.LESS_THAN);

        v1 = SemanticVersion.from("1.1.2");
        v2 = SemanticVersion.from("1.1.1");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.GREATER_THAN);

        v1 = SemanticVersion.from("1.2.1");
        v2 = SemanticVersion.from("1.1.1");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.GREATER_THAN);

        v1 = SemanticVersion.from("2.1.1");
        v2 = SemanticVersion.from("1.1.1");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.GREATER_THAN);
    }
}
