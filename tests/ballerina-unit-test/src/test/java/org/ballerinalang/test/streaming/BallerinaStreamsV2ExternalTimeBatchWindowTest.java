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

/**
 * This contains methods to test ExternalTimeBatchWindow behaviour in Ballerina Streaming V2.
 *
 * @since 0.981.2
 */
public class BallerinaStreamsV2ExternalTimeBatchWindowTest {

    private CompileResult result1, result2, result3, result4, result5, result6;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result1 = BCompileUtil.compile("test-src/streaming/streamingv2-external-time-batch-window-test.bal");

        System.setProperty("enable.siddhiRuntime", "false");
        result2 = BCompileUtil.compile("test-src/streaming/streamingv2-external-time-batch-window-test2.bal");

        System.setProperty("enable.siddhiRuntime", "false");
        result3 = BCompileUtil.compile("test-src/streaming/streamingv2-external-time-batch-window-test3.bal");

        System.setProperty("enable.siddhiRuntime", "false");
        result4 = BCompileUtil.compile("test-src/streaming/streamingv2-external-time-batch-window-test4.bal");

        System.setProperty("enable.siddhiRuntime", "false");
        result5 = BCompileUtil.compile("test-src/streaming/streamingv2-external-time-batch-window-test5.bal");

        System.setProperty("enable.siddhiRuntime", "false");
        result6 = BCompileUtil.compile("test-src/streaming/streamingv2-external-time-batch-window-test6.bal");
    }

    @Test(description = "Test externalTimeBatch query")
    public void testExternalTimeBatchQuery1() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result1, "startExternalTimeBatchwindowTest1");
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
    public void testExternalTimeBatchQuery2() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result2, "startExternalTimeBatchwindowTest2");
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
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result3, "startExternalTimeBatchwindowTest3");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 1, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];

        Assert.assertEquals(employee0.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 4);
    }

    @Test(description = "Test externalTimeBatch query")
    public void testExternalTimeBatchQuery4() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result4, "startExternalTimeBatchwindowTest4");
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
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result5, "startExternalTimeBatchwindowTest5");
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
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result6, "startExternalTimeBatchwindowTest6");
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
