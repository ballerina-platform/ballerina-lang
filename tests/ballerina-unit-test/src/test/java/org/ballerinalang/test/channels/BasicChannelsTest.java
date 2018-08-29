package org.ballerinalang.test.channels;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for basic channel functionality
 */
public class BasicChannelsTest {

    CompileResult result;

    @Test(description = "Test channels in workers")
    public void setup() {
        result = BCompileUtil.compile("test-src/channels/channel-worker-interactions.bal");
        Assert.assertEquals(result.getErrorCount() , 0,"Basic channel test bal file compilation failed");
    }

    public void testChannelsInWorker() {
        BValue[] returns = BRunUtil.invoke(result, "workerWithChannels");
        Assert.assertEquals(returns[0].stringValue(), "{\"payment\":10000}");
    }
}
