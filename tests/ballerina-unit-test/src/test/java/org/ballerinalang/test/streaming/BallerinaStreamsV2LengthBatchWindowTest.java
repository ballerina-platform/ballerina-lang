package org.ballerinalang.test.streaming;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BallerinaStreamsV2LengthBatchWindowTest {
    private CompileResult result1, result2;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result1 = BCompileUtil.compile("test-src/streaming/streamingv2-length-batch-window-test.bal");

        System.setProperty("enable.siddhiRuntime", "false");
        result2 = BCompileUtil.compile("test-src/streaming/streamingv2-length-batch-window-test2.bal");
    }

    @Test(description = "Test lengthBatch window query")
    public void testExternalTimeBatchQuery1() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result1, "startLengthBatchwindowTest1");
        System.setProperty("enable.siddhiRuntime", "true");

        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 1, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];

        Assert.assertEquals(employee0.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 2);
    }

    @Test(description = "Test lengthBatch window query")
    public void testExternalTimeBatchQuery2() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result2, "startLengthBatchwindowTest2");
        System.setProperty("enable.siddhiRuntime", "true");

        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 2);

        Assert.assertEquals(employee1.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 2);
    }
}
