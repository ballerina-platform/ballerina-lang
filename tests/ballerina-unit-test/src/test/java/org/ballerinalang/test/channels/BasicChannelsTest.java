package org.ballerinalang.test.channels;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for basic channel functionality.
 */
public class BasicChannelsTest {

    CompileResult result;
    private static final String CHANNEL_TEST = "ChannelsTest";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/channels/channel-worker-interactions.bal");
        Assert.assertEquals(result.getErrorCount() , 0, "Channels with workers compilation");
        CompileResult result2 = BCompileUtil.compile("test-src/channels/channel-in-resources.bal");
        Assert.assertEquals(result2.getErrorCount(), 0, "Channels with resources test failed");
    }

    @Test(description = "Test channels in workers", groups = CHANNEL_TEST)
    public void testChannelsInWorker() {
        BValue[] returns = BRunUtil.invoke(result, "workerWithChannels");
        Assert.assertEquals(returns.length, 1, "Basic channel test, worker returns failed");
        Assert.assertEquals(returns[0].stringValue(), "{\"payment\":10000}", "Incorrect msg returned from channel");

        returns = BRunUtil.invoke(result, "sendBeforeReceive");
        Assert.assertEquals(returns.length, 1, "Basic channel test, send before receive failed");
        Assert.assertEquals(returns[0].stringValue(), "{\"payment\":10000}", "Incorrect msg returned from channel");
    }

    @Test(description = "Test channel messages with null keys", groups = CHANNEL_TEST)
    public void testNullKeys() {
        BValue[] returns = BRunUtil.invoke(result, "nullKeyChannels");
        Assert.assertEquals(returns.length, 1, "Basic channel test, channels with null key failed");
        Assert.assertEquals(returns[0].stringValue(), "{\"payment\":10000}", "Incorrect msg returned from channel");
    }

    @Test(description = "Test multiple interactions with channels", groups = CHANNEL_TEST)
    public void testMultipleInteractions() {
        BValue[] returns = BRunUtil.invoke(result, "multipleInteractions");
        Assert.assertEquals(returns.length, 1, "Basic channel test, channels with multiple interactions failed");
        Assert.assertEquals(returns[0].stringValue(), "{\"payment\":50000}", "Incorrect msg returned from channel");
    }

    @Test(description = "Test with multiple channels", groups = CHANNEL_TEST)
    public void testMultipleChannels() {
        BValue[] returns = BRunUtil.invoke(result, "multipleChannels");
        Assert.assertEquals(returns.length, 1, "Basic channel test, multiple channels test failed");
        Assert.assertEquals(returns[0].stringValue(), "{\"payment\":60000}", "Incorrect msg returned from channel");
    }

    @Test(description = "Test channels with xml messages", groups = CHANNEL_TEST)
    public void testXmlChannels() {
        BValue[] returns = BRunUtil.invoke(result, "xmlChannels");
        Assert.assertEquals(returns.length, 1, "Basic channel test, xml channels test failed");
        Assert.assertEquals(returns[0].stringValue(), "<payment>10000</payment>", "Incorrect msg returned from xml " +
                "channel");
    }

    @Test(description = "Test channels with simple datatypes", groups = CHANNEL_TEST)
    public void testSimpleTypeChannels() {
        BValue[] returns = BRunUtil.invoke(result, "primitiveTypeChannels");
        Assert.assertEquals(returns.length, 1, "Basic channel test, simple types channels test failed");
        Assert.assertEquals(returns[0].stringValue(), "10.5", "Incorrect msg returned from channel");
    }
}
