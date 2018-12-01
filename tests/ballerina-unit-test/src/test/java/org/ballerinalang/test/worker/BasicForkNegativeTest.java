package org.ballerinalang.test.worker;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BasicForkNegativeTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/basic_fork_negative.bal");
    }

    @Test
    public void testBasicForkNegative() {
        Assert.assertEquals(result.getErrorCount(), 1, "Incorrect error count");
        BAssertUtil.validateError(result, 0,
                "worker send/receive interactions are invalid; worker(s) cannot move onwards from the state: " +
                        "'[a -> default, b -> default, FINISHED]'", 6, 5);
    }
}
