package org.ballerinalang.test.config;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Test cases for retrieving tables from the config API.
 */
public class ConfigTableTest {

    private static final ConfigRegistry registry = ConfigRegistry.getInstance();
    private static final String BALLERINA_CONF = "ballerina.conf";
    private CompileResult compileResult;
    private String resourceRoot;
    private Path sourceRoot;
    private Path ballerinaConfPath;

    @BeforeClass
    public void setup() throws IOException {
        resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        sourceRoot = Paths.get(resourceRoot, "test-src", "config");
        ballerinaConfPath = Paths.get(resourceRoot, "datafiles", "config", BALLERINA_CONF);

        compileResult = BCompileUtil.compile(sourceRoot.resolve("config.bal").toString());
    }

    @Test
    public void testGetTable() throws IOException {
        BString key = new BString("http1");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsMap", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);

        BMap table = (BMap) returnVals[0];
        Assert.assertEquals(table.get("host").stringValue(), "localhost");
        Assert.assertEquals(Integer.parseInt(table.get("port").stringValue()), 8080);
    }

    @Test(description = "Test retrieving a non-existent table")
    public void testGetNonExistentTable() throws IOException {
        BString key = new BString("https");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsMap", inputArg);
        BMap table = (BMap) returnVals[0];

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        Assert.assertEquals(table.size(), 0);
    }

    @Test(description = "Test retrieving a table with nested tables")
    public void testGetNestedTables() throws IOException {
        BString key = new BString("http1");
        BValue[] inputArg = {key};

        registry.initRegistry(new HashMap<>(), null, ballerinaConfPath);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetAsMap", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);

        BMap table = (BMap) returnVals[0];
        Assert.assertEquals(table.size(), 3);
        Assert.assertEquals(table.get("host").stringValue(), "localhost");
        Assert.assertEquals(Integer.parseInt(table.get("port").stringValue()), 8080);
        Assert.assertEquals(table.get("hello.basePath").stringValue(), "/hello");
    }
}
