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

public class BallerinaStreamsV2ExternalTimeBatchWindowTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/streamingv2-external-time-batch-window-test.bal");
    }

    @Test(description = "Test externalTimeBatch query")
    public void testExternalTimeBatchQuery1() {
        System.setProperty("enable.siddhiRuntime", "true");
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startExternalTimeBatchwindowTest1");

        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 3);

        Assert.assertEquals(employee1.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 1);
    }

    @Test(description = "Test externalTimeBatch query")
    public void testExternalTimeBatchQuery2() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/streamingv2-external-time-batch-window-test2.bal");

        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startExternalTimeBatchwindowTest2");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 2);

        Assert.assertEquals(employee1.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 2);
    }

    @Test(description = "Test externalTimeBatch query")
    public void testExternalTimeBatchQuery3() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/streamingv2-external-time-batch-window-test3.bal");

        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startExternalTimeBatchwindowTest3");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 1, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];

        Assert.assertEquals(employee0.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 4);
    }

    @Test(description = "Test externalTimeBatch query")
    public void testExternalTimeBatchQuery4() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/streamingv2-external-time-batch-window-test4.bal");

        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startExternalTimeBatchwindowTest4");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 3);

        Assert.assertEquals(employee1.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 1);
    }

    @Test(description = "Test externalTimeBatch query")
    public void testExternalTimeBatchQuery5() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/streamingv2-external-time-batch-window-test5.bal");

        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startExternalTimeBatchwindowTest5");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 3);

        Assert.assertEquals(employee1.get("name").stringValue(), "Nimal");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 2);
    }

    @Test(description = "Test externalTimeBatch query")
    public void testExternalTimeBatchQuery6() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/streamingv2-external-time-batch-window-test6.bal");

        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startExternalTimeBatchwindowTest6");
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
