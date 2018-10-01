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

public class BallerinaStreamsV2ExternalTimeWindowTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        System.setProperty("enable.siddhiRuntime", "false");
        result = BCompileUtil.compile("test-src/streaming/streamingv2-external-time-window-test.bal");
    }

    @Test(description = "Test externalTime query")
    public void testExternalTimeQuery() {
                BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startExternalTimeWindowTest");
        System.setProperty("enable.siddhiRuntime", "true");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 6, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[2];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[3];

        Assert.assertEquals(employee0.get("name").stringValue(), "Naveen");
        Assert.assertEquals(((BInteger) employee0.get("count")).intValue(), 3);

        Assert.assertEquals(employee1.get("name").stringValue(), "Amal");
        Assert.assertEquals(((BInteger) employee1.get("count")).intValue(), 2);
    }
}
