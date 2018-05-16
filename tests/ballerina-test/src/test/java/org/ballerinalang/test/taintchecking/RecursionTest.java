package org.ballerinalang.test.taintchecking;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test recursion conditions (direct recursions and cyclic invocations of functions), that will be addressed by the
 * the conflict resolution mechanism of taint analyzer.
 */
public class RecursionTest {
    // Test recursions.

    @Test
    public void testRecursiveFunctionCallingSensitiveFunction() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/recursions/" +
                "recursive-function-calling-sensitive-function.bal");
        Assert.assertTrue(result.getDiagnostics().length == 0);
    }

    @Test
    public void testRecursiveFunctionCallingSensitiveFunctionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/recursions/" +
                "recursive-function-calling-sensitive-function-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 12, 20);
    }

    @Test
    public void testRecursiveFunctionAlteringSensitiveStatusCallingSensitiveFunctionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/taintchecking/recursions/" +
                "cyclic-call-altering-sensitive-status-calling-sensitive-function-negative.bal");
        Assert.assertTrue(result.getDiagnostics().length == 1);
        BAssertUtil.validateError(result, 0, "tainted value passed to sensitive parameter 'secureIn'", 12, 20);
    }
}
