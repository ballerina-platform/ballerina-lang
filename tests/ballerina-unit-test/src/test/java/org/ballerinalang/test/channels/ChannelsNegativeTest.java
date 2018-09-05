package org.ballerinalang.test.channels;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test for channels.
 */
public class ChannelsNegativeTest {

    private static final String CHANNEL_TEST = "ChannelsTest";
    CompileResult result;

    @BeforeClass
    public void setup() {

        result = BCompileUtil.compile("test-src/channels/channel-negative-test.bal");
        Assert.assertEquals(result.getErrorCount(), 3, "Channels negative test error count");

    }

    @Test(description = "Test channel result's incompatible types", groups = CHANNEL_TEST)
    public void checkIncomptableResult() {

        Assert.assertEquals(result.getDiagnostics()[0].getPosition().getStartLine(), 7, "Wrong channel position line " +
                "number");
        Assert.assertEquals(result.getDiagnostics()[0].getMessage(), "incompatible types: expected 'json', found " +
                "'xml'", "Channel receive expression, incompatible result type error");

        Assert.assertEquals(result.getDiagnostics()[1].getPosition().getStartColumn(), 9, "Wrong channel position " +
                "column number");
        Assert.assertEquals(result.getDiagnostics()[1].getMessage(), "incompatible types: expected 'json', found " +
                "'string'", "Channel receive expression, incompatible result type error");

    }

    //todo:should change once we support other types as well
    @Test(description = "Test unsupported channel constraint types", groups = CHANNEL_TEST)
    public void checkChannelConstraintErrors() {

        Assert.assertEquals(result.getDiagnostics()[2].getPosition().getStartLine(), 18, "Incorrect position for " +
                "channel constraint error");
        Assert.assertEquals(result.getDiagnostics()[2].getMessage(), "incompatible types: 'channel' cannot be " +
                "constrained with 'map'", "Channel constraint type error message");
        Assert.assertEquals(result.getDiagnostics()[3].getPosition().getStartColumn(), 1, "Incorrect column position " +
                "for " +
                "channel constraint error");
        Assert.assertEquals(result.getDiagnostics()[3].getMessage(), "incompatible types: 'channel' cannot be " +
                "constrained with '(json,json)'", "Channel constraint type error message");
    }

}
