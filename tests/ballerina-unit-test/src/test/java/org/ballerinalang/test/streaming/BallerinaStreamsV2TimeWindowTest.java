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

public class BallerinaStreamsV2TimeWindowTest {
    private CompileResult result1, result2;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result1 = BCompileUtil.compile("test-src/streaming/streamingv2-time-window-test.bal");

        System.setProperty("enable.siddhiRuntime", "false");
        result2 = BCompileUtil.compile("test-src/streaming/streamingv2-time-window-test2.bal");
    }

    @Test(description = "Test Time window query")
    public void testExternalTimeQuery1() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result1, "startTimeWindowTest");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(employee1.get("name").stringValue(), "Raja");
    }

    @Test(description = "Test Time window query")
    public void testExternalTimeQuery2() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result2, "startTimeWindowTest2");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 6, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[2];

        Assert.assertEquals(employee0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 1);

        Assert.assertEquals(employee1.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 2);
    }
}
