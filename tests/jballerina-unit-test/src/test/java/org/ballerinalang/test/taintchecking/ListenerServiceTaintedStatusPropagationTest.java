package org.ballerinalang.test.taintchecking;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ListenerServiceTaintedStatusPropagationTest {
    @Test
    public void testUntaintedListernBasedService() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/listener-taintedness-propagation-untainted-listener.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }

    @Test
    public void testTaintednessPropagationFromTaintedListener() {
        CompileResult result = BCompileUtil.compile(
                "test-src/taintchecking/propagation/listener-taintedness-propagation-tainted-listener.bal");
        Assert.assertEquals(result.getDiagnostics().length, 3);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'p'", 26, 23);
        BAssertUtil.validateError(result, 1, "tainted value passed to sensitive parameter 'p'", 27, 23);
        BAssertUtil.validateError(result, 2, "tainted value passed to sensitive parameter 'p'", 42, 23);
    }
}
