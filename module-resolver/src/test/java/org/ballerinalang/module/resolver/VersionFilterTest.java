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

package org.ballerinalang.module.resolver;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.PrintStream;

import static org.ballerinalang.module.resolver.Util.isGreaterVersion;
import static org.ballerinalang.module.resolver.Util.isValidVersion;

public class VersionFilterTest {

    private static final PrintStream errStream = System.err;

    @DataProvider(name = "validFilterVersions")
    public Object[][] provideValidFilterVersions() {
        return new Object[][] {
                { "1.2.3", "" },
                { "1.2.3", "*" },
                { "1.2.3", "1.*" },
                { "1.2.3", "1.2.*" },
                { "1.2.3", "1.2.3" },
                { "~1.2.3", "1.2.9" }
        };
    }

    @DataProvider(name = "compareSemVersions")
    public Object[][] provideCompareSemVersions() {
        return new Object[][] {
                { "1.0", "1.1", true },
                { "1.2.3", "1.2.1", false },
                { "1.0", "1.0.1", true },
                { "1.2.3", "1.2", false },
                { "1.2.3", "1.2.3", false },
                { "9.0", "9.0", false }
        };
    }

    @Test(description = "Test valid versions for the given filter", dataProvider = "validFilterVersions")
    void testFilterVersion(String version, String filter) {
        if (!isValidVersion("1.2.3", "1.*")) {
            errStream.println("valid version for the given filter failed, version:" + version + " filter:" + filter);
            Assert.fail();
        }
    }

    @Test(description = "Test is greater semver version", dataProvider = "compareSemVersions")
    void testIsGreaterVersion(String version, String newVersion, boolean isGreaterVersion) {
        if (isGreaterVersion(version, newVersion) != isGreaterVersion) {
            errStream.println("comparing semver versions failed, version:" + version + " newVersion:" + newVersion);
            Assert.fail();
        }
    }
}
