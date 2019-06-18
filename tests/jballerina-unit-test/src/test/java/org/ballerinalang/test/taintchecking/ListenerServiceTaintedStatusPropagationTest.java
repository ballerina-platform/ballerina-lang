package org.ballerinalang.test.taintchecking;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ListenerServiceTaintedStatusPropagationTest {
    @Test
    public void testServiceVariables() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/propagation/listener-taintedness-propagation.bal");
        Assert.assertEquals(result.getDiagnostics().length, 0);
    }
}
