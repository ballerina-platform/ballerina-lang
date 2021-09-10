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
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.LESS_THAN);

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
    public void testInitialPreReleaseVersions() {
        SemanticVersion v1 = SemanticVersion.from("0.1.1-alpha");
        SemanticVersion v2 = SemanticVersion.from("0.1.1-alpha");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.EQUAL);

        v1 = SemanticVersion.from("0.1.1-alpha");
        v2 = SemanticVersion.from("0.1.2-alpha");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.LESS_THAN);

        v1 = SemanticVersion.from("0.1.1-alpha");
        v2 = SemanticVersion.from("0.1.2-beta");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.LESS_THAN);

        v1 = SemanticVersion.from("0.1.1-alpha");
        v2 = SemanticVersion.from("0.3.2-alpha");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.INCOMPATIBLE);

        v1 = SemanticVersion.from("0.1.1-alpha");
        v2 = SemanticVersion.from("0.3.2-alpha.1");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.INCOMPATIBLE);

        v1 = SemanticVersion.from("0.1.1-alpha.1");
        v2 = SemanticVersion.from("0.3.2-alpha.2");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.INCOMPATIBLE);

        v1 = SemanticVersion.from("0.1.1-alpha");
        v2 = SemanticVersion.from("0.3.2-beta");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.INCOMPATIBLE);

        v1 = SemanticVersion.from("0.1.1-alpha.1");
        v2 = SemanticVersion.from("0.3.2-beta");
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
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.INCOMPATIBLE);

        v1 = SemanticVersion.from("1.1.2");
        v2 = SemanticVersion.from("1.1.1");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.GREATER_THAN);

        v1 = SemanticVersion.from("1.2.1");
        v2 = SemanticVersion.from("1.1.1");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.GREATER_THAN);

        v1 = SemanticVersion.from("2.1.1");
        v2 = SemanticVersion.from("1.1.1");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.INCOMPATIBLE);
    }

    @Test
    public void testPreReleaseStableVersions() {
        SemanticVersion v1 = SemanticVersion.from("1.1.1-alpha");
        SemanticVersion v2 = SemanticVersion.from("1.1.1-alpha");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.EQUAL);

        v1 = SemanticVersion.from("1.1.1-alpha");
        v2 = SemanticVersion.from("1.1.1-beta");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.LESS_THAN);

        v1 = SemanticVersion.from("1.1.1-alpha");
        v2 = SemanticVersion.from("2.0.0-alpha");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.INCOMPATIBLE);
    }

    @Test
    public void testPreReleaseNumericVersions() {
        SemanticVersion v1 = SemanticVersion.from("1.1.1-alpha.1");
        SemanticVersion v2 = SemanticVersion.from("1.1.1-alpha.2");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.LESS_THAN);

        v1 = SemanticVersion.from("1.1.1-alpha.1");
        v2 = SemanticVersion.from("1.1.1-beta");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.LESS_THAN);

        v1 = SemanticVersion.from("1.1.1-alpha.1");
        v2 = SemanticVersion.from("1.1.2-beta");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.LESS_THAN);

        v1 = SemanticVersion.from("1.1.1-alpha.1");
        v2 = SemanticVersion.from("1.5.2-beta");
        Assert.assertEquals(v1.compareTo(v2), VersionCompatibilityResult.LESS_THAN);
    }

    @Test
    public void testGreaterThanComparison() {
        Assert.assertTrue(SemanticVersion.from("1.1.0").greaterThan(
                SemanticVersion.from("1.0.0")));
        Assert.assertTrue(SemanticVersion.from("1.1.0-alpha").greaterThan(
                SemanticVersion.from("1.0.0-beta")));
        Assert.assertTrue(SemanticVersion.from("1.0.1-alpha+kjkjkj").greaterThan(
                SemanticVersion.from("1.0.0-alpha+kjkjkj")));

        Assert.assertFalse(SemanticVersion.from("1.1.0").greaterThan(
                SemanticVersion.from("1.5.0")));
        Assert.assertFalse(SemanticVersion.from("1.0.0+kjkjkj").greaterThan(
                SemanticVersion.from("1.0.0+kjkjkj")));
    }

    @Test
    public void testGreaterThanOrEqualComparison() {
        Assert.assertTrue(SemanticVersion.from("1.1.0").greaterThanOrEqualTo(
                SemanticVersion.from("1.0.0")));
        Assert.assertTrue(SemanticVersion.from("1.0.0-alpha").greaterThanOrEqualTo(
                SemanticVersion.from("1.0.0-alpha")));
        Assert.assertTrue(SemanticVersion.from("1.0.0-alpha+kjkjkj").greaterThanOrEqualTo(
                SemanticVersion.from("1.0.0-alpha+kjkjkj")));
    }

    @Test
    public void testLessThanComparison() {
        Assert.assertFalse(SemanticVersion.from("1.1.0").lessThan(
                SemanticVersion.from("1.0.0")));
        Assert.assertFalse(SemanticVersion.from("1.1.0-alpha").lessThan(
                SemanticVersion.from("1.0.0-beta")));
        Assert.assertFalse(SemanticVersion.from("1.0.1-alpha+kjkjkj").lessThan(
                SemanticVersion.from("1.0.0-alpha+kjkjkj")));

        Assert.assertTrue(SemanticVersion.from("1.1.0").lessThan(
                SemanticVersion.from("1.5.0")));
        Assert.assertFalse(SemanticVersion.from("1.0.0+kjkjkj").lessThan(
                SemanticVersion.from("1.0.0+kjkjkj")));
    }

    @Test
    public void testLessThanOrEqualComparison() {
        Assert.assertFalse(SemanticVersion.from("1.1.0").lessThanOrEqualTo(
                SemanticVersion.from("1.0.0")));
        Assert.assertTrue(SemanticVersion.from("1.0.0-alpha").lessThanOrEqualTo(
                SemanticVersion.from("1.0.0-alpha")));
        Assert.assertTrue(SemanticVersion.from("1.0.0-alpha+kjkjkj").lessThanOrEqualTo(
                SemanticVersion.from("1.0.0-alpha+kjkjkj")));
    }

    @Test
    public void testLatestComparison() {
        SemanticVersion v1 = SemanticVersion.from("1.1.0");
        SemanticVersion v2 = SemanticVersion.from("1.1.0-alpha");
        Assert.assertEquals(VersionCompatibilityResult.GREATER_THAN, v1.compareTo(v2));
    }
}
