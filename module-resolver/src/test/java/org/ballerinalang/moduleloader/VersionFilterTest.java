package org.ballerinalang.moduleloader;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.PrintStream;

import static org.ballerinalang.moduleloader.Util.isGreaterVersion;
import static org.ballerinalang.moduleloader.Util.isValidVersion;

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
