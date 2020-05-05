package org.ballerinalang.moduleloader;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.PrintStream;

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

    @Test(description = "Test valid versions for the given filter", dataProvider = "validFilterVersions")
    void testFilterVersion(String version, String filter) {
        if (!isValidVersion("1.2.3", "1.*")) {
            errStream.println("valid version for the given filter failed, version:" + version + " filter:" + filter);
            Assert.fail();
        }
    }
}
