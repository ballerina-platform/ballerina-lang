package org.wso2.ballerinalang.compiler.module.resolver;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.Central;

import java.io.IOException;
import java.util.List;

public class RepoTest {

    @Test(description = "Get module version from ModuleId if module version exists in ModuleId")
    void testGetCentralVersions() throws IOException {
        List<String> versions = Central.getCentralVersions("wso2", "sfdc46");

        Assert.assertEquals(versions.size(), 3);
        Assert.assertEquals(versions.get(0), "0.10.0");
        Assert.assertEquals(versions.get(1), "0.10.1");
        Assert.assertEquals(versions.get(2), "0.11.0");
    }
}
